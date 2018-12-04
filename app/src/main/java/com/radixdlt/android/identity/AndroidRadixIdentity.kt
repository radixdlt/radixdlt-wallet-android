package com.radixdlt.android.identity

import com.radixdlt.client.application.identity.Data
import com.radixdlt.client.application.identity.RadixIdentity
import com.radixdlt.client.application.identity.UnencryptedData
import com.radixdlt.client.core.atoms.Atom
import com.radixdlt.client.core.atoms.UnsignedAtom
import com.radixdlt.client.core.crypto.CryptoException
import com.radixdlt.client.core.crypto.ECKeyPair
import com.radixdlt.client.core.crypto.ECPublicKey
import com.radixdlt.client.core.crypto.MacMismatchException
import io.reactivex.Single

class AndroidRadixIdentity(private val myKey: ECKeyPair) : RadixIdentity {

    override fun sign(atom: UnsignedAtom): Single<Atom> {
        return Single.create { emitter ->
            val signature = myKey.sign(atom.hash.toByteArray())
            val signatureId = myKey.getUID()
            emitter.onSuccess(atom.sign(signature, signatureId))
        }
    }

    override fun decrypt(data: Data): Single<UnencryptedData> {
        val encrypted = data.getMetaData()["encrypted"] as Boolean
        if (encrypted) {
            for (protector in data.encryptor.protectors) {
                // TODO: remove exception catching
                try {
                    val bytes = myKey.decrypt(data.bytes!!, protector)
                    return Single.just(UnencryptedData(bytes, data.getMetaData(), true))
                } catch (e: MacMismatchException) {
                }
            }
            return Single.error(CryptoException("Cannot decrypt"))
        } else {
            return Single.just(UnencryptedData(data.bytes!!, data.getMetaData(), false))
        }
    }

    override fun getPublicKey(): ECPublicKey {
        return myKey.getPublicKey()
    }
}
