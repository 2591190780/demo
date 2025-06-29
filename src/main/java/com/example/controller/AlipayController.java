package com.example.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.easysdk.factory.Factory;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.config.AliPayConfig;
import com.example.entity.AliPay;
import com.example.entity.dto.TransactionAccountDto;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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

    private static final String GATEWAY_URL ="https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    //签名方式
    private static final String SIGN_TYPE = "RSA2";

    @PutMapping("/payinfoTodb")
    public void  payInfoTodb(HttpServletRequest request,HttpServletResponse response
            ,@RequestBody TransactionAccountDto transactionAccountDto){
        this.transactionProcessService.TransactionInfoAdd(transactionAccountDto,request);
    }

    //生成表单，回调显示给前端。
    @GetMapping("/pay") // &subject=xxx&traceNo=xxx&totalAmount=xxx
    public void pay(HttpServletRequest request,
                    HttpServletResponse response) throws Exception {

        /**
         * 1. 用户先提交订单，执行transaction.add函数 添加交易信息等待用户支付
         *
         * 2. 提交订单后，用户跳转界面，获取订单信息，确定订单信息后 点击 支付  执行alipay相关函数
         *    此时需要从数据库中获取之前的交易信息。（15min限时）
         *
         * 3. 支付后，等待alipay回执信息。
         */

        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
        Integer id = jwtUtils.toId(jwt);

        // 1. 根据订单ID查询订单信息(从redis缓存中获取)
        Set<String> hashes = stringRedisTemplate.opsForSet().members(id.toString());
        if (hashes == null || hashes.isEmpty()) {
            response.getWriter().write("没有待支付的订单");
            return;
        }
        // 2. 获取所有订单信息并验证
        List<TransactionAccountDto> pendingOrders = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (String hash : hashes) {
           TransactionAccountDto order =  transactionProcessService.getOrderByHash(hash);
           if (order == null || !"1".equals(order.getStatus())) {
                response.getWriter().write("订单不存在或状态异常");
                return ;
            }
            pendingOrders.add(order);
            totalAmount = totalAmount.add(order.getTotalPrice());

        }
        // 3. 生成合并支付的唯一标识
        String combinedOrderId = "COMBINED_" + UUID.randomUUID().toString().replace("-", "");
        // 4. 创建支付宝支付请求
        AlipayClient alipayClient = new DefaultAlipayClient(
                GATEWAY_URL,
                aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(),
                "json",
                "UTF-8",
                aliPayConfig.getAlipayPublicKey(),
                "RSA2");
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());
        payRequest.setReturnUrl(GATEWAY_URL);

        // 5. 设置业务参数（使用合并订单信息）
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", combinedOrderId); // 使用合并订单ID
        bizContent.put("total_amount", totalAmount);

        // 构建订单标题，包含订单数量信息
        String subject = String.format("合并支付(%d个订单)-%.2f元",
                pendingOrders.size(), totalAmount);
        bizContent.put("subject", subject);

        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.put("timeout_express", "15m");

        // 添加自定义参数，存储原始订单信息（用于回调处理）
        JSONArray orderArray = new JSONArray();
        for (TransactionAccountDto order : pendingOrders) {
            JSONObject orderInfo = new JSONObject();
            orderInfo.put("order_id", order.getOrderId());
            orderInfo.put("hash", order.getCertificationHash());
            orderArray.add(orderInfo);
        }

        JSONObject passbackParams = new JSONObject();
        passbackParams.put("orders", orderArray);
        bizContent.put("passback_params", passbackParams.toJSONString());

        payRequest.setBizContent(bizContent.toString());

        // 6. 生成支付表单
        String form = alipayClient.pageExecute(payRequest).getBody();

        // 7. 存储合并订单信息到Redis（用于回调处理）
        storeCombinedOrder(combinedOrderId, pendingOrders, id);

        // 8. 返回支付页面
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(form);

    }

    // 存储合并订单信息
    private void storeCombinedOrder(String combinedOrderId,
                                    List<TransactionAccountDto> orders,
                                    Integer userId) {
        JSONObject combinedInfo = new JSONObject();
        combinedInfo.put("user_id", userId);

        JSONArray orderArray = new JSONArray();
        for (TransactionAccountDto order : orders) {
            JSONObject orderInfo = new JSONObject();
            orderInfo.put("order_id", order.getOrderId());
            orderInfo.put("hash", order.getCertificationHash());
            orderInfo.put("amount", order.getTotalPrice());
            orderArray.add(orderInfo);
        }

        combinedInfo.put("orders", orderArray);

        // 存储到Redis，15分钟过期
        stringRedisTemplate.opsForValue().set(
                "combined:order:" + combinedOrderId,
                combinedInfo.toJSONString(),
                15, TimeUnit.MINUTES
        );
    }


    @PostMapping("/notify")  // 注意这里必须是POST接口
    public String payNotify(HttpServletRequest request) throws Exception {
        try {
            // 1. 转换参数
            Map<String, String> params = this.convertParams(request);

            // 2. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    aliPayConfig.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );
            if (!signVerified) {
                return "failure";
            }
            // 3. 验证交易状态
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                return "success";
            }
            // 4. 获取合并订单ID
            String combinedOrderId = params.get("out_trade_no");

            // 5. 从Redis获取合并订单息
            String combinedInfoStr = stringRedisTemplate.opsForValue().get("combined:order:" + combinedOrderId);
            if (combinedInfoStr == null) {

                return "failure";
            }

            JSONObject combinedInfo = JSON.parseObject(combinedInfoStr);
            Integer userId = combinedInfo.getInteger("user_id");
            JSONArray orders = combinedInfo.getJSONArray("orders");
            // 6. 处理每个子订单
            for (int i = 0; i < orders.size(); i++) {
                JSONObject orderInfo = orders.getJSONObject(i);
                String orderId = orderInfo.getString("order_id");
                String hash = orderInfo.getString("hash");
                BigDecimal amount = orderInfo.getBigDecimal("amount");

                // 7. 更新订单状态
                TransactionAccountDto orderUpdate = new TransactionAccountDto();
                orderUpdate.setOrderId(orderId);
                orderUpdate.setActualPayment(amount);
                orderUpdate.setAlipayOrder(Long.valueOf(params.get("trade_no")));
                orderUpdate.setCompleteTime(LocalDateTime.now());

                transactionProcessService.transactionStatusUpdate(orderUpdate,"2");
                // 8. 清理Redis缓存
                cleanOrderFromRedis(userId, hash);

            }

            // 9. 清理合并订单信息
            stringRedisTemplate.delete("combined:order:" + combinedOrderId);

            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }

    // 清理订单缓存
    private void cleanOrderFromRedis(Integer userId, String hash) {
        // 从用户待支付集合中移除
        stringRedisTemplate.opsForSet().remove(userId.toString(), hash);

        // 删除订单缓存
        stringRedisTemplate.delete("order:detail:" + hash);
    }

    // 参数转换实现
    private Map<String, String> convertParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }

}
