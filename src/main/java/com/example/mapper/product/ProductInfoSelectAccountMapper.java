package com.example.mapper.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.NFTTransactionDto;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.response.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

@Mapper
public interface ProductInfoSelectAccountMapper extends BaseMapper<ProductInfoAccountDto> {

    @Select("""
            SELECT *
            FROM product_info
            WHERE farmer_id = #{id}
            ORDER BY 
              CASE WHEN is_active = 1 THEN 0 ELSE 1 END,  /* 激活状态的排前面 */
              create_time DESC  /* 时间从新到旧 */
            """)
    Page<ProductVO> selectBySellerIdPage(
            Page< ProductVO > page, Integer id);


}
