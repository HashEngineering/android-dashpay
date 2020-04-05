package org.dash.dashjk.platform

import org.bitcoinj.params.EvoNetParams
import org.bitcoinj.params.MobileDevNetParams
import org.dashevo.dapiclient.DapiClient
import org.dashevo.dapiclient.model.DocumentQuery
import org.dashevo.dpp.DashPlatformProtocol
import org.dashevo.dpp.DataProvider
import org.dashevo.dpp.contract.Contract
import org.dashevo.dpp.document.Document
import org.dashevo.dpp.identity.Identity

class Platform(val isMobile: Boolean = false) {

    var dataProvider: DataProvider = object : DataProvider {
        override fun fetchDataContract(s: String): Contract? {
            return contracts.get(s)
        }

        override fun fetchDocuments(s: String, s1: String, o: Any): List<Document> {
            return documents.get(s, o as DocumentQuery)
        }

        override fun fetchTransaction(s: String): Int {
            TODO()
        }

        override fun fetchIdentity(s: String): Identity? {
            return identities.get(s)
        }
    }

    val dpp = DashPlatformProtocol(dataProvider)
    val apps = HashMap<String, ContractInfo>()
    val contracts = ContractHandler(this)
    val documents = DocumentsHandler(this)
    val identities = IdentityHandler(this)
    var names = NamesHandler(this)
    lateinit var client: DapiClient

    init {
        if(!isMobile) {
            apps["dpns"] = ContractInfo("77w8Xqn25HwJhjodrHW133aXhjuTsTv9ozQaYpSHACE3")
        } else {
            apps["dpns"] = ContractInfo("7hjHdNMWNvj3QMiDWuehzBPzMbxCVrWPazukrT2uNGVB")
            apps["dashpay"] = ContractInfo("42isZhFyhPzVFfJPZbNqAotFHb1iBajbaQAMPyVdvY6F")
        }
        client = DapiClient(if(isMobile) MobileDevNetParams.MASTERNODES[1] else EvoNetParams.MASTERNODES[1], true)
    }

}