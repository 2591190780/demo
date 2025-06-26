package com.example.utils;

import jakarta.servlet.http.PushBuilder;

public class Const {

    /**
     * 产品  农户的相关操作  存入Redis队列等待管理员处理
     */
    public static final  String FARMER_UPDATE_APPLY_LIST = "FARMER_UPDATE_APPLY_LIST";
    public static final  String FARMER_ADD_APPLY_LIST="farmerAddApplyList";
    public static final  String FARMER_DELETE_APPLY_LIST="farmerDeleteList";

    public static final  String FARMER_INDEX ="FarmerIndex";

    public static final  String PRODUCT_ID_LIST ="productIdList";
    public static final  String SENSOR_ID_LIST = "sensorIdList";
    public static final  String NFT_ID_LIST = "nftIdList";

    //经常使用的属性
    public static  final  String JWT_BLACK_LIST="jwt_blacklist:";

    public static  final  int ORDER_CORS= -110 ;
    public static  final  int ORDER_LIMIT= -101 ;

    public static  final  String FLOW_LIMIT_COUNT="flow_limit_count:";
    public static  final  String FLOW_LIMIT_BLOCK="flow_limit_block:";

    public static final String VERIFY_EMAIL_LIMIT="verify:email:limit:";
    public static final String VERIFY_EMAIL_DATA="verify:email:data";

    //用户角色属性
    public static  final  String ROLE_OF_USER_MERCHANT="Merchant"; //商家  1
    public static  final  String ROLE_OF_USER_ORDINARY="Ordinary";  //普通用户  2
    public static  final  String ROLE_OF_USER_ADMINISTRATOR="Administrator"; //管理员  3

}

