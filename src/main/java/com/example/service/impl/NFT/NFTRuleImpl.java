package com.example.service.impl.NFT;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.NFTInfoDto;
import com.example.entity.dto.NFTRuleDto;
import com.example.mapper.NFT.NFTRuleMapper;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class NFTRuleImpl extends ServiceImpl<NFTRuleMapper, NFTRuleDto> implements NFTRuleService {

    @Override
    public NFTRuleDto nftRuleSelectByID (Integer id){
        return this.query().eq("rule_id", id).one();
    }
    @Override
    public List<NFTRuleDto> nftRuleSelectByName (String name){
        return this.query().like("name", name).list();
    }
    @Override
    public List<NFTRuleDto> nftRuleSelectByTemplateID (Integer id){
        return this.query().eq("template_id", id).list();
    }
    @Override
    public NFTRuleDto nftRuleSelectByHash(String hash){
        return this.query().eq("apply_hash", hash).one();
    }

    @Override
    public  List<NFTRuleDto> nftRuleSelectCondition(NFTRuleDto params){
        QueryWrapper<NFTRuleDto> queryWrapper = new QueryWrapper<>();
        // 精确匹配条件
        if (params.getRuleId() != null) {
            queryWrapper.eq("rule_id", params.getTemplateId());
        }
        if (params.getTemplateId() != null) {
            queryWrapper.eq("template_id", params.getTemplateId());
        }
        if (params.getApplyHash() != null) {
            queryWrapper.eq("apply_hash", params.getApplyHash());
        }

        if (params.getIsActive() == 0 || params.getIsActive() == 1) {
            queryWrapper.eq("is_active", params.getIsActive());
        }
        // 模糊查询条件
        if (StringUtils.isNotBlank(params.getName())) {
            queryWrapper.like("name", params.getName());
        }

        return this.list(queryWrapper);
    }

    /**
     * 同样的，rule也需要上链，不能修改。
     */
    @Override
    public  boolean nftRuleUpdateAdmin(Integer id,byte answer){
        return  this.update().eq("rule_id",id)
                .set("is_active",answer)
                .set("update_time",LocalDateTime.now())
                .update();
    }

    private String tableChoose(int type){
        return switch (type){
            case 1 -> "order_info";
            case 2 -> "product_info";
            case 3 -> "sensor_info";
            case 4 -> "sensor_datainfo";
            case 5 -> "delivery_info";
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

}
