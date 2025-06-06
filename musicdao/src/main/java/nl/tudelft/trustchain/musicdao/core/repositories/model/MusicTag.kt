package nl.tudelft.trustchain.musicdao.core.repositories.model

data class MusicTag(
    val publicKey: String,
    val songName: String,
    val tag: String,
    val protocolVersion: String
)
