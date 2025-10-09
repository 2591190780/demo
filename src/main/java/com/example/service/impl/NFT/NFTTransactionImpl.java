package com.example.service.impl.NFT;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.NFTPendingApplication;
import com.example.entity.dto.BlockChainEvidenceDto;
import com.example.entity.dto.NFTTransactionDto;
import com.example.mapper.BlockChainEvidenceMapper;
import com.example.mapper.NFT.NFTTransactionMapper;
import com.example.service.AccountService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTTransactionService;
import com.example.service.NFT.UserNFTService;
import com.example.service.blockchain.MessageReportService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import com.example.utils.WeBaseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class NFTTransactionImpl extends ServiceImpl<NFTTransactionMapper, NFTTransactionDto>
        implements NFTTransactionService {

    @Resource
    BlockchainHashUtil blockchainHashUtil;

    @Resource
    UserNFTService userNFTService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    JwtUtils jwtUtils;

    @Resource
    NFTInfoService nftInfoService;

    @Resource
    AccountService accountService;

    @Resource
    MessageReportService messageReportService;
    @Resource
    WeBaseUtils weBaseUtils;
    @Resource
    BlockChainEvidenceMapper blockChainEvidenceMapper;

    @Override
    public List<NFTTransactionDto> selectNFTTransactionByNFTId(Integer nftId){
        return this.query().eq("nft_id", nftId).list();
    }
    @Override
    public  NFTTransactionDto selectNFTTransactionById(Integer Id){
        return this.query().eq("tx_id", Id).one();
    }

    @Override
    public List<NFTTransactionDto> selectNFTTransactionByFromId(Integer fromID){
        return this.query().eq("from_user", fromID).list();
    }

    @Override
    public List<NFTTransactionDto> selectNFTTransactionByToId(Integer toId){
        return this.query().eq("to_user", toId).list();
    }


    @Override
    public  NFTTransactionDto selectOrderByTime(Integer nftId){
        return this.query().eq("nft_id", nftId)
                .orderByDesc("tx_time").last("LIMIT 1").one();
    }

    @Override
    public  List<NFTTransactionDto> selectNFTCondition(NFTTransactionDto dto){
        QueryWrapper<NFTTransactionDto> queryWrapper = new QueryWrapper<>();
        // 精确匹配条件
        if (dto.getTxId() != null) {
            queryWrapper.eq("tx_id",dto.getTxId());
        }
        if (dto.getNftId() != null) {
            queryWrapper.eq("nft_id)", dto.getNftId());
        }
        if (dto.getFromUser() != null) {
            queryWrapper.eq("from_user", dto.getFromUser());
        }
        if (dto.getToUser() != null) {
            queryWrapper.eq("to_user", dto.getToUser());
        }
        if (dto.getType() == 1 || dto.getType() == 2 || dto.getType() == 3) {
            queryWrapper.eq("type", dto.getType());
        }

        return this.list(queryWrapper);
    }

    @Override
    public boolean buyApplyForNFT(NFTPendingApplication application){
        //用户间的交易需要 买家先申请（信息存入redis中） 卖家 从redis中获取交易申请。
        //  NFT_TRANSACTION:1:2:3:700.00 买家id+卖家id+nftid+价格
        String saveKey = Const.NFT_TRANSACTION + ":"
                + application.getBuyerID() + ":" + application.getSellerID()
                +":"+ application.getNftID() + ":" + application.getPrice().toString();
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(saveKey))){
            return false;
        }
        stringRedisTemplate.opsForSet().add(Const.NFT_TRANSACTION+":"+application.getSellerID(),saveKey);
        stringRedisTemplate.expire(Const.NFT_TRANSACTION+":"+application.getSellerID()
                ,1,TimeUnit.DAYS);
        stringRedisTemplate.opsForSet().add(Const.NFT_TRANSACTION+":"+application.getBuyerID(),saveKey);
        stringRedisTemplate.expire(Const.NFT_TRANSACTION+":"+application.getBuyerID()
                ,1,TimeUnit.DAYS);
        stringRedisTemplate.opsForValue().set(saveKey,"",1, TimeUnit.DAYS);
        return true;
    }

    public String checkApplyStatusInRedis(String saveKey){
        String[] param = saveKey.split(":");
        if (
                Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(Const.NFT_TRANSACTION + ":" + param[1], saveKey))
                        &&
                        Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(Const.NFT_TRANSACTION + ":" + param[2], saveKey))
        ){
            return "待处理";
        }else if(
                Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(Const.NFT_TRANSACTION + ":" + param[1], saveKey))
                        &&
                        !Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(Const.NFT_TRANSACTION + ":" + param[2], saveKey))
        ){
            return "拒绝";
        }
        return null;
    }


    public boolean deleteApplyInRedis(String saveKey){
        String[] param = saveKey.split(":");
        stringRedisTemplate.opsForSet().remove(Const.NFT_TRANSACTION+":"+param[2],saveKey);
        return true;
    }

    @Override
    public List<NFTPendingApplication>  getApplyForNFT(Integer id) {
        // NFT_TRANSACTION:1:2:3:700.00 买家id+卖家id+nftid+价格
        // Const.NFT_TRANSACTION:sellerID
        Set<String> keys = stringRedisTemplate.opsForSet().members(Const.NFT_TRANSACTION+":"+id);
        if (keys != null && keys.isEmpty()) return null; //检查空
        for (String key : keys) {
            if(!Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) { //申请信息已过期，删除此信息。
                stringRedisTemplate.opsForSet().remove(Const.NFT_TRANSACTION+":"+id,key);
            }
        } //检查有效key
        keys = stringRedisTemplate.opsForSet().members(Const.NFT_TRANSACTION+":"+id); //获取id的key = NFT_TRANSACTION:1:2:3:700.00
        //创建对象列表。
        List<NFTPendingApplication> dtoList = new java.util.ArrayList<>(List.of());
        //检查空
        if (keys == null || keys.isEmpty()) return null;
        //赋值
        for (String key : keys) {
            try {
                // key 格式示例: somePrefix:buyerId:sellerId:nftId:price
                String[] param = key.split(":");
                if (param.length < 5) continue; // 避免数组越界
                String status = this.checkApplyStatusInRedis(key);
                // 获取剩余存活时间
                Long ttlSeconds = stringRedisTemplate.getExpire(key);
                if (ttlSeconds != null && ttlSeconds > 0) {
                    long expireTimeMillis = System.currentTimeMillis() + ttlSeconds * 1000;
                    long startTimeMillis  = expireTimeMillis - TimeUnit.DAYS.toMillis(1);
                    LocalDateTime start = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(startTimeMillis), ZoneId.systemDefault());
                    LocalDateTime end = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(expireTimeMillis), ZoneId.systemDefault());
                    // 构建 DTO
                    NFTPendingApplication dto = new NFTPendingApplication(
                            param[1],                          // buyerId
                            param[2],                          // sellerId
                            param[3],                          // nftId
                            new BigDecimal(param[4]),          // price
                            nftInfoService.NFTInfoSelectByTemplateId(Integer.parseInt(param[3])),
                            start,
                            end,
                            status
                    );
                    dtoList.add(dto);
                }
            } catch (Exception e) {
                // 记录异常，不影响其他 key
                log.error("处理 key={} 出现异常");
            }
        }
        return dtoList;
    }



    @Override
    public boolean replyNFTTransaction(NFTTransactionDto nftTransactionDto,Integer ans) throws Exception {
        /**
         *        当用户在购买农产品时 支付完成 触发智能合约 发放NFT  --> nft_type 为 赠送 2
         *         用户之间也可以交易NFT、赠送 -->type 交易 1
         *         用户也可以售卖自己的NFT  -->type 3
         *         前端传入  nft_id from_user to_user  type  price
         */
        //  NFT_TRANSACTION:1:2:3:700.00 买家id+卖家id+nftid+价格

        String saveKey = Const.NFT_TRANSACTION + ":"
                + nftTransactionDto.getToUser() + ":" + nftTransactionDto.getFromUser()
                +":"+ nftTransactionDto.getNftId()+ ":" + nftTransactionDto.getPrice().toString();
        if(!Boolean.TRUE.equals(stringRedisTemplate.hasKey(saveKey))) return false;
        if (ans==0){
            this.deleteApplyInRedis(saveKey);
            return true;
        }
        LocalDateTime createTime = LocalDateTime.now();
        String hash = this.blockchainHashUtil.generateNFTTransactionRuleHash(nftTransactionDto);
        nftTransactionDto.setTxTime(createTime);
        nftTransactionDto.setTxHash(hash);
        if(this.save(nftTransactionDto)){
            String fromAddress = accountService.findAccountById(nftTransactionDto.getFromUser()).getWalletAddress();
            String toAddress = accountService.findAccountById(nftTransactionDto.getToUser()).getWalletAddress();
            String contractAddress  = Const.CONTRACT_FOR_MESSAGE_REPORT;
            String methodName = Const.CONTRACT_FOR_MESSAGE_REPORT_METHOD_ADD_EVIDENCE;
            List<Object> parameters = new ArrayList<>();
            parameters.add(0,4);
            parameters.add(1,nftTransactionDto.getTxId().intValue());
            parameters.add(2,hash);
            /** （已完成）
             * 这里要执行上链操作 ----->  blockChainEvidenceService
             * 如果信息存储成功--->生成区块链凭证初始信息--->调用合约进行上链操作
             *      --->区块链返回上链成功的区块号--->更新数据库的上链信息。
             */
            List<Object> para = new ArrayList<>();
            para.add(0, nftTransactionDto.getNftId());
            para.add(1,toAddress);
            String priceStr = String.valueOf(nftTransactionDto.getPrice());          // "10000000000000.000000000000000000"
            BigDecimal bd = new BigDecimal(priceStr)
                    .setScale(18, RoundingMode.HALF_UP);             // 保证 18 位小数
            BigInteger onChainValue = bd.movePointRight(18)          // 去掉小数点
                    .toBigInteger();                                 // 10000000000000000000000000000
            para.add(2, onChainValue);
            // 1. 转 BigDecimal
//            BigDecimal bd2 = new BigDecimal(onChainValue);
//            // 2. ÷10^18 移回小数点
//            BigDecimal realPrice = bd2.movePointLeft(18);             // 10000000000000.000000000000000000
//            // 3. 去掉尾部多余的 0（可选）
//            String display = realPrice.stripTrailingZeros()
//                    .toPlainString();                                  // "10000000000000"
            // 入库/上链
            // para.add(2, realPrice.toPlainString());           // 写入
            //对链上NFT归属进行操作//这里是对NFT转移的发生记录
            Map<String, Object> result = this.weBaseUtils.callContractMethod(fromAddress,Const.CONTRACT_FOR_NFT_INFO,
                    Const.CONTRACT_FOR_NFT_INFO_METHOD_TRADENFT,para);
            if (result.get("statusOK") == Boolean.FALSE) return false;
            //返回错误代码链上链下信息有误，不予执行。
            //这里是记录了交易的发生。
            String result1 = this.messageReportService.blockChainEvidenceReport(methodName,parameters,fromAddress,contractAddress);
            String result2 = this.messageReportService.blockChainEvidenceReport(methodName,parameters,toAddress,contractAddress);
            System.out.println("买家购买信息上链结果:"+result1+"  "+"卖家购买信息上链结果:"+result2);
            //对user_nft表格执行操作
            if (this.userNFTService.UserNFT(nftTransactionDto)){
                stringRedisTemplate.delete(saveKey);
                stringRedisTemplate.opsForSet().remove(Const.NFT_TRANSACTION+":"+
                        nftTransactionDto.getToUser(),saveKey);
                String txHash = (String) result.get("transactionHash");
                String blockNumber = (String) result.get("blockNumber");
                BlockChainEvidenceDto evidenceDto = new BlockChainEvidenceDto(
                        null,4, nftTransactionDto.getTxId().intValue()
                        ,txHash,hash,blockNumber, LocalDateTime.now()
                );
                this.blockChainEvidenceMapper.insert(evidenceDto);
                this.update().eq("tx_hash", hash)
                        .set("active",1).update();
                return true;
            }
            return false;
        }
        return false;
    }

}