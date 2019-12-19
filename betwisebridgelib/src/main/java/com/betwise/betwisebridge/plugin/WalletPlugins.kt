package com.betwise.betwisebridge.plugin

import android.content.Context
import com.betwise.betwisebridge.bean.*
import com.betwise.betwisebridge.callback.CommonCallBack
import com.betwise.betwisebridge.constant.CodeStatus
import com.betwise.betwisebridge.constant.SPConstant
import com.betwise.betwisebridge.dict.WalletError
import com.betwise.betwisebridge.utils.JSONUtils
import com.betwise.betwisebridge.utils.SPUtils
import com.betwise.betwisebridge.wallet.WalletManager
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.google.gson.Gson
import java.util.ArrayList

/**
 * desc: Plugins content for wallet.
 * Date: 2019-11-27
 */
class WalletPlugins {

    companion object{
        // Create a new wallet and return
        fun notifyAppCreateNewAccount(args: String, function: CallBackFunction){
            val bean = Gson().fromJson<CreateAccountBean>(args, CreateAccountBean::class.java)
            val password = bean.password
            if (password.isNullOrEmpty()) {
                function.onCallBack(Gson().toJson(BaseBean(CodeStatus.IS_COMPLETE_FAIL,"Password is null or empty")))//JSONUtils.buildResult("isComplete", CodeStatus.IS_COMPLETE_FAIL))
                return
            }
            WalletManager.instance.createWallet(object : CommonCallBack<CreateOrImportWalletBean> {
                override fun onSuccess(bean: CreateOrImportWalletBean) {
                    val resultSucc = Gson().toJson(bean)
                    function.onCallBack(resultSucc)
                }

                override fun onFailure(errBean: CreateOrImportWalletBean) {
                    val failStr = Gson().toJson(errBean)
                    function.onCallBack(failStr)
                }
            })
        }

        // Import wallet by mnemonics
        fun notifyAppCheckMnemonics(args: String, function: CallBackFunction){
            val bean = Gson().fromJson<CreateAccountBean>(args, CreateAccountBean::class.java)
            val password = bean?.password
            val helpStr = bean?.helpStr

            if (password.isNullOrEmpty() || helpStr.isNullOrEmpty()) {
                function.onCallBack(JSONUtils.buildResult("isComplete", CodeStatus.IS_COMPLETE_FAIL))
                return
            }
            WalletManager.instance.importWallet(helpStr!!,
                object : CommonCallBack<CreateOrImportWalletBean> {
                    override fun onSuccess(bean: CreateOrImportWalletBean) {
                        val walletSuccGson = Gson()
                        val resultSucc = walletSuccGson.toJson(bean)
                        function.onCallBack(resultSucc)
                    }

                    override fun onFailure(errBean: CreateOrImportWalletBean) {
                        val walletSuccGson = Gson()
                        val resultFail = walletSuccGson.toJson(bean)
                        function.onCallBack(resultFail)
                    }
                })
        }

        // Import wallet by private key
        fun notifyAppCheckPrivateKey(mContext: Context, args: String, function: CallBackFunction){
            val bean = Gson().fromJson<PrivateKeyBean>(args, PrivateKeyBean::class.java!!)

            val password = bean?.password
            val privatekey = bean?.privateKey

            if (password.isNullOrEmpty() || privatekey.isNullOrEmpty()) {
                function.onCallBack(JSONUtils.buildResult("isComplete", CodeStatus.IS_COMPLETE_FAIL))
                return
            }
            WalletManager.instance.notifyAppCheckPrivateKey(mContext, password!!, privatekey!!,
                object : CommonCallBack<ImportPrivateKeyBean> {
                    override fun onSuccess(bean: ImportPrivateKeyBean) {
                        function.onCallBack(Gson().toJson(bean))
                    }

                    override fun onFailure(errBean: ImportPrivateKeyBean) {
                        function.onCallBack(Gson().toJson(errBean))
                    }
                })

        }

        // Get wallet infomation
        fun getAddressInfo(mContext: Context, args: String, function: CallBackFunction){
            if(!checkAddressExist(mContext)){
                function.onCallBack(
                        Gson().toJson(
                                DappBean(
                                        CodeStatus.WALLET_NOT_EXIT,
                                        WalletError.getMsgByErrorCode(mContext, CodeStatus.WALLET_NOT_EXIT)
                                )
                        )
                )
                return
            }

            val address = SPUtils.get(mContext, SPConstant.WALLET_ADDRESS, "") as String
            val addressInfoBean = AddressInfoBean()
            addressInfoBean.address = address
            addressInfoBean.supplier = "BetWise"
            function.onCallBack(
                Gson().toJson(
                    DappBean(
                        CodeStatus.SUCCESS,
                        WalletError.getMsgByErrorCode(mContext, CodeStatus.SUCCESS),
                        addressInfoBean
                    )
                )
            )
        }

        // Transfer by Multi-currency
        fun getUCoinTransferSignHex(mContext: Context, args: String, function: CallBackFunction) {
            if(!checkAddressExist(mContext)){
                function.onCallBack(
                        Gson().toJson(
                                DappBean(
                                        CodeStatus.WALLET_NOT_EXIT,
                                        WalletError.getMsgByErrorCode(mContext, CodeStatus.WALLET_NOT_EXIT)
                                )
                        )
                )
                return
            }
            val bean = Gson().fromJson<WalletUCoinTransferSignHexBean>(args, WalletUCoinTransferSignHexBean::class.java)
            val password = bean?.password
            val validHeight = bean?.height
            val fee = bean?.fee
            val regId = bean?.regId
            val feeSymbol = bean?.feeSymbol
            val memo = bean?.memo

            if (password.isNullOrEmpty() || fee.isNullOrEmpty() || feeSymbol.isNullOrEmpty()
                || validHeight == null) {
                function.onCallBack(Gson().toJson(SignHexBean(CodeStatus.IS_COMPLETE_FAIL, "","")))
                return
            }
            val uCoinTransferBean = UCoinTransferBean()
            uCoinTransferBean.destArr = ArrayList<UCoinTransferBean.DestArrBean>()
            uCoinTransferBean.memo = memo
            for (i in 0 until bean.destArr?.size!!) {
                val destArrBean = UCoinTransferBean.DestArrBean()
                destArrBean.amount = bean.destArr?.get(i)?.amount
                destArrBean.coinSymbol = bean.destArr?.get(i)?.coinSymbol
                destArrBean.destAddr = bean.destArr?.get(i)?.destAddr
                uCoinTransferBean?.destArr?.add(destArrBean)
            }

            if (uCoinTransferBean.destArr?.size!! < 1) {
                function.onCallBack(Gson().toJson(SignHexBean(CodeStatus.IS_COMPLETE_FAIL, "","")))
                return
            }
            WalletManager.instance.ucoinTransfer(mContext, password, fee, validHeight, regId, feeSymbol, uCoinTransferBean,
                    object : CommonCallBack<SignHexBean> {
                        override fun onSuccess(bean: SignHexBean) {
                            function.onCallBack(Gson().toJson(bean))
                        }

                        override fun onFailure(err: SignHexBean) {
                            function.onCallBack(Gson().toJson(err))
                        }
                    })
        }

        // Call the contract by Multi-currency
        fun getUCoinContractSignHex(mContext: Context, args: String, function: CallBackFunction) {
            if(!checkAddressExist(mContext)){
                function.onCallBack(
                        Gson().toJson(
                                DappBean(
                                        CodeStatus.WALLET_NOT_EXIT,
                                        WalletError.getMsgByErrorCode(mContext, CodeStatus.WALLET_NOT_EXIT)
                                )
                        )
                )
                return
            }
            val bean = Gson().fromJson<WalletUCoinContractBean>(args, WalletUCoinContractBean::class.java)
            val password = bean?.password
            val validHeight = bean?.height
            val regId = bean?.regId
            val userid = bean?.userId
            val contract = bean?.contract
            val value = bean?.amount
            val fees = bean?.fee
            val feeSymbol = bean?.feeSymbol
            val coinSymbol = bean?.coinSymbol
            val memo = bean?.memo

            if (password.isNullOrEmpty() ||
                validHeight == null ||
                fees.isNullOrEmpty() ||
                regId.isNullOrEmpty() ||
                contract.isNullOrEmpty() ||
                feeSymbol.isNullOrEmpty()
            ) {
                function.onCallBack(Gson().toJson(BaseBean(CodeStatus.IS_COMPLETE_FAIL,"")))
                return
            }
            val coinContractBean = UCoinContractBean()
            coinContractBean.amount=value
            coinContractBean.coinSymbol=coinSymbol
            coinContractBean.amount=value
            coinContractBean.memo=memo
            coinContractBean.contract=contract

            WalletManager.instance.ucoinContractInvoke(mContext, password,
                validHeight,
                userid,
                fees,
                feeSymbol,
                coinContractBean,
                object : CommonCallBack<SignHexBean>{
                    override fun onSuccess(bean: SignHexBean) {
                        function.onCallBack(Gson().toJson(bean))
                    }
                    override fun onFailure(err: SignHexBean) {
                        function.onCallBack(Gson().toJson(err))
                    }
                })
        }

        // Save wallet reg id, We need reg id when transfer and invoke contract.
        fun notifyAppSaveRegId(mContext: Context, args: String, function: CallBackFunction) {
            val gson = Gson()
            val bean = gson.fromJson<RegIdBean>(args, RegIdBean::class.java)
            val regId = bean.regId
            if (regId.isNullOrEmpty()) {
                function.onCallBack(JSONUtils.buildResult("isComplete", CodeStatus.IS_COMPLETE_FAIL))
                return
            }
            SPUtils.put(mContext, SPConstant.REGID, regId)
            function.onCallBack(JSONUtils.buildResult("isComplete", CodeStatus.IS_COMPLETE_SUCC))
        }

        // Get wallet regid
        fun getRegId(mContext: Context, function: CallBackFunction) {
            val regId = SPUtils.get(mContext, SPConstant.REGID, "") as String
            function.onCallBack(JSONUtils.buildResult("regId", regId))
        }

        // Save wallet info
        fun notifyAppSaveWallet(mContext: Context, args: String, function: CallBackFunction){
            val bean = Gson().fromJson<CreateAccountBean>(args, CreateAccountBean::class.java)
            if (bean == null) {
                function.onCallBack(JSONUtils.buildResult("isComplete", CodeStatus.IS_COMPLETE_FAIL))
                return
            }

            val isSuccess = WalletManager.instance.saveWallet(mContext, bean.helpStr, bean.password)
            if (isSuccess) {
                function.onCallBack(JSONUtils.buildResult("isComplete", CodeStatus.IS_COMPLETE_SUCC))
            } else {
                function.onCallBack(JSONUtils.buildResult("isComplete", CodeStatus.IS_COMPLETE_FAIL))
            }
        }

        // Revise wallet password
        fun notifyAppRevisePassword(mContext: Context, args: String, function: CallBackFunction){
            if(!checkAddressExist(mContext)){
                function.onCallBack(
                        Gson().toJson(
                                DappBean(
                                        CodeStatus.WALLET_NOT_EXIT,
                                        WalletError.getMsgByErrorCode(mContext, CodeStatus.WALLET_NOT_EXIT)
                                )
                        )
                )
                return
            }
            val bean = Gson().fromJson<ChangePasswordBean>(args, ChangePasswordBean::class.java)
            val newPassword = bean?.newPassword
            val oldPassword = bean?.oldPassword
            if (newPassword.isNullOrEmpty() || oldPassword.isNullOrEmpty()) {
                function.onCallBack(Gson().toJson(BaseBean(CodeStatus.IS_COMPLETE_FAIL, "Password Error")))//JSONUtils.buildResult("isComplete", CodeStatus.IS_COMPLETE_FAIL))
                return
            }
            WalletManager.instance.changePassword(mContext, oldPassword, newPassword, object : CommonCallBack<BaseBean> {
                override fun onSuccess(bean: BaseBean) {
                    function.onCallBack(Gson().toJson(bean))
                }

                override fun onFailure(err: BaseBean) {
                    function.onCallBack(Gson().toJson(err))
                }
            })

        }

        // Get wallet mnemonics
        fun getMnemonics(mContext: Context, args: String, function: CallBackFunction){
            if(!checkAddressExist(mContext)){
                function.onCallBack(
                        Gson().toJson(
                                DappBean(
                                        CodeStatus.WALLET_NOT_EXIT,
                                        WalletError.getMsgByErrorCode(mContext, CodeStatus.WALLET_NOT_EXIT)
                                )
                        )
                )
                return
            }
            val bean = Gson().fromJson<PasswordBean>(args, PasswordBean::class.java)
            val psw = bean.password
            if (psw.isNullOrEmpty()) {
                function.onCallBack(JSONUtils.buildResult("error", "Invalid parameter"))
                return
            }
            WalletManager.instance.getMnemonics(mContext, psw, object : CommonCallBack<GetMnemonicsBean> {
                override fun onSuccess(t: GetMnemonicsBean) {
                    function.onCallBack(Gson().toJson(t))
                }

                override fun onFailure(t: GetMnemonicsBean) {
                    function.onCallBack(Gson().toJson(t))
                }

            })
        }

        //Get wallet private key
        fun getPrivateKey(mContext: Context, args: String, function: CallBackFunction){
            if(!checkAddressExist(mContext)){
                function.onCallBack(
                        Gson().toJson(
                                DappBean(
                                        CodeStatus.WALLET_NOT_EXIT,
                                        WalletError.getMsgByErrorCode(mContext, CodeStatus.WALLET_NOT_EXIT)
                                )
                        )
                )
                return
            }
            val bean = Gson().fromJson<PasswordBean>(args, PasswordBean::class.java)
            val password = bean?.password
            if (password.isNullOrEmpty()) {
                function.onCallBack(JSONUtils.buildResult("error", "Invalid parameter"))
                return
            }

            WalletManager.instance.getPrivateKey(mContext, password, object : CommonCallBack<GetPrivateKeyBean>{
                override fun onSuccess(t: GetPrivateKeyBean) {
                    function.onCallBack(Gson().toJson(t))
                }

                override fun onFailure(t: GetPrivateKeyBean) {
                    function.onCallBack(Gson().toJson(t))
                }

            })
        }

        private fun checkAddressExist(mContext: Context): Boolean{
            val address = SPUtils.get(mContext, SPConstant.WALLET_ADDRESS, "") as String
            return address.isNotEmpty()
        }
    }
}