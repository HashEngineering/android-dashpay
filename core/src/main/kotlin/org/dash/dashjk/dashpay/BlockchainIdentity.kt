package org.dash.dashjk.dashpay

import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.params.EvoNetParams
import org.bitcoinj.wallet.Wallet
import org.dash.dashjk.platform.Platform
import org.dashevo.dapiclient.DapiClient
import org.dashevo.dpp.DashPlatformProtocol
import org.dashevo.dpp.DataProvider
import org.dashevo.dpp.contract.Contract
import org.dashevo.dpp.document.Document
import org.dashevo.dpp.identity.Identity
import org.dashevo.dpp.util.Entropy
import java.io.ByteArrayOutputStream

class BlockchainIdentity(val identity: Identity, val wallet: Wallet, val platform: Platform) {


}