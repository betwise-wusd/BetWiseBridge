package com.betwise.betwisebridge.dict

import android.content.Context
import com.betwise.betwisebridge.R

/**
 * desc: Error Code For Wallet
 * Date: 2019-11-26
 */
enum class WalletError(var code: Int) {

    SUCCESS(0),
    OPERATION_FAILED(102),
    NO_WALLET_CREATE(2000),
    NO_PERMISSION(3000),
    ILLEGAL_PARAMETER(5000),
    USER_DENIED(7000);

    companion object{

        fun getMsgByErrorCode(mContext: Context, code: Int): String{
            return when(code){
                SUCCESS.code -> mContext.resources.getString(R.string.wallet_success)
                OPERATION_FAILED.code -> mContext.resources.getString(R.string.operation_failed)
                NO_WALLET_CREATE.code -> mContext.resources.getString(R.string.no_wallet_create)
                NO_PERMISSION.code ->mContext.resources.getString(R.string.no_permission)
                ILLEGAL_PARAMETER.code -> mContext.resources.getString(R.string.illegal_parameter)
                USER_DENIED.code -> mContext.resources.getString(R.string.user_denied)
                else -> ""
            }
        }
    }
}