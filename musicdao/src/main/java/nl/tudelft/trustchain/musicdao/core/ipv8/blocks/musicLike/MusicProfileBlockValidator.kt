package nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike

import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.TrustChainTransaction
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainStore
import nl.tudelft.ipv8.attestation.trustchain.validation.TransactionValidator
import nl.tudelft.ipv8.attestation.trustchain.validation.ValidationResult
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import javax.inject.Inject

class MusicProfileBlockValidator
    @Inject
    constructor() : TransactionValidator {
        override fun validate(
            block: TrustChainBlock,
            database: TrustChainStore
        ): ValidationResult =
            if (validate(block)) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid(listOf("Not all information included."))
            }

        private fun validate(block: TrustChainBlock): Boolean = validateTransaction(block.transaction)

        fun validateTransaction(transaction: TrustChainTransaction): Boolean {
            val publicKey = transaction["publicKey"]
            val likedSongs = transaction["likedSongs"]
            val protocolVersion = transaction["protocolVersion"]
            val tags = transaction["tags"]

            val isValidTags = tags == null || (
                tags is Map<*, *> &&
                    tags.keys.all { it is String } &&
                    tags.values.all { it is List<*> && it.all { tag -> tag is String } }
                )


            return (
<<<<<<< HEAD:musicdao/src/main/java/nl/tudelft/trustchain/musicdao/core/ipv8/blocks/musicLike/MusicLikeBlockValidator.kt
                publicKey is String &&
                    publicKey.isNotEmpty() &&
                    transaction.containsKey("publicKey") &&
                    likedMusicId is String &&
                    likedMusicId.isNotEmpty() &&
                    transaction.containsKey("likedMusicId") &&
                    name is String &&
                    name.isNotEmpty() &&
                    transaction.containsKey("name") &&
                    protocolVersion is String &&
                    protocolVersion.isNotEmpty() &&
                    protocolVersion == Constants.PROTOCOL_VERSION
            )
=======
                publicKey is String && publicKey.isNotEmpty() &&
                    likedSongs is List<*> && likedSongs.all { it is String } &&
                    protocolVersion is String && protocolVersion == Constants.PROTOCOL_VERSION &&
                    isValidTags
                )
>>>>>>> master:musicdao/src/main/java/nl/tudelft/trustchain/musicdao/core/ipv8/blocks/musicLike/MusicProfileBlockValidator.kt
        }

        companion object {
            const val BLOCK_TYPE = MusicProfile.BLOCK_TYPE
        }
    }
