package nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike

import nl.tudelft.ipv8.attestation.trustchain.TrustChainTransaction


data class MusicProfile(
    val publicKey: String,
    val likedSongs: List<String>, // List of all liked song IDs
    val protocolVersion: String
    // Additional fields like tags can be added in the future
) {
    companion object {
        const val BLOCK_TYPE = "music_profile"

        fun fromTrustChainTransaction(transaction: TrustChainTransaction): MusicProfile {
            return MusicProfile(
                publicKey = transaction["publicKey"] as String,
                likedSongs = (transaction["likedSongs"] as List<*>).filterIsInstance<String>(),
                protocolVersion = transaction["protocolVersion"] as String
            )
        }

        fun toTransaction(block: MusicProfile): Map<String, Any> {
            return mapOf(
                "publicKey" to block.publicKey,
                "likedSongs" to block.likedSongs, // Serialize liked songs
                "protocolVersion" to block.protocolVersion
                // Additional data like tags can be added here in the future
            )
        }
    }
}
