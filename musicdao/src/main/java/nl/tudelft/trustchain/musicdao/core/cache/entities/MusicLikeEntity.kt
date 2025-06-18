package nl.tudelft.trustchain.musicdao.core.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike

@Entity(primaryKeys = ["publicKey", "songName"])
data class MusicLikeEntity(
    val publicKey: String,
    val songName: String,
    val protocolVersion: String
) {
    fun toMusicLike(): MusicLike {
        return MusicLike(
            publicKey = publicKey,
<<<<<<< HEAD
            name = name,
            likedMusicId = likedMusicId,
            protocolVersion = protocolVersion,
=======
            songName = songName,
            protocolVersion = protocolVersion
>>>>>>> master
        )
    }
}
