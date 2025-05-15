package nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike

import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.TrustChainTransaction
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainStore
import nl.tudelft.ipv8.attestation.trustchain.validation.TransactionValidator
import nl.tudelft.ipv8.attestation.trustchain.validation.ValidationResult
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import javax.inject.Inject

class MusicLikeBlockValidator
    @Inject
    constructor() : TransactionValidator {
        override fun validate(
            block: TrustChainBlock,
            database: TrustChainStore
        ): ValidationResult {
            return if (validate(block)) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid(listOf("Not all information included."))
            }
        }

        private fun validate(block: TrustChainBlock): Boolean {
            return validateTransaction(block.transaction)
        }

        fun validateTransaction(transaction: TrustChainTransaction): Boolean {
            val publicKey = transaction["publicKey"]
            val likedMusicId = transaction["likedMusicId"]
            val name = transaction["name"]
            val protocolVersion = transaction["protocolVersion"]

            return (
                    publicKey is String && publicKey.isNotEmpty() && transaction.containsKey("publicKey") &&
                    likedMusicId is String && likedMusicId.isNotEmpty() && transaction.containsKey("likedMusicId") &&
                    name is String && name.isNotEmpty() && transaction.containsKey("name") &&
                    protocolVersion is String && protocolVersion.isNotEmpty() && protocolVersion == Constants.PROTOCOL_VERSION
                )
        }
    }
