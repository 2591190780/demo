package com.example.service.impl.NFT;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.NFTInfoDto;
import com.example.mapper.NFT.NFTInfoMapper;
import com.example.service.NFT.NFTInfoService;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class NFTInfoImpl extends ServiceImpl<NFTInfoMapper, NFTInfoDto> implements NFTInfoService {

    @Resource
    JwtUtils jwtUtils;

    @Resource
    InfoToRedisUtils infoToRedisUtils;

    @Override
    public boolean NFTInfoAddSingle(HttpServletRequest request,NFTInfoDto nftInfoDto){
        /**
         * 前端传入--> 名称、描述、图片、发行个数、nft稀有度，metadata元数据
         * 后端需要 使用ipfs 以及 上链获取nft地址
         */
        Integer userId =  jwtUtils.getRequesetId(request);
        if(!Objects.equals(nftInfoDto.getPublicBy(), userId)) return false;
        LocalDateTime createTime = LocalDateTime.now();
        nftInfoDto.setCreatedAt(createTime);
        nftInfoDto.setIsActive(0);
        if (this.save(nftInfoDto)){
            // 请求发送给 Redis等待管理员确认
            infoToRedisUtils.InfoToRedis(nftInfoDto.getTemplateId(),userId,"add","nft_info");
            return true;
        }
        return false;
    }
    /**
     * 查询不需要权限验证
     */
    @Override
    public NFTInfoDto NFTInfoSelectByTemplateId(Integer id){
        return this.query().eq("template_id", id).one();
    }
    @Override
    public List<NFTInfoDto> NFTInfoSelectByPublic(Integer publicBy){
        return this.query().eq("public_by", publicBy).list();
    }
    @Override
    public NFTInfoDto NFTInfoSelectByURL(String url){
        return this.query().eq("image_url", url).one();
    }

    @Override
    public List<NFTInfoDto> infoSelectByCondition(NFTInfoDto params){
        QueryWrapper<NFTInfoDto> queryWrapper = new QueryWrapper<>();

        // 精确匹配条件
        if (params.getTemplateId() != null) {
            queryWrapper.eq("template_id", params.getTemplateId());
        }
        if (params.getPublicBy() != null) {
            queryWrapper.eq("public_by", params.getPublicBy());
        }

        if (params.getIsActive() == 0 || params.getIsActive() == 1) {
            queryWrapper.eq("is_active", params.getIsActive());
        }

        // 模糊查询条件
        if (StringUtils.isNotBlank(params.getName())) {
            queryWrapper.like("name", params.getName());
        }
        if (StringUtils.isNotBlank(params.getDescription())) {
            queryWrapper.like("description", params.getDescription());
        }

        // URL精确查询
        if (StringUtils.isNotBlank(params.getImageUrl())) {
            queryWrapper.eq("img_url", params.getImageUrl());
        }
        // 等级匹配
        if (StringUtils.isNotBlank(params.getNftLevel())) {
            queryWrapper.eq("nft_level", params.getNftLevel());
        }

        // 时间范围查询
        if (params.getCreatedAt() != null) {
            queryWrapper.ge("update_time", params.getCreatedAt());
        }

        return this.list(queryWrapper);
        }

    /**
     * NFT在发行后需要上链，所以不能更改基本信息。
     */

    @Override
    public boolean NFTInfoUpdateAdmin(Integer id,byte answer){
        return  this.update().eq("template_id",id)
                    .set("is_active",answer)
                .set("update_time",LocalDateTime.now())
                .update();
    }


    @Override
    public boolean NFTInfoUpdateMulti(NFTInfoDto nftInfoDto){

        return true;
    }


    @Override
    public List<NFTInfoDto> NFTInfoSelectMulti(List<NFTInfoDto> nftInfoDtoList){
        return null;
    }
}
