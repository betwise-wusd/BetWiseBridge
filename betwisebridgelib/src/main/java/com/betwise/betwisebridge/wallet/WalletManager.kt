package com.betwise.betwisebridge.wallet

import android.content.Context
import com.betwise.betwisebridge.bean.*
import com.betwise.betwisebridge.callback.CommonCallBack
import com.betwise.betwisebridge.constant.CodeStatus
import com.betwise.betwisebridge.constant.SPConstant
import com.betwise.betwisebridge.utils.*
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.google.gson.Gson
import wiccwallet.*
import java.util.*


/**
 *
 * desc: Wallet manager class, All plugin`s function is written in this class,
 * You should init netType before call the inside method, for example：
 *
 *  //WalletManager.instance.initNetType(1)
 *  //WalletManager.instance.createWallet(...)
 *
 * Date: 2019-11-27
 */
class WalletManager {

    //1 - mainNet | 2 - testNet
    private var netType: Int? = null

    fun initNetType(netType: Int){
        this.netType = netType
    }

    companion object {
        val instance: WalletManager by lazy { Holder.INSTANCE }
    }

    private object Holder{
        val INSTANCE = WalletManager()
    }

    private fun checkNetType(){
        if(netType == null){
            throw Exception("Wallet has not been initialized")
        }
    }

    //Create a new wallet, and return the wallet`s address and mnemonic
    fun createWallet(callback: CommonCallBack<CreateOrImportWalletBean>) {
        checkNetType()
        //Create mnemonic for wallet
        val mnemonic = Wiccwallet.generateMnemonics()
        try {
            val address = Wiccwallet.getAddressFromMnemonic(mnemonic,
                netType!!.toLong()
            )
            val mHash = SHAUtil.sha512Encrypt(mnemonic)
            val bean = CreateOrImportWalletBean(
                CodeStatus.IS_COMPLETE_SUCC,
                CodeStatus.IS_COMPLETE_SUCC,
                mnemonic,
                address,
                mHash,
                ""
            )
            callback.onSuccess(bean)
        } catch (e: Exception) {
            callback.onFailure(
                CreateOrImportWalletBean(
                    CodeStatus.IS_COMPLETE_FAIL,
                    CodeStatus.IS_COMPLETE_FAIL,
                    null,
                    null,
                    null,
                    e.message.toString()
                )
            )
        }
    }

    // Import wallet by mnemonic.
    fun importWallet(mnemonic: String, callback: CommonCallBack<CreateOrImportWalletBean>) {
        checkNetType()
        try {
            val address = Wiccwallet.getAddressFromMnemonic(mnemonic,
                netType!!.toLong()
            )

            if (address.isNullOrBlank()){
                callback.onFailure(
                    CreateOrImportWalletBean(
                        CodeStatus.IS_COMPLETE_FAIL,
                        CodeStatus.IS_COMPLETE_FAIL,
                        null,
                        null,
                        null,
                        "Invalid mnemonic"
                    )
                )
                return
            }

            val mHash = SHAUtil.sha512Encrypt(mnemonic)
            val bean = CreateOrImportWalletBean(
                CodeStatus.IS_COMPLETE_SUCC,
                CodeStatus.IS_COMPLETE_SUCC,
                mnemonic,
                address,
                mHash,
                ""
            )
            callback.onSuccess(bean)
        } catch (e: Exception) {
            callback.onFailure(
                CreateOrImportWalletBean(
                    CodeStatus.IS_COMPLETE_FAIL,
                    CodeStatus.IS_COMPLETE_FAIL,
                    null,
                    null,
                    null,
                    e.message.toString()
                )
            )
        }
    }

    fun notifyAppCheckPrivateKey(mContext: Context, psw: String, privateKey: String, callback: CommonCallBack<ImportPrivateKeyBean>) {
        checkNetType()
        try {
            val success = Wiccwallet.checkPrivateKey(privateKey,
                netType!!.toLong()
            )
            if (success) {
                val address = Wiccwallet.getAddressFromPrivateKey(privateKey,
                    netType!!.toLong()
                )
                val mHash = SHAUtil.sha512Encrypt(privateKey)
                val bean = ImportPrivateKeyBean(CodeStatus.IS_COMPLETE_SUCC, privateKey, address, mHash, "")
                val saveSucc = savePrivateKey(mContext, bean, psw)
                if(saveSucc){
                    callback.onSuccess(bean)
                }else{
                    val bean = ImportPrivateKeyBean(CodeStatus.IS_COMPLETE_FAIL, "", "", "", "Invalid Params")
                    callback.onFailure(bean)
                }

            } else {
                val bean = ImportPrivateKeyBean(CodeStatus.IS_COMPLETE_FAIL, "", "", "", "Invalid PrivateKey")
                callback.onFailure(bean)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val bean = ImportPrivateKeyBean(CodeStatus.IS_COMPLETE_FAIL, "", "", "", e.message.toString())
            callback.onFailure(bean)
        }
    }

    // Transfer by Multi-currency
    fun ucoinTransfer(mContext: Context, pwd: String, fee: String, height: Int, regid: String?, feeSymbol: String, tbean: UCoinTransferBean, walletCallback: CommonCallBack<SignHexBean>){
        checkNetType()
        if (!checkPassword(mContext, pwd)) {
            walletCallback.onFailure(SignHexBean( CodeStatus.IS_COMPLETE_PSW_ERROR,"Password Error", ""))
            return
        }
        try {
            val arr = DestArr()
            for (i in 0 until tbean.destArr?.size!!) {
                val rightAddr = Wiccwallet.checkWalletAddress(tbean.destArr?.get(i)?.destAddr,
                    netType!!.toLong()
                )
                if (!rightAddr) {
                    walletCallback.onFailure(
                        SignHexBean(
                            CodeStatus.IS_COMPLETE_FAIL,
                            CodeStatus.Incorrect_dest_account,
                            ""
                        )
                    )
                    return
                }

                val value = tbean.destArr?.get(i)?.amount?.toLong()
                val symbol = tbean.destArr?.get(i)?.coinSymbol
                val destarr = tbean.destArr?.get(i)?.destAddr
                val dest = Dest()
                dest.coinSymbol = symbol
                dest.destAddr = destarr
                dest.coinAmount = value!!
                arr.add(dest)
            }
            val privateKey = getPrivateKey(mContext, pwd)
            val fees = fee.toLong()
            val param = UCoinTransferTxParam()
            param.dests = arr
            param.srcRegId = regid
            param.validHeight = height.toLong()
            param.feeSymbol = feeSymbol
            param.memo = tbean.memo
            param.fees = genWorkerFee(fees)
            try {
                val pubkey = Wiccwallet.getPubKeyFromPrivateKey(privateKey)
                param.pubKey = pubkey
                val signHex = Wiccwallet.signUCoinTransferTx(privateKey, param)
                walletCallback.onSuccess(SignHexBean(CodeStatus.IS_COMPLETE_SUCC, "", signHex))
            } catch (e: Exception) {
                walletCallback.onFailure(SignHexBean(CodeStatus.IS_COMPLETE_FAIL, e.message.toString(), ""))
            }

        } catch (e: Exception) {
            walletCallback.onFailure(SignHexBean(CodeStatus.IS_COMPLETE_FAIL, CodeStatus.Signature_exception, ""))
        }
    }

    // Call the contract by Multi-currency
    fun ucoinContractInvoke(mContext: Context,
                            pwd: String,
                            height: Int,
                            userid: String?,
                            fees: String,
                            feeSymbol: String,
                            bean: UCoinContractBean, walletCallback: CommonCallBack<SignHexBean>){
        checkNetType()
        if (!checkPassword(mContext, pwd)) {
            walletCallback.onFailure(SignHexBean( CodeStatus.IS_COMPLETE_PSW_ERROR,"Password Error", ""))
            return
        }
        val privateKey = getPrivateKey(mContext, pwd)
        val fee = fees.toLong()
        val amount = bean.amount?.toLong()

        val param = UCoinContractTxParam()
        param.validHeight = height.toLong()
        param.fees = genWorkerFee(fee)
        param.srcRegId = userid
        param.appId = bean.regId
        param.feeSymbol = feeSymbol
        param.coinSymbol = bean.coinSymbol
        param.contractHex = bean.contract
        param.coinAmount = amount!!

        try {
            val pubkey = Wiccwallet.getPubKeyFromPrivateKey(privateKey)
            param.pubKey = pubkey
            val signHex = Wiccwallet.signUCoinCallContractTx(privateKey, param)
            walletCallback.onSuccess(SignHexBean(CodeStatus.IS_COMPLETE_SUCC, "", signHex))
        } catch (e: Exception) {
            walletCallback.onFailure(SignHexBean(CodeStatus.Incorrect_parameter, e.message.toString(), ""))
        }
    }

    fun changePassword(mContext: Context, oldpwd: String, newPwd: String, callback: CommonCallBack<BaseBean>) {
        if (!checkPassword(mContext, oldpwd)) {
            callback.onFailure(BaseBean( CodeStatus.IS_COMPLETE_PSW_ERROR,"Password Error"))
            return
        }
        val md5_pwd = MD5Util.getMD5Str(oldpwd)
        val dwords = SPUtils.get(
                mContext,
                SPConstant.WALLET_WORDS,
                ""
        )!!.toString()
        if (!dwords.isNullOrEmpty()) {
            val words = AESUtils2.decrypt(md5_pwd, dwords)
            if (words != null) {
                val new_md5_pwd = MD5Util.getMD5Str(newPwd)
                val new_en_words = AESUtils2.encrypt(new_md5_pwd, words)
                SPUtils.put(
                        mContext,
                        SPConstant.WALLET_WORDS,
                        new_en_words
                )

                callback.onSuccess(BaseBean(CodeStatus.IS_COMPLETE_SUCC, ""))
            } else {
                callback.onFailure(BaseBean(CodeStatus.IS_COMPLETE_FAIL, "Password Error"))
            }
        } else {
            val pk = SPUtils.get(
                    mContext,
                    SPConstant.WALLET_PRIVATE_KEY,
                    ""
            )!!.toString()
            val dpk = AESUtils2.decrypt(md5_pwd, pk)
            if (dpk != null) {
                val new_md5_pwd = MD5Util.getMD5Str(newPwd)
                val new_en_pk = AESUtils2.encrypt(new_md5_pwd, dpk)
                SPUtils.put(
                        mContext,
                        SPConstant.WALLET_PRIVATE_KEY,
                        new_en_pk
                )

                callback.onSuccess(BaseBean(CodeStatus.IS_COMPLETE_SUCC, ""))
            } else {
                callback.onFailure(BaseBean(CodeStatus.IS_COMPLETE_FAIL, "Password Error"))
            }
        }

    }

    // Get wallet mnemonics
    fun getMnemonics(mContext: Context, password: String, callback: CommonCallBack<GetMnemonicsBean>){
        if (!checkPassword(mContext, password)) {
            callback.onFailure(GetMnemonicsBean(CodeStatus.IS_COMPLETE_PSW_ERROR,"Password Error", ""))
            return
        }
        val md5_pwd = MD5Util.getMD5Str(password)
        val dwords = SPUtils.get(
                mContext,
                SPConstant.WALLET_WORDS,
                ""
        )
        if(dwords == null){
            callback.onFailure(GetMnemonicsBean(CodeStatus.IS_COMPLETE_FAIL,"No Wallet", ""))
        }else{
            try {
                val words= AESUtils2.decrypt(md5_pwd, dwords as String)
                callback.onSuccess(GetMnemonicsBean(CodeStatus.IS_COMPLETE_SUCC, "", words))
            }catch (e: Exception){
                callback.onFailure(GetMnemonicsBean(CodeStatus.IS_COMPLETE_FAIL, e.message ?: "", ""))
            }
        }
    }

    // Get wallet mnemonics for call-back.
    fun getPrivateKey(mContext: Context, password: String, callback: CommonCallBack<GetPrivateKeyBean>){
        if (!checkPassword(mContext, password)) {
            callback.onFailure(GetPrivateKeyBean(CodeStatus.IS_COMPLETE_PSW_ERROR,"Password Error", ""))
            return
        }
        val privateKey = getPrivateKey(mContext, password)
        if(privateKey.isNullOrEmpty()){
            callback.onFailure(GetPrivateKeyBean(CodeStatus.IS_COMPLETE_FAIL,"No Wallet", ""))
        }else{
            callback.onSuccess(GetPrivateKeyBean(CodeStatus.IS_COMPLETE_SUCC, "", privateKey))
        }
    }

    // Get wallet private key for return.
    fun getPrivateKey(mContext: Context, psw: String): String?{
        val helpStr = SPUtils.get(
                mContext,
                SPConstant.WALLET_WORDS,
                ""
        ) ?: return null
        var privateKey: String? = null
        try {
            if (!(helpStr as String).isNullOrEmpty()) {
                val mn = getHelpStr(mContext, psw)
                privateKey = Wiccwallet.getPrivateKeyFromMnemonic(
                        mn,
                        netType!!.toLong()
                )
            } else {
                privateKey = getPKStr(mContext, psw)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            return privateKey
        }
    }

    //Save wallet infomation to native
    fun savePrivateKey(mContext: Context, bean: ImportPrivateKeyBean, password: String): Boolean {
        val md5_pwd = MD5Util.getMD5Str(password)
        val key = AESUtils2.encrypt(md5_pwd, bean.privateKey)
        if (md5_pwd != null && key != null && bean.address != null && bean.mhash != null) {
            deleteAllWalletCache(mContext)
            SPUtils.put(
                mContext,
                SPConstant.WALLET_PRIVATE_KEY,
                key
            )
            SPUtils.put(
                mContext,
                SPConstant.WALLET_MHASH,
                bean.mhash
            )
            SPUtils.put(
                mContext,
                SPConstant.WALLET_ADDRESS,
                bean.address
            )
            return true
        }
        return false
    }

    fun saveWallet(mContext: Context, helpStr: String, password: String): Boolean {
        var address: String? = null
        try {
            address = Wiccwallet.getAddressFromMnemonic(helpStr,
                netType!!.toLong()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (address != null) {
                val md5_pwd = MD5Util.getMD5Str(password)
                val en_Mnemonics = AESUtils2.encrypt(md5_pwd, helpStr)
                if (md5_pwd != null && en_Mnemonics != null) {
                    deleteAllWalletCache(mContext)
                    val mHash = SHAUtil.sha512Encrypt(helpStr)
                    SPUtils.put(
                        mContext,
                        SPConstant.WALLET_WORDS,
                        en_Mnemonics
                    )
                    SPUtils.put(
                        mContext,
                        SPConstant.WALLET_MHASH,
                        mHash
                    )
                    SPUtils.put(
                        mContext,
                        SPConstant.WALLET_ADDRESS,
                        address
                    )
                    return true
                } else {
                    return false
                }
            } else {
                return false
            }
        }
    }

    /*清除所有信息*/
    fun deleteAllWalletCache(context: Context) {
        SPUtils.remove(
            context,
            SPConstant.WALLET_PRIVATE_KEY
        )
        SPUtils.remove(
            context,
            SPConstant.WALLET_WORDS
        )
        SPUtils.remove(
            context,
            SPConstant.WALLET_MHASH
        )
        SPUtils.remove(
            context,
            SPConstant.WALLET_ADDRESS
        )
        SPUtils.remove(
            context,
            SPConstant.REGID
        )
    }

    fun genWorkerFee(fee: Long): Long {
        val random = Random()
        val ramdonFee = random.nextInt(100).toLong()
        return fee + ramdonFee
    }

    fun getPKStr(mContext: Context, psw: String): String? {
        val md5_pwd = MD5Util.getMD5Str(psw)
        val dwords = SPUtils.get(
            mContext,
            SPConstant.WALLET_PRIVATE_KEY,
            ""
        ) as String
        return AESUtils2.decrypt(md5_pwd, dwords)

    }

    fun getHelpStr(mContext: Context, psw: String): String? {
        val md5_pwd = MD5Util.getMD5Str(psw)
        val dwords = SPUtils.get(
            mContext,
            SPConstant.WALLET_WORDS,
            ""
        ) as String
        return AESUtils2.decrypt(md5_pwd, dwords)

    }

    fun checkPassword(mContext: Context, pwd: String): Boolean {
        if (pwd.isNullOrEmpty()) return false
        val md5_pwd = MD5Util.getMD5Str(pwd)
        val dwords = SPUtils.get(
            mContext,
            SPConstant.WALLET_WORDS,
            ""
        )!!.toString()
        if (dwords.isNullOrEmpty()) {
            val pk = SPUtils.get(
                mContext,
                SPConstant.WALLET_PRIVATE_KEY,
                ""
            )!!.toString()
            if (pk.isNullOrEmpty()) {
                return false
            } else {
                val pkd = AESUtils2.decrypt(md5_pwd, pk)
                return pkd != null
            }
        } else {
            val words = AESUtils2.decrypt(md5_pwd, dwords)
            return words != null
        }
    }
}