package com.betwise.betwisebridge.bean

/**
 * All bean class
 * Date: 2019-11-26
 */
class DappBean(val errorCode: Int, val errorMsg: String, val result: Any, val rawtx: String){
    constructor(errorCode: Int, errorMsg: String) : this(errorCode, errorMsg, "","")
    constructor(errorCode: Int, errorMsg: String, result: Any) : this(errorCode, errorMsg, result,"")
}

class AddressInfoBean {
    var address: String? = null
    var supplier: String? = null
}

data class CreateOrImportWalletBean(
    var isComplete: String,
    var mnCorrectness: String,
    var helpStr: String?,
    var address: String?,
    var mhash: String?,
    var errorMsg: String
)

data class ImportPrivateKeyBean(
    var isComplete: String?,
    var privateKey: String?,
    var address: String?,
    var mhash: String?,
    var errorMsg: String?
)

class WalletUCoinTransferSignHexBean {
    /**
     * destArr : [{"amount":"100000000","coinSymbol":"WICC","destAddr":"wbZTWpEnbYoYsedMm2knnP4q7KiSdS3yVq"}]
     * memo : test transfer
     */
    var password: String? = null
    var fee: String? = null
    var height: Int? = null
    var regId: String? = null
    var feeSymbol: String? = null
    var memo: String? = null
    var destArr: List<DestArrBean>? = null

    class DestArrBean {
        /**
         * amount : 100000000
         * coinSymbol : WICC
         * destAddr : wbZTWpEnbYoYsedMm2knnP4q7KiSdS3yVq
         */

        var amount: String? = null
        var coinSymbol: String? = null
        var destAddr: String? = null
    }
}

class UCoinTransferBean {

    /**
     * destArr : [{"amount":"100000000","coinSymbol":"WICC","destAddr":"wbZTWpEnbYoYsedMm2knnP4q7KiSdS3yVq"}]
     * memo : test transfer
     */

    var memo: String? = null
    var genSign:String?=null
    var destArr: ArrayList<DestArrBean>? = null

    class DestArrBean {
        /**
         * amount : 100000000
         * coinSymbol : WICC
         * destAddr : wbZTWpEnbYoYsedMm2knnP4q7KiSdS3yVq
         */

        var amount: String? = null
        var coinSymbol: String? = null
        var destAddr: String? = null
    }
}

class WalletUCoinContractBean {

    /**
     * amount : 100000000
     * coinSymbol : WICC
     * regId : 0-1
     * contract : f001
     * memo : test transfer
     */

    var password: String? = null
    var height: Int? = null
    var userId: String? = null
    var fee: String? = null
    var feeSymbol: String? = null

    var amount: String? = null
    var coinSymbol: String? = null
    var regId: String? = null
    var contract: String? = null
    var memo: String? = null
}

class UCoinContractBean {

    /**
     * amount : 100000000
     * coinSymbol : WICC
     * regId : 0-1
     * contract : f001
     * memo : test transfer
     */

    var amount: String? = null
    var coinSymbol: String? = null
    var regId: String? = null
    var contract: String? = null
    var memo: String? = null
    var genSign: String? =null
}

data class ChangePasswordBean(var newPassword: String, var oldPassword: String)
data class PasswordBean(var password: String)

class SignHexBean(isComplete: String, errorMsg: String,var signHex:String) : BaseBean(isComplete, errorMsg)

class CreateAccountBean(var password: String, var helpStr: String)

open class BaseBean(var isComplete: String, var errorMsg: String)

data class PrivateKeyBean(var password: String, var privateKey: String)

data class RegIdBean(var regId: String)