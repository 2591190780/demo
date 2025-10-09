package com.example.utils;

public class Const {


    /**
     * 一些常用的智能合约地址
     */
    public static final  String CONTRACT_FOR_NFT_RULE_METHOD_GETRULE = "GETRule";
    public static final  String CONTRACT_FOR_NFT_RULE_METHOD_DELETERULEGROUP = "deleteRulesByTokenId";
    public static final  String CONTRACT_FOR_NFT_RULE_METHOD_GETRULEBYTOKENID = "getRulesByTokenIdFormatted";
    public static final  String CONTRACT_FOR_NFT_RULE_METHOD_ADDORUODATERULE = "addOrUpdateRule";
    public static final  String CONTRACT_FOR_NFT_RULE = "0x59875722fa83b3b8b79c186dd1fdf5b07d452f3e";
    public static final  String CONTRACT_FOR_NFT_INFO_METHOD_STORENFTINFO = "storeNFTInfo";
    public static final  String CONTRACT_FOR_NFT_INFO_METHOD_TRADENFT = "tradeNFT";
    public static final  String CONTRACT_FOR_NFT_INFO = "0xee0a61b0d8ebcabfd0037406b82b93f6ac66f2f8";
    public static final  String CONTRACT_FOR_MESSAGE_REPORT_METHOD_GETFULLEVIDENCEBYHASH = "getFullEvidenceByHash";
    public static final  String CONTRACT_FOR_MESSAGE_REPORT_METHOD_ADD_EVIDENCE = "addEvidence";
    public static final  String CONTRACT_FOR_MESSAGE_REPORT = "0x8331bc3d7d298ccb5c088f047344cab0a7ff9d7c";
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
    public static final  String APLIPAY_RETURN_HTML_URL_SUCCESS ="redirect:http://demotestccit.natapp1.cc/demo/payment-success.html";
    public static final  String APLIPAY_RETURN_HTML_URL_FAIL ="redirect:http://demotestccit.natapp1.cc/demo/payment-failed.html";
    public static final  String APLIPAY_RETURN_HTML_URL_EXCEPTION ="redirect:http://demotestccit.natapp1.cc/demo/payment-error.html";

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

