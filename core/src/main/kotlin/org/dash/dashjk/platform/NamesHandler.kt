package org.dash.dashjk.platform

import org.bitcoinj.core.Base58
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.params.EvoNetParams
import org.dashevo.dapiclient.DapiClient
import org.dashevo.dapiclient.model.DocumentQuery
import org.dashevo.dpp.document.Document
import org.dashevo.dpp.identity.Identity
import org.dashevo.dpp.util.Entropy
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.Thread.sleep

class NamesHandler (val platform: Platform) {


    fun register(name: String, identity: Identity, identityHDPrivateKey: ECKey): Document
    {
        val dpp = platform.dpp

        val identityType = if (identity.type.value == 2) "application" else "user"

        // @ts-ignore

        val records = HashMap<String, Any?>(1)
        records["dashIdentity"] = identity.id

        val nameSlice = name.indexOf('.')
        val normalizedParentDomainName =
            if(nameSlice == -1) "dash" else name.slice(nameSlice + 1 .. name.length)

        val label = if(nameSlice == -1) name else name.slice(0 .. nameSlice)

        val normalizedLabel = label.toLowerCase();
        val fullDomainName = "$normalizedLabel.$normalizedParentDomainName";

        val nameHash = Sha256Hash.twiceOf(fullDomainName.toByteArray())
        val nameHashHex = nameHash.toString()

        val preorderSaltBase58 = Entropy.generate();
        val preOrderSaltRaw = Base58.decode(preorderSaltBase58)

        val baos = ByteArrayOutputStream(preOrderSaltRaw.size + nameHash.bytes.size)
        baos.write(preOrderSaltRaw)
        baos.write(0x56)
        baos.write(0x20)
        baos.write(nameHash.bytes)

        val saltedDomainHash = Sha256Hash.twiceOf(baos.toByteArray()).toString()

        if (platform.apps["dpns"] == null) {
            throw Error("DPNS is required to register a new name.")
        }
        // 1. Create preorder document

        //val client = DapiClient(EvoNetParams.MASTERNODES[0])

        val map = JSONObject("{saltedDomainHash: \"5620$saltedDomainHash\"}").toMap()

        val preorderDocument = platform.documents.create(
            "dpns.preorder",
            identity,
            map
            )

        println("preorder:" + preorderDocument.toJSON().toString())
        val preorderTransition = dpp.document.createStateTransition(listOf(preorderDocument))
        preorderTransition.sign(identity.getPublicKeyById(1)!!, identityHDPrivateKey.privateKeyAsHex);

        val isValid = preorderTransition.verifySignature(identity.getPublicKeyById(1)!!)
        // @ts-ignore
        platform.client.applyStateTransition(preorderTransition)

        sleep(1000*60)

        val fields = HashMap<String, Any?>(6);
        fields["nameHash"] = "5620$nameHashHex"
        fields["label"] = label
        fields["normalizedLabel"] = normalizedLabel
        fields["normalizedParentDomainName"] = normalizedParentDomainName
        fields["preorderSalt"] = preorderSaltBase58
        fields["records"] = records

        // 3. Create domain document
        val domainDocument = platform.documents.create(
            "dpns.domain",
            identity,
            fields
        );

        println(domainDocument.toJSON())

        // 4. Create and send domain state transition
        val domainTransition = dpp.document.createStateTransition(listOf(domainDocument));
        domainTransition.sign(identity.getPublicKeyById(1)!!, identityHDPrivateKey.privateKeyAsHex);

        println(domainTransition.toJSON())

        // @ts-ignore
        platform.client.applyStateTransition(domainTransition)

        return domainDocument;

    }

    fun get(id: String): Document? {
        val queryOpts = DocumentQuery(
            listOf(
                listOf("normalizedLabel", "==", id.toLowerCase()).toMutableList(),
                listOf("normalizedParentDomainName", "==", "dash").toMutableList()
            ).toMutableList(),
            null, 1, 0, 0
        )
        try{
            val documents = platform.documents.get("dpns.domain", queryOpts);
            return if(documents[0] != null && documents.size != 0) documents[0] else null;
        } catch (e: Exception) {
            throw e;
        }
    }
}