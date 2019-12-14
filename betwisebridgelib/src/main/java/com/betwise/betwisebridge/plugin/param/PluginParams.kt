package com.betwise.betwisebridge.plugin.param

/**
 *
 * desc:
 *
 * Date: 2019-11-26
 */
object PluginParams {

    //创建钱包
    const val ACTION_CREATE_NEW_ACCOUNT = "notifyAppCreateNewAccount"
    //根据助记词导入钱包
    const val ACIION_NOTIFY_CHECK_MNEMONICS = "notifyAppCheckMnemonics"
    //获取钱包的详细信息
    const val ACTION_WALLET_GET_ACCOUNT_INFO = "getAddressInfo"
    //根据私钥导入钱包
    const val ACTION_NOTIFY_CHECK_PRIVATEKEY = "notifyAppCheckPrivateKey"
    //多币种转账
    const val ACTION_GET_UCOIN_TRANSFER_SIGN_HEX = "getUCoinTransferSignHex"
    //多币种合约调用
    const val ACTION_GET_UCOIN_CONTRACT_SIGN_HEX = "getUCoinContractSignHex"
    //保存钱包RegID
    const val ACTION_SAVE_REGID = "notifyAppSaveRegId"
    //获取钱包RegID
    const val ACTION_GET_REGID = "getRegId"
    //保存钱包信息
    const val ACTION_SAVE_WALLET = "notifyAppSaveWallet"
    //修改钱包密码
    const val ACTION_NOTIFY_APP_REVISE_PSW = "notifyAppRevisePassword"
    //导出助记词
    const val ACTION_GET_MNEMONICS = "getMnemonics"
    //导出私钥
    const val ACTION_GET_PRIVATE_KEY = "getPrivateKey"

}