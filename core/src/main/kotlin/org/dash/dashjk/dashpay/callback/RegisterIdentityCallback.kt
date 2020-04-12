package org.dash.dashjk.dashpay.callback

interface RegisterIdentityCallback {
    fun onComplete(uniqueId: String)
    fun onTimeout()
}