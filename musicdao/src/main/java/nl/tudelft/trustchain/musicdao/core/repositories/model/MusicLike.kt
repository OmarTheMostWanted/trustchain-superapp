package nl.tudelft.trustchain.musicdao.core.repositories.model

class MusicLike (
    val publicKey: String,
    val name: String,
    val likedMusicId: String,
    val protocolVersion: String
)
