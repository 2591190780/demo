package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.response.ProductVO;
import com.example.mapper.ProductInfoSelectAccountMapper;
import com.example.service.ProductInfoSelectAccountService;

import org.springframework.stereotype.Service;

import java.util.Objects;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProductInfoSelectAccountImpl extends ServiceImpl<ProductInfoSelectAccountMapper, ProductInfoAccountDto>
        implements ProductInfoSelectAccountService {
    /**
     *     根据产品id,farmeid,name/category/location查询，并返回前端的vo类进行数据显示
     */
    @Override
    public ProductVO getProductInfoAccountByProductId(Integer Id)  {  //根据产品id的精准查询
        //查询信息
        ProductInfoAccountDto productInfoAccountDto = this.findProductInfoAccountByProductId(Id);
        return this.convertToProductVO(productInfoAccountDto);
    }

    @Override
    public List<ProductVO> getProductInfoAccountByFarmerID(Integer Id) {  //根据商家id的精准查询
        List<ProductInfoAccountDto> productInfoAccountDto = this.findProductInfoAccountByFarmerID(Id);
        return this.convertToProductVOList(productInfoAccountDto);
    }

    @Override
    public List<ProductVO> getProductInfoAccountByName(String text){  //字符模糊查询
        List<ProductInfoAccountDto> productInfoAccountDto = this.findProductInfoAccountByName(text);
        return this.convertToProductVOList(productInfoAccountDto);
    }

    /**
     * 多参数模糊查询
     * @param id
     * @param fid
     * @param name
     * @param category
     * @param location
     * @return
     */
    @Override
    public List<ProductVO> selectProductAccByText(Integer id,Integer fid,String name,String category,String location){
        // 检查所有参数是否都为空
        if (id == null && fid == null &&
                name == null && category == null && location == null) {
            return Collections.emptyList();
        }
        // 创建查询条件
        QueryWrapper<ProductInfoAccountDto> queryWrapper = new QueryWrapper<>();
        // 添加精确查询条件
        if (id != null) {
            queryWrapper.eq("product_id", id); // 假设数据库字段是product_id
        }

        if (fid != null) {
            queryWrapper.eq("farmer_id", fid); // 假设数据库字段是farmer_id
        }
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.eq("name", name.trim()); // 精确匹配产品名称
        }

        if (category != null && !category.trim().isEmpty()) {
            queryWrapper.eq("category", category.trim()); // 精确匹配产品分类
        }

        if (location != null && !location.trim().isEmpty()) {
            queryWrapper.like("origin_location", location.trim()); // 模糊匹配产地
            // 如果需要精确匹配产地，改为：.eq("origin_location", location.trim())
        }
        // 执行查询
        return convertToProductVOList(list(queryWrapper));
    }
    /**
     * 根据农民ID查询产品信息账户（可能多个或者单个）
     */
    private ProductInfoAccountDto findProductInfoAccountByProductId(Integer Id) {
        if(Id==null)return null;
        return query()
                .eq("product_id", Id)
                .one();
    }

    private List<ProductInfoAccountDto> findProductInfoAccountByFarmerID(Integer farmerID) {
        if(farmerID==null)return Collections.emptyList();
        return query()
                .eq("farmer_id", farmerID)
                .list();
    }

    /**
     * 根据字符串查询
     */
    private List<ProductInfoAccountDto> findProductInfoAccountByName(String text) {
        if(text==null)return Collections.emptyList();
        return query()
                .like("name", text).or()
                .like("category", text).or()
                .like("origin_location", text)
                .list();
    }

    /**
     * 实体类转换器
     */
    private List<ProductVO> convertToProductVOList(List<ProductInfoAccountDto> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }

        return accounts.stream()
                .map(this::convertToProductVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ProductVO convertToProductVO(ProductInfoAccountDto account) {
        if (account == null) return null;
        ProductVO vo = new ProductVO();
        // 手动设置字段（避免使用反射工具，更安全）
        vo.setProductId(account.getProductId());
        vo.setFarmerId(account.getFarmerId());
        vo.setName(account.getName());
        vo.setCategory(account.getCategory());
        vo.setPrice(account.getPrice());
        vo.setStock(account.getStock());
        vo.setOriginLocation(account.getOriginLocation());
        vo.setCertificationHash(account.getCertificationHash());
        vo.setCreateTime(account.getCreateTime() );
        vo.setUpdateTime(account.getUpdateTime());
        return vo;
    }
}
