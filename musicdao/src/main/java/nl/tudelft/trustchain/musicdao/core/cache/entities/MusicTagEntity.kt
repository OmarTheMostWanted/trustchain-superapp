package nl.tudelft.trustchain.musicdao.core.cache.entities

import androidx.room.Entity
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicTag

@Entity(primaryKeys = ["publicKey", "songName", "tag"])
data class MusicTagEntity(
    val publicKey: String,
    val songName: String,
    val tag: String,
    val protocolVersion: String
) {
    fun toMusicTag(): MusicTag {
        return MusicTag(
            publicKey = publicKey,
            songName = songName,
            tag = tag,
            protocolVersion = protocolVersion
        )
    }
}
