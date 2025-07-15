package com.example.service.impl.NFT;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.*;
import com.example.mapper.NFT.UserNFTMapper;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.service.NFT.UserNFTService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class UserNFTImpl extends ServiceImpl<UserNFTMapper, UserNFTDto> implements UserNFTService {

    @Resource
    NFTInfoService nftInfoService;

    @Resource
    NFTRuleService nftRuleService;

    @Resource
    UserNFTMapper userNFTMapper;

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
    public  boolean deleteUserNFT(Integer id,Integer nftid){
        QueryWrapper<UserNFTDto> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",id).eq("nft_id", nftid);
        this.userNFTMapper.delete(wrapper);
        return true;
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
        if(nftRuleDto.getValidityPeriod()!=-1){
            if(now.isAfter(nftRuleDto.getPassActive())){return false;}
        }
        //如果是自动发布的话还需要查询NFT信息内的NFT剩余数量是否足够
        if (fromUserDto == null ){
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
