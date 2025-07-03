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


}
