package org.dash.dashjk.examples

import org.dash.dashjk.Client
import org.dash.dashjk.dashpay.BlockchainIdentity
import org.json.JSONObject
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.timerTask

class WaitUntilRegistered {
    companion object {
        val sdk = Client("mobile")

        @JvmStatic
        fun main(args: Array<String>) {
            val nonExistantId = "GJvyvWN4M8LWQ1KW2N6ure9cYmqxcpX8CE5HojXebhTV"
            monitorForBlockchainIdentityWithRetryCount(nonExistantId, 10, 1000, BlockchainIdentity.RetryDelayType.LINEAR,
                object: MonitorListener {
                    override fun onComplete() {
                        println("listener called")
                    }

                    override fun onTimeout() {
                        println("listener called: falure") //To change body of created functions use File | Settings | File Templates.
                    }
                })
            println("starting wait before finish")
            sleep(5000)
        }

        interface MonitorListener {
            fun onComplete()
            fun onTimeout()
        }

        //should this have a callback or let the client handle the end
        private fun monitorForBlockchainIdentityWithRetryCount(uniqueIdString: String, retryCount: Int, delayMillis: Long, retryDelayType: BlockchainIdentity.RetryDelayType,
                                                               listener: MonitorListener) {

            var identityResult = sdk.platform.identities.get(uniqueIdString)

            if(retryCount == 2)
                identityResult = sdk.platform.identities.get("GJvyvWN4M8LWQ1KW2N6ure9cYmqxcpX8CE5HojXebhTv")

            if (identityResult != null) {
                println("Identity found: $uniqueIdString")
                listener.onComplete()
            } else {
                if (retryCount > 0) {
                    Timer("monitorBlockchainIdentityStatus", false).schedule(timerTask {
                        println("retrying $retryCount times")
                        val nextDelay = delayMillis * when (retryDelayType) {
                            BlockchainIdentity.RetryDelayType.SLOW20 -> 5 / 4
                            BlockchainIdentity.RetryDelayType.SLOW50 -> 3 / 2
                            else -> 1
                        }
                        monitorForBlockchainIdentityWithRetryCount(uniqueIdString, retryCount - 1, nextDelay, retryDelayType, listener)
                    }, delayMillis)
                } else listener.onTimeout()
            }
            println("end of function")
        }
    }
}