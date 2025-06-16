package nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike

import nl.tudelft.ipv8.attestation.trustchain.TrustChainTransaction


data class MusicProfile(
    val publicKey: String,
    val likedSongs: List<String>, // List of all liked song IDs
    val protocolVersion: String,
    val tags: Map<String, List<String>>,
    val ethereumWalletAddress: String
    // Additional fields like tags can be added in the future
) {
    companion object {
        const val BLOCK_TYPE = "music_profile"

        fun fromTrustChainTransaction(transaction: TrustChainTransaction): MusicProfile {
            val rawTags = transaction["tags"] as? Map<*, *>
            val tags = rawTags?.mapNotNull { (key, value) ->
                if (key is String && value is List<*> && value.all { it is String })
                    key to value.filterIsInstance<String>()
                else null
            }?.toMap() ?: emptyMap()

            return MusicProfile(
                publicKey = transaction["publicKey"] as String,
                likedSongs = (transaction["likedSongs"] as List<*>).filterIsInstance<String>(),
                protocolVersion = transaction["protocolVersion"] as String,
                tags = tags,
                ethereumWalletAddress = transaction["ethereumWalletAddress"] as? String ?: ""
            )
        }

        fun toTransaction(block: MusicProfile): Map<String, Any> {
            return mapOf(
                "publicKey" to block.publicKey,
                "likedSongs" to block.likedSongs, // Serialize liked songs
                "protocolVersion" to block.protocolVersion,
                "tags" to block.tags,
                "ethereumWalletAddress" to block.ethereumWalletAddress
                // Additional data
            )
        }
    }
}
