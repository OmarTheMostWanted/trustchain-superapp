package nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike

import nl.tudelft.ipv8.attestation.trustchain.TrustChainTransaction


data class MusicLikeBlock(
    val publicKey: String,
    val name: String,
    val likedMusicId: String,
    val protocolVersion: String
) {
    companion object {
        const val BLOCK_TYPE = "music_like"

        fun fromTrustChainTransaction(transaction: TrustChainTransaction): MusicLikeBlock {
            return MusicLikeBlock(
                publicKey = transaction["publicKey"] as String,
                likedMusicId = transaction["likedMusicId"] as String,
                name = transaction["name"] as String,
                protocolVersion = transaction["protocolVersion"] as String
            )
        }
    }
}
