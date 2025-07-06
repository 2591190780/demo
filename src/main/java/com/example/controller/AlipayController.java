package com.example.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.annotation.Auditable;
import com.example.config.AliPayConfig;
import com.example.entity.AliPay;
import com.example.entity.RestBean;
import com.example.entity.dto.TransactionAccountDto;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/alipay")
@Tag(name = "支付",description = "支付宝支付操作")
public class AlipayController {

    @Resource
    AliPayConfig aliPayConfig;

    @Resource
    TransactionProcessService transactionProcessService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    JwtUtils jwtUtils;

    @Resource
    ProductInfoSelectAccountService productInfoSelectAccountService;

    private static final String GATEWAY_URL ="https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT ="JSON";
    private static final String CHARSET ="utf-8";
    private static final String SIGN_TYPE ="RSA2";

    //生成订单信息并返回给前端

    @Auditable(
            operationType = "PAY_INFO_SINGLE_ALIPAY",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/payInfoSingle")
    public void  payInfoTodb(HttpServletRequest request,HttpServletResponse response
            ,@RequestBody TransactionAccountDto transactionAccountDto) throws IOException {

        response.setContentType("application/json;Charset=utf-8");
        /**
         * 在这里还需要加入 传入订单信息 与 商家库存 是否满足的逻辑。需要返回提示。
         * 在service中加入 订单有效性逻辑判断。
         */
        TransactionAccountDto dto = this.transactionProcessService.TransactionInfoAdd(transactionAccountDto,request);
        if (dto != null){
            response.getWriter().write(RestBean.success(dto).asJsonString());
        }else{
            response.getWriter().write(RestBean.failure(401,"未查询到订单信息").asJsonString());
        }
    }

    @Auditable(
            operationType = "PAY_INFO_MULTI",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/payInfoMulti")
    public void  payInfoMulti(HttpServletRequest request,HttpServletResponse response,@RequestBody List<TransactionAccountDto> dto) throws IOException {
        response.setContentType("application/json;Charset=utf-8");
        /**
         * 在这里还需要加入 传入订单信息 与 商家库存 是否满足的逻辑。需要返回提示。
         * 在service中加入 订单有效性逻辑判断。
         */
        List <TransactionAccountDto> dtoList = this.transactionProcessService.TransactionInfoAddMulti(dto);
        if (dtoList != null){
            response.getWriter().write(RestBean.success(dto).asJsonString());
        }else{
            response.getWriter().write(RestBean.failure(401,"请联系管理员").asJsonString());
        }

    }

    //根据redis中的缓存发送给alipay到支付页面

    @Auditable(
            operationType = "PAY_ALIPAY",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/pay") // 前端路径参数格式?subject=xxx&traceNo=xxx&totalAmount=xxx
    public void pay(HttpServletRequest httpRequest
            , HttpServletResponse  response) throws Exception {
        /**
         * 1. 用户先提交订单，执行transaction.add函数 添加交易信息等待用户支付
         *
         * 2. 提交订单后，用户跳转界面， 点击 支付  执行alipay相关函数
         *    此时需要从数据库中获取之前的交易信息。（15min限时）
         *
         * 3. 支付后，等待alipay回执信息。
         */
        String authorization = httpRequest.getHeader("Authorization");
        DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
        Integer userId = jwtUtils.toId(jwt);

        // 从Redis获取待支付订单哈希集
        String userKey = userId.toString();
        Set<String> hashes = stringRedisTemplate.opsForSet().members(userKey);

        // 清理过期订单哈希
        if (hashes != null) {
            for (String hash : hashes) {
                if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(hash))) {
                    stringRedisTemplate.opsForSet().remove(userKey, hash);
                }
            }
            // 重新获取有效哈希
            hashes = stringRedisTemplate.opsForSet().members(userKey);
        }
        // 检查有效订单
        if (hashes == null || hashes.isEmpty()) {
            response.getWriter().write(RestBean.failure(401, "没有待支付的订单").asJsonString());
            return;
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        StringBuilder productNames = new StringBuilder();

        // 生成唯一订单号（用户ID+时间戳+随机数）
        String combinedOrderId = "PAY_" + userId + "_" +
                System.currentTimeMillis() + "_" +
                ThreadLocalRandom.current().nextInt(1000, 9999);

        //将发送给支付宝的订单号和列表集合存储到redis中
        for (String hash : hashes) {
            //存入  订单号 : hash  的redis中
            stringRedisTemplate.opsForSet().add(combinedOrderId,hash);
            stringRedisTemplate.expire(combinedOrderId,15,TimeUnit.MINUTES);
            //删除原有的redis hash 缓存
            stringRedisTemplate.opsForSet().remove(userKey,hash);
        }

        int productCount = 0;
        for (String hash : hashes) {
            TransactionAccountDto order = transactionProcessService.getOrderByHash(hash);
            if (order == null || !"1".equals(order.getStatus())) {
                sendJsonResponse(response, 401, "订单状态无效或已过期"); return;
            }
            totalAmount = totalAmount.add(order.getTotalPrice());

            //——————————————————————————这里可以计算优惠逻辑————————————————————————————————————————
            /*
            数据库新增  商家优惠表 :  id  product_id farmer_id  折扣  折扣描述  折扣生效日期  折扣截至日期
                   新建实付金额 REAL_PAY_COUNT 存入 redis 缓存中 便于后续处理
            */
            //____________________________________end___________________________________________
            stringRedisTemplate.opsForValue().set( hash
                    , order.getTotalPrice().toString(),15,TimeUnit.MINUTES);
            // 只收集前3个商品名称
            if (productCount < 3) {
                String productName = productInfoSelectAccountService
                        .getProductInfoAccountByProductId(order.getProductId()).getName();
                if (productName != null) {
                    // 过滤特殊字符并截取
                    String cleanName = productName.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");
                    if (cleanName.length() > 10) cleanName = cleanName.substring(0, 10);
                    if (productCount > 0) productNames.append(",");
                    productNames.append(cleanName);
                    productCount++;
                }
            }
        }
        // 构建订单标题
        String subject = productNames.toString();
        if (subject.isEmpty()) {
            subject = "商品订单";
        } else if (hashes.size() > 3) {
            subject += "等" + hashes.size() + "件商品";
        }
        // 确保标题长度合规（支付宝要求<=256字符）
        if (subject.length() > 128) {
            subject = subject.substring(0, 125) + "...";
        }
        // 创建支付宝请求
        AliPay aliPay = new AliPay();
        aliPay.setTotalAmount(totalAmount.doubleValue());
        aliPay.setSubject(subject);
        aliPay.setTraceNo(combinedOrderId);
        // 配置支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(
                "https://openapi-sandbox.dl.alipaydev.com/gateway.do",
                aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(),
                "json",
                "UTF-8",
                aliPayConfig.getAlipayPublicKey(),
                "RSA2");

        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setReturnUrl(Const.APLIPAY_RETURN_URL);
        payRequest.setNotifyUrl(Const.APLIPAY_NOTIFY_URL);

        // 构建业务参数 - 使用LinkedHashMap保持顺序
        Map<String, Object> bizContentMap = new LinkedHashMap<>();
        bizContentMap.put("out_trade_no", aliPay.getTraceNo());
        bizContentMap.put("total_amount", String.format("%.2f", aliPay.getTotalAmount()));
        bizContentMap.put("subject", aliPay.getSubject());
        bizContentMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContentMap.put("timeout_express", "15m");

        // 使用Jackson安全序列化
        ObjectMapper objectMapper = new ObjectMapper();
        String bizContentJson = objectMapper.writeValueAsString(bizContentMap);
        payRequest.setBizContent(bizContentJson);

        // 发送请求
        try {
            String form = alipayClient.pageExecute(payRequest).getBody();
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(form);
            response.getWriter().flush();
        } catch (AlipayApiException e) {
            response.getWriter().write(RestBean.failure(500,
                    "支付系统错误: " + e.getErrCode()).asJsonString());
        }


    }

    @Auditable(
            operationType = "ALIPAY_RETURN",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/alipay/return")
    public String handleReturn(HttpServletRequest request) throws AlipayApiException {
        // 1. 获取所有参数
        Map<String, String> params = getAllParameters(request);
        // 2. 打印原始参数（调试用）
        System.out.println("同步回调原始参数: " + params);
        try {
            // 3. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    aliPayConfig.getAlipayPublicKey(),
                    CHARSET,
                    SIGN_TYPE
            );
            if (signVerified) {
                // 4. 处理支付成功逻辑
                String outTradeNo = params.get("out_trade_no");
                String totalAmount = params.get("total_amount");
                String tradeNo = params.get("trade_no");  //支付宝的交易号
                System.out.println("同步回调支付成功: 订单号=" + outTradeNo + ", 金额=" + totalAmount);

                // 5. 重定向到前端支付成功页面
                return "redirect:http://your-frontend-domain/payment-success.html";
            } else {
                System.err.println("同步回调验签失败");
                // 验签失败重定向到失败页面
                return "redirect:http://your-frontend-domain/payment-failed.html";
            }
        } catch (AlipayApiException e) {
            System.err.println("同步回调验签异常: " + e.getMessage());
            return "redirect:http://your-frontend-domain/payment-error.html";
        }
    }

    @Auditable(
            operationType = "NOTIFY_ALIPAY",
            captureBefore = true,
            captureAfter = true
    )
    @PostMapping("/notify")  // 注意这里必须是POST接口
    public String payNotify(HttpServletRequest request) throws Exception {
        // 1. 获取所有参数
        Map<String, String> params = getAllParameters(request);
        // 2. 打印原始参数（调试用）
        System.out.println("异步回调原始参数: " + params);
        try {
            // 3. 检查基本参数
            String tradeStatus = params.get("trade_status");
            if (tradeStatus == null) {
                System.err.println("支付宝回调缺少trade_status参数");
                return "failure";
            }
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                System.err.println("交易状态异常: " + tradeStatus);
                return "failure";
            }
            // 4. 验证通知ID（防止伪造通知）
            if (!isValidNotifyId(params.get("notify_id"))) {
                System.err.println("通知ID验证失败");
                return "failure";
            }
            // 5. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    aliPayConfig.getAlipayPublicKey(),
                    CHARSET,
                    SIGN_TYPE
            );
            if (!signVerified) {
                // 打印详细的验签信息用于调试
                String signContent = AlipaySignature.getSignContent(params);
                System.err.println("验签失败，待验签内容: " + signContent);
                System.err.println("签名值: " + params.get("sign"));
                System.err.println("支付宝公钥: " + aliPayConfig.getAlipayPublicKey());

                return "failure";
            }
            // 6. 验证时间戳（防止重放攻击）
            if (!isValidTimestamp(params.get("notify_time"))) {
                System.err.println("回调时间已过期");
                return "failure";
            }

            // 7. 处理业务逻辑
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");

            System.out.println("支付成功，订单: " + outTradeNo);
            System.out.println("支付宝交易号: " + tradeNo);
            System.out.println("支付金额: " + totalAmount);

            /**
             * 这里对数据库中 交易记录表的商品交易状态进行了更新。
             */
            Set<String> hashes = stringRedisTemplate.opsForSet().members(outTradeNo); //获取redis中交易hash的缓存
            for (String hash : hashes) {
                String counts = stringRedisTemplate.opsForValue().get(hash);
                BigDecimal count = BigDecimal.valueOf(Float.parseFloat(counts));
                TransactionAccountDto dto = new TransactionAccountDto();
                dto.setActualPayment(count);
                dto.setCertificationHash(hash);
                dto.setAlipayOrder(tradeNo);

                /**
                 * 这里要 执行 只能合约，NFT触发机制，hash上链等操作  对相应的表格进行操作 user_nft ......
                 * 还没写。
                 */

                if( transactionProcessService.transactionStatusUpdate(dto,"2")){
                    stringRedisTemplate.delete(hash);
                    stringRedisTemplate.opsForSet().remove(outTradeNo,hash);

                }
                System.out.println(stringRedisTemplate.opsForSet().size(outTradeNo));
            }

            // TODO: 这里添加您的订单状态更新逻辑
            // 例如: orderService.updateOrderStatus(outTradeNo, "PAID");
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "failure";
        }
    }

    @Auditable(
            operationType = "PAY_CANCEL",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/payCancel")
    public <T> RestBean<T> payCancel(HttpServletRequest request, @Parameter @Valid String hash) throws Exception {
        return this.transactionProcessService.cancelTransaction(request,hash)?
                RestBean.success():RestBean.failure(401,"订单已失效或订单不存在");
    }

    @Auditable(
            operationType = "PAY_CANCEL_MULTI",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/payCancelMulti")
    public <T> RestBean<T> payCancelMulti(HttpServletRequest request, @Parameter @Valid List<String> hashes) throws Exception {
        return this.transactionProcessService.cancelTransactionMulti(request,hashes)?
                RestBean.success():RestBean.failure(401,"订单已失效或订单不存在");
    }

    @Auditable(
            operationType = "PAY_SELECT",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/paySelect")
    public  <T> RestBean<T> paySelect(HttpServletRequest request
            ,HttpServletResponse response, @Parameter @Valid String Id) throws IOException {
        Integer userId = jwtUtils.convertToInteger(Id);
        TransactionAccountDto dto = this.transactionProcessService.transactionSelect(request,userId);
        if (dto == null) {
            return RestBean.failure(401,"未查询到您的订单");
        }
        response.getWriter().write(RestBean.success(dto).asJsonString());
        return null;
    }

    @Auditable(
            operationType = "PAY_SELECT_MULTI",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/paySelectMulti")
    public  <T> RestBean<T> paySelectMulti(HttpServletRequest request
            , @Parameter @Valid String Id
            ,HttpServletResponse response ) throws IOException {
        Integer userId = jwtUtils.convertToInteger(Id);
        List<TransactionAccountDto> dtoList;
        dtoList= this.transactionProcessService.transactionSelectMulti(request,userId);
        if (dtoList == null) {
            return RestBean.failure(401,"未查询到您的订单");
        }
        response.getWriter().write(RestBean.success(dtoList).asJsonString());
        return null;

    }


    // 辅助方法：发送JSON响应
    private void sendJsonResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 手动构建JSON确保有效性
        String json = "{\"code\":" + code + ",\"message\":\"" + message + "\"}";
        response.getWriter().write(json);
        response.getWriter().flush();
    }

    // 通用方法：获取所有请求参数
    private Map<String, String> getAllParameters(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();

        // 获取所有参数名
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);

            // 处理中文乱码问题
            try {
                // 支付宝使用UTF-8编码，直接使用即可
                params.put(paramName, paramValue);
            } catch (Exception e) {
                params.put(paramName, paramValue); // 回退方案
            }
        }
        return params;
    }

    // 验证时间戳是否在合理范围内（2小时内）
    private boolean isValidTimestamp(String notifyTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date notifyDate = format.parse(notifyTime);
            long timeDiff = System.currentTimeMillis() - notifyDate.getTime();

            // 接受2小时内的回调
            return timeDiff <= 2 * 60 * 60 * 1000;
        } catch (Exception e) {
            return false;
        }
    }
    // 验证通知ID（需要alipayClient实例）
    private boolean isValidNotifyId(String notifyId) {
        // 实际项目中需要alipayClient实例来验证
        // return alipayClient.checkNotifyId(notifyId);

        // 沙箱环境跳过此验证
        return true;
    }
}
