package nl.tudelft.trustchain.musicdao.core.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike

@Entity
data class MusicLikeEntity(
    @PrimaryKey val id: String,
    val publicKey: String,
    val name: String,
    val likedSongs: String, // Store as a JSON string
    val protocolVersion: String
) {
    fun toMusicLike(): MusicLike {
        return MusicLike(
            publicKey = publicKey,
            name = name,
            likedSongs = if (likedSongs.isNotEmpty()) likedSongs.split(",") else emptyList(), // Safely split the string
            protocolVersion = protocolVersion
        )
    }
}
