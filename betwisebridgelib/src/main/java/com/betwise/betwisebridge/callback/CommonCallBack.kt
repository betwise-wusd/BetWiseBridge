package com.betwise.betwisebridge.callback

interface  CommonCallBack<T> {
    fun onSuccess(t: T)
    fun onFailure(t: T)
}