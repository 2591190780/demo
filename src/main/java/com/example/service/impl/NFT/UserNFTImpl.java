package com.example.service.impl.NFT;

import com.alipay.api.domain.AccountInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.*;
import com.example.entity.records.BestSellingProducts;
import com.example.entity.records.MostNFTNumberOwner;
import com.example.entity.records.SalesTrend;
import com.example.entity.vo.response.ProductVO;
import com.example.mapper.NFT.UserNFTMapper;
import com.example.service.AccountService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.service.NFT.UserNFTService;
import com.example.utils.DataTypeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserNFTImpl extends ServiceImpl<UserNFTMapper, UserNFTDto> implements UserNFTService {

    @Resource
    NFTInfoService nftInfoService;

    @Resource
    NFTRuleService nftRuleService;

    @Resource
    UserNFTMapper userNFTMapper;

    @Resource
    AccountService accountService;

    @Override
    public  List<MostNFTNumberOwner> getMostNFTNumberOwner(){
        QueryWrapper<UserNFTDto> qw = new QueryWrapper<>();
        qw.select("user_id", "COUNT(nft_id) AS total_count")
                .eq("status",1)
                .groupBy("user_id")
                .orderByDesc("total_count")
                .last("LIMIT 10");

        List<Map<String, Object>> dataList = this.getBaseMapper().selectMaps(qw);
        List<MostNFTNumberOwner> result = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            Long userId = DataTypeUtils.getLongValue(data.get("user_id"));
            Integer count = DataTypeUtils.getIntegerValue(data.get("total_count"));
            String userName = getProductNameById(Math.toIntExact(userId));

            result.add(new MostNFTNumberOwner(userName, count));
        }
        return result;
    }
    private String getProductNameById(Integer uid) {
        try {
            Account account = this.accountService.findAccountById(uid);
            return account != null ? account.getUsername() : "未知用户";
        } catch (Exception e) {
            log.warn("获取产品名称失败, productId: {%d,%s}".formatted(uid, e));
            return "未知商品";
        }
    }
    @Override
    public UserNFTDto selectNFTById(Integer id){
        return query().eq("id", id).one();
    }
    @Override
    public List<UserNFTDto> selectNFTByUid(Integer uid){
        return query().eq("user_id", uid).list();
    }
    @Override
    public List<UserNFTDto> selectNFTByNFTid(Integer nftId){
        return query().eq("nft_id", nftId).list();
    }
    @Override
    public UserNFTDto selectNFTByUNid(Integer userId,Integer nftId){
        return query().eq("nft_id", nftId)
                .eq("user_id",userId).one();
    }

    @Override
    public  boolean deleteUserNFT(Integer id,Integer nftid){ //销毁NFT
        QueryWrapper<UserNFTDto> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",id).eq("nft_id", nftid);
        this.userNFTMapper.delete(wrapper);
        return true;
    }

    @Override
    public boolean updateNFTStatus(Integer uid,Integer nftId ,Integer status){
        return this.update().eq("user_id",uid).eq("nft_id",nftId).set("status",status).update();
    }


    @Override
    public boolean UserNFT(NFTTransactionDto dto){
        //获取交易双方NFT收藏的信息  并在user_nft中查找是否有该nft的信息
        UserNFTDto fromUserDto = this.selectNFTByUNid(dto.getFromUser(),dto.getNftId());
        UserNFTDto toUserDto = this.selectNFTByUNid(dto.getToUser(),dto.getNftId());
        // 查找NFT的 相关信息以及NFT的发行规则
        NFTInfoDto nftInfo = this.nftInfoService.NFTInfoSelectByTemplateId(dto.getNftId());
        NFTRuleDto nftRuleDto = this.nftRuleService.nftRuleSelectByActId(nftInfo.getTemplateId());

        //检查NFT是否已过期。
        LocalDateTime now = LocalDateTime.now();
        if(!Objects.equals(nftRuleDto.getValidityPeriod(), "-1")){
            if(now.isAfter(nftRuleDto.getPassActive())){return false;}
        }
        //如果是自动发布的话还需要查询NFT信息内的NFT剩余数量是否足够
        if (fromUserDto == null || dto.getType()==2){
            //NFT交易发货人与NFT发布者不是同一个人
            if(!Objects.equals(nftInfo.getPublicBy(), dto.getFromUser())){return false;}
            //检查发布的NFT是否有足够的剩余数量。
            if(nftInfo.getRemainCount()<1){return false;}
            //交易对象已经拥有该类型的nft了则不允许交易
            if (toUserDto != null ){return false;}
            //全部验证已通过对user_nft的两个记录进行所属变更
            UserNFTDto userNFTDto = new UserNFTDto(
                    null,dto.getToUser(),dto.getNftId(),null,dto.getTxHash()
                    , LocalDateTime.now(),1,nftInfo.getMetadataUrl()
            );
            //将NFT数量减一
            nftInfoService.NFTInfoUpdateCount(dto.getNftId());
            return this.save(userNFTDto);
        }
        if(toUserDto == null){
            UserNFTDto userNFTDto = new UserNFTDto(
                    null,dto.getToUser(),dto.getNftId(),null,dto.getTxHash()
                    , LocalDateTime.now(),1,nftInfo.getMetadataUrl()
            );
            return this.deleteUserNFT(dto.getFromUser(),dto.getNftId()) && this.save(userNFTDto);
        }
        return false;
    }

}
