package org.dashevo.examples

import org.bitcoinj.core.Sha256Hash
import org.dashevo.Client
import org.dashevo.dashpay.BlockchainIdentity
import org.dashevo.dapiclient.model.DocumentQuery
import org.dashevo.dpp.toHexString
import org.dashevo.dpp.util.HashUtils
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.timerTask

class WaitUntilRegistered {
    companion object {
        val sdk = Client("mobile")

        @JvmStatic
        fun main(args: Array<String>) {
            val nonExistantId = "GJvyvWN4M8LWQ1KW2N6ure9cYmqxcpX8CE5HojXebhTV"
            /*monitorForBlockchainIdentityWithRetryCount(nonExistantId, 10, 1000, BlockchainIdentity.RetryDelayType.LINEAR,
                object: MonitorListener {
                    override fun onComplete() {
                        println("listener called")
                    }

                    override fun onTimeout() {
                        println("listener called: falure") //To change body of created functions use File | Settings | File Templates.
                    }
                })
            println("starting wait before finish")
           */
            val saltedDomainHashes = HashMap<String, ByteArray>()

            saltedDomainHashes["name1"] = HashUtils.fromHex("56202b7c08fa22c63994de74d7d8b085a55354e410dd4f8a9053fecdff36fe577e4c") //Sha256Hash.twiceOf(ByteArray(32)).bytes
            saltedDomainHashes["name2"] = HashUtils.fromHex("56204e6d046b316643f0c10d7d53eb4c3ae60fd5eaeb7347052f60b59d4a39761c65")//Sha256Hash.twiceOf(ByteArray(33)).bytes
            saltedDomainHashes["name3"] = HashUtils.fromHex("5620a4dd64d037d0aa170a148665d841db74a69739a4b08c18c7e295788a7d35223e")//Sha256Hash.twiceOf(ByteArray(33)).bytes


            var query = DocumentQuery.Builder()
                .where(listOf("saltedDomainHash","in",saltedDomainHashes.map {"5620${it.value.toHexString()}"})).build()
            val preorderDocuments = sdk.platform.documents.get("dpns.preorder", query)

            val usernames = HashMap<String, Any>()
            usernames["test1"] = Sha256Hash.of("hello".toByteArray())
            usernames["test2"] = Sha256Hash.of("hi".toByteArray())

            query = DocumentQuery.Builder()
                .where("normalizedParentDomainName", "==", "dash")
                .where(listOf("normalizedLabel","in",usernames.map {"${it.key.toLowerCase()}"})).build()
            val bytes = query.encodeWhere()
            val nameDocuments = sdk.platform.documents.get("dpns.domain", query)
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