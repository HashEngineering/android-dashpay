package org.dash.dashjk.dashpay.callback

interface RegisterPreorderCallback {
    fun onComplete(names: List<String>)
    fun onTimeout(incompleteNames: List<String>)
}