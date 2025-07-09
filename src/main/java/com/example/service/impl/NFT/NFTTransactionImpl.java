package com.example.service.impl.NFT;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.NFTPendingApplication;
import com.example.entity.dto.NFTTransactionDto;
import com.example.mapper.NFT.NFTTransactionMapper;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTTransactionService;
import com.example.service.NFT.UserNFTService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

        if(Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(Const.NFT_TRANSACTION, saveKey))){
            return false;
        }
        stringRedisTemplate.opsForSet().add(Const.NFT_TRANSACTION+":"+application.getSellerID(),saveKey);
        stringRedisTemplate.expire(Const.NFT_TRANSACTION+":"+application.getSellerID(),1,TimeUnit.DAYS);

        stringRedisTemplate.opsForValue().set(saveKey,"",1, TimeUnit.DAYS);
        return true;
    }


    @Override
    public List<NFTPendingApplication>  getApplyForNFT(HttpServletRequest request) {
        Integer id = jwtUtils.getRequesetId(request);
        Set<String> keys = stringRedisTemplate.opsForSet().members(Const.NFT_TRANSACTION+":"+id);
        if (keys != null && keys.isEmpty()) return null;
        for (String key : keys) {
            if(!Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) { //申请信息已过期，删除此信息。
                stringRedisTemplate.opsForSet().remove(Const.NFT_TRANSACTION+":"+id,key);
            }
        }
        keys = stringRedisTemplate.opsForSet().members(Const.NFT_TRANSACTION+":"+id);

        List<NFTPendingApplication> dtoList = new java.util.ArrayList<>(List.of());
        if (keys == null || keys.isEmpty()) return null;
        for (int i = 1;i<=keys.size();i++){
            String key = keys.iterator().next();
            String[] param = key.split(":");
            //买家id+卖家id+nftid+价格
            NFTPendingApplication dto = new NFTPendingApplication(
                    param[1],param[2],param[3], BigDecimal.valueOf(Float.parseFloat(param[4]))
                    ,nftInfoService.NFTInfoSelectByTemplateId(Integer.valueOf(param[3]))
            );
            dtoList.add(dto);
        }
        return dtoList;
    }




    @Override
    public boolean addNFTTransaction(NFTTransactionDto nftTransactionDto){
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

        LocalDateTime createTime = LocalDateTime.now();
        nftTransactionDto.setTxTime(createTime);
        String hash = this.blockchainHashUtil.generateNFTTransactionRuleHash(nftTransactionDto);
        nftTransactionDto.setTxHash(hash);
        if(this.save(nftTransactionDto)){
            /**
             * 这里要执行上链操作 ----->  blockChainEvidenceService
             * 如果信息存储成功--->生成区块链凭证初始信息--->调用合约进行上链操作
             *      --->区块链返回上链成功的区块号--->更新数据库的上链信息。
             */
            //对user_nft表格执行操作
            if (this.userNFTService.UserNFT(nftTransactionDto)){
                this.update().eq("tx_hash", hash)
                        .set("active",1).update();
                stringRedisTemplate.delete(saveKey);
                stringRedisTemplate.opsForSet().remove(Const.NFT_TRANSACTION+":"+
                        nftTransactionDto.getToUser(),saveKey);
                return true;
            }
            return false;
        }
        return false;
    }

}
