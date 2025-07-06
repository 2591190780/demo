package com.example.utils;

public class Const {
    /**
     * NFT交易信息
     */
    public static final  String NFT_TRANSACTION = "NFT_TRANSACTION";
    /**
     * 交易订单，相关信息
     */
    public static final  String ORDER_ID_INFO = "orderIdInfo";
    public static final  String REAL_PAY_COUNT = "realPayCount";

    public static final  String APLIPAY_RETURN_URL = "http://demotestccit.natapp1.cc/alipay/return";
    public static final  String APLIPAY_NOTIFY_URL = "http://demotestccit.natapp1.cc/alipay/notify";

    /**
     * 产品  农户的相关操作  存入Redis队列等待管理员处理
     */
    public static final  String FARMER_UPDATE_APPLY_LIST = "userUpdateApplyList";
    public static final  String FARMER_ADD_APPLY_LIST="userAddApplyList";
    public static final  String FARMER_DELETE_APPLY_LIST="userDeleteList";

    public static final  String FARMER_INDEX ="userIndex";

    public static final  String PRODUCT_ID_LIST ="productIdList";
    public static final  String SENSOR_ID_LIST = "sensorIdList";
    public static final  String NFT_ID_LIST = "nftIdList";
    public static final  String USER_ID_LIST = "userIdList";
    public static final  String NFT_RULE_ID_LIST = "nftRuleIdList";

    public static final  String IMG_FOR_PRODUCT = "imgForProduct";
    public static final  String IMG_FOR_NFT = "imgForNft";
    public static final  String IMG_FOR_USER = "imgForUser";
    /**
     * JWT过滤器设置
     */
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

    /**
     * 区块链
     */
    public static  final  String BLOCK_CHAIN_WALLET="BlockchainWallet";
    public static  final  String BLOCK_CHAIN_PRIVATE_KEY="BlockchainPrivateKey";
}

