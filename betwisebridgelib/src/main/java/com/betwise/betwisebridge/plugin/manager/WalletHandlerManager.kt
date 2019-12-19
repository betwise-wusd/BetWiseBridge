package com.betwise.betwisebridge.plugin.manager

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.betwise.betwisebridge.bean.DappBean
import com.betwise.betwisebridge.constant.CodeStatus
import com.betwise.betwisebridge.constant.SPConstant
import com.betwise.betwisebridge.dict.WalletError
import com.betwise.betwisebridge.plugin.WalletPlugins
import com.betwise.betwisebridge.plugin.param.PluginParams
import com.betwise.betwisebridge.utils.SPUtils
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.google.gson.Gson
import java.util.jar.Manifest

/**
 * Date: 2019-11-26
 */
class WalletHandlerManager(val mContext: Context) {

    val handlerMap = HashMap<String, WalletHandler>()
    private fun register(actionName: String){
        handlerMap[actionName] = WalletHandler(mContext, actionName)
    }

    fun registerHandlers(): Map<String, WalletHandler>{
        register(PluginParams.ACTION_CREATE_NEW_ACCOUNT)
        register(PluginParams.ACIION_NOTIFY_CHECK_MNEMONICS)
        register(PluginParams.ACTION_WALLET_GET_ACCOUNT_INFO)
        register(PluginParams.ACTION_NOTIFY_CHECK_PRIVATEKEY)
        register(PluginParams.ACTION_GET_UCOIN_TRANSFER_SIGN_HEX)
        register(PluginParams.ACTION_GET_UCOIN_CONTRACT_SIGN_HEX)
        register(PluginParams.ACTION_SAVE_REGID)
        register(PluginParams.ACTION_GET_REGID)
        register(PluginParams.ACTION_SAVE_WALLET)
        register(PluginParams.ACTION_NOTIFY_APP_REVISE_PSW)
        register(PluginParams.ACTION_GET_MNEMONICS)
        register(PluginParams.ACTION_GET_PRIVATE_KEY)
        return handlerMap
    }
}

class WalletHandler(val mContext: Context, val actionName: String): BridgeHandler{

    override fun handler(data: String, function: CallBackFunction) {
        //check permission
        if(ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            function.onCallBack(
                    Gson().toJson(
                            DappBean(
                                    CodeStatus.NO_PERMISSION,
                                    WalletError.getMsgByErrorCode(mContext, CodeStatus.NO_PERMISSION)
                            )
                    )
            )
            return
        }
        try {
            when(actionName){
                PluginParams.ACTION_CREATE_NEW_ACCOUNT -> WalletPlugins.notifyAppCreateNewAccount(data, function)
                PluginParams.ACIION_NOTIFY_CHECK_MNEMONICS -> WalletPlugins.notifyAppCheckMnemonics(data, function)
                PluginParams.ACTION_NOTIFY_CHECK_PRIVATEKEY -> WalletPlugins.notifyAppCheckPrivateKey(mContext, data, function)
                PluginParams.ACTION_GET_UCOIN_TRANSFER_SIGN_HEX -> WalletPlugins.getUCoinTransferSignHex(mContext, data, function)
                PluginParams.ACTION_GET_UCOIN_CONTRACT_SIGN_HEX -> WalletPlugins.getUCoinContractSignHex(mContext, data, function)
                PluginParams.ACTION_WALLET_GET_ACCOUNT_INFO -> WalletPlugins.getAddressInfo(mContext, data, function)
                PluginParams.ACTION_SAVE_REGID -> WalletPlugins.notifyAppSaveRegId(mContext, data, function)
                PluginParams.ACTION_GET_REGID -> WalletPlugins.getRegId(mContext, function)
                PluginParams.ACTION_SAVE_WALLET -> WalletPlugins.notifyAppSaveWallet(mContext, data, function)
                PluginParams.ACTION_NOTIFY_APP_REVISE_PSW -> WalletPlugins.notifyAppRevisePassword(mContext, data, function)
                PluginParams.ACTION_GET_MNEMONICS -> WalletPlugins.getMnemonics(mContext, data, function)
                PluginParams.ACTION_GET_PRIVATE_KEY -> WalletPlugins.getPrivateKey(mContext, data, function)
            }
        }catch (e:Exception){
            function.onCallBack(
                Gson().toJson(
                    DappBean(
                        CodeStatus.ILLEGAL_PARAMETER,
                        e.message.toString()
                    )
                )
            )
        }
    }
}

