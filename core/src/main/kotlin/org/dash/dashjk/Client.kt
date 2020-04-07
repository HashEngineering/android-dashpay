package org.dash.dashjk

import org.bitcoinj.params.EvoNetParams
import org.bitcoinj.params.MobileDevNetParams
import org.dash.dashjk.platform.Platform

class Client(network: String) {
    val platform = Platform(if(network == "testnet") EvoNetParams.get() else MobileDevNetParams.get())

    fun isReady(): Boolean {
        return true
    }
}