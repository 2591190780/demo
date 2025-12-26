package com.example.mapper.transaction;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.TransactionAccountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

@Mapper
public interface TransactionProcessMapper extends BaseMapper<TransactionAccountDto>{
    // 新增分页查询方法，不影响原有方法
    @Select("SELECT * FROM order_info WHERE seller_id = #{sellerId}")
    Page<TransactionAccountDto> selectBySellerIdPage(
            Page<TransactionAccountDto> page, @Param("sellerId") Integer sellerId);

    @Select("SELECT COUNT(*) FROM order_info WHERE buyer_id = #{buyerId} AND status!=1 AND status!=6  ")
    Integer countNumTransactionByBuyerIdStatus( Integer buyerId);

    @Select("SELECT SUM(actual_payment) FROM order_info WHERE buyer_id = #{buyerId} AND status!=1 AND status!=6 ")
    Integer sumTotalMoneyByBuyerIdStatus( Integer buyerId);


}
