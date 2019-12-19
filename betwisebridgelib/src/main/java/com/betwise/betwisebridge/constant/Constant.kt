package com.betwise.betwisebridge.constant

object SPConstant {
    const val WALLET_WORDS = "WALLET_WORDS"
    const val WALLET_PRIVATE_KEY = "WALLET_PRIVATE_KEY"
    const val WALLET_ADDRESS = "WALLET_ADDRESS"
    const val UUID = "UUID"
    const val REGID = "REGID"
    const val WALLET_MHASH = "MHASH"
    const val SHOWGUIDE = "SHOWGUIDE"
    const val LANGUAGE = "LANGUAGE"
    const val SHOWTIP = "SHOWTIP"
}

object CodeStatus {
    /**
     * 调用的结果，业务相关
     */
    //插件结果返回失败
    val IS_COMPLETE_FAIL = "0"
    //插件结果返回成功
    val IS_COMPLETE_SUCC = "1"
    //密码错误
    val IS_COMPLETE_PSW_ERROR = "4010"
    //接收地址错误
    val Incorrect_dest_account = "4030"
    //参数错误
    val Incorrect_parameter = "4020"
    //签名出错
    val Signature_exception = "4040"

    /**
     * 插件相关，不关注结果，所有插件共用
     */
    //插件调用成功
    val SUCCESS = 0
    //非法参数
    val ILLEGAL_PARAMETER = 5000
    //取消
    val CANCEL = 7000
    //失败
    val FAIL = 102
    // 钱包不存在
    val WALLET_NOT_EXIT = 2000
    //没有权限
    val NO_PERMISSION = 3000
}