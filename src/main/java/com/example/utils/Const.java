package com.example.utils;

import jakarta.servlet.http.PushBuilder;

public class Const {
    //经常使用的属性
    public static  final  String JWT_BLACK_LIST="jwt_blacklist:";

    public static  final  int ORDER_CORS= -102 ;

    public static final String VERIFY_EMAIL_LIMIT="verify:email:limit:";
    public static final String VERIFY_EMAIL_DATA="verify:email:data";

    //用户角色属性
    public static  final  String ROLE_OF_USER_MERCHANT="Merchant"; //商家  1
    public static  final  String ROLE_OF_USER_ORDINARY="Ordinary";  //普通用户  2
    public static  final  String ROLE_OF_USER_ADMINISTRATOR="Administrator"; //管理员  3

}

