package nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike

import nl.tudelft.ipv8.attestation.trustchain.TrustChainTransaction


data class MusicLikeBlock(
    val publicKey: String,
    val name: String,
    val likedSongs: List<String>, // List of all liked song IDs
    val protocolVersion: String
) {
    companion object {
        const val BLOCK_TYPE = "music_likes_state"

        fun fromTrustChainTransaction(transaction: TrustChainTransaction): MusicLikeBlock {
            return MusicLikeBlock(
                publicKey = transaction["publicKey"] as String,
                likedSongs = (transaction["likedSongs"] as List<*>).filterIsInstance<String>(),
                name = transaction["name"] as String,
                protocolVersion = transaction["protocolVersion"] as String
            )
        }

        fun toTransaction(block: MusicLikeBlock): Map<String, Any> {
            return mapOf(
                "publicKey" to block.publicKey,
                "name" to block.name,
                "likedSongs" to block.likedSongs, // Serialize liked songs
                "protocolVersion" to block.protocolVersion
                // Additional data like tags can be added here in the future
            )
        }
    }
}
