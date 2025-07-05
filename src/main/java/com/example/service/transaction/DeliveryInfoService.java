package com.example.service.transaction;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.DeliveryInfoDto;
import jakarta.servlet.http.HttpServletRequest;

public interface DeliveryInfoService extends IService<DeliveryInfoDto> {

    boolean addDeliveryInfo(HttpServletRequest request , DeliveryInfoDto deliveryInfoDto);
    DeliveryInfoDto selectDeliveryInfoByAlipayOrder(String alipayOrderId);
    boolean updateDeliveryInfo(HttpServletRequest request, String alipayOrderId,JSONObject jsonObject);

}
