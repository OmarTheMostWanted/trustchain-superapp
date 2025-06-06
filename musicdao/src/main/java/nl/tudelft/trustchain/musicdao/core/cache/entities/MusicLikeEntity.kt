package nl.tudelft.trustchain.musicdao.core.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike

@Entity
data class MusicLikeEntity(
    @PrimaryKey val id: String,
    val publicKey: String,
    val name: String,
    val likedMusicId: String,
    val protocolVersion: String
) {
    fun toMusicLike(): MusicLike {
        return MusicLike(
            publicKey = publicKey,
            name = name,
            likedMusicId = likedMusicId,
            protocolVersion = protocolVersion,
        )
    }
}
