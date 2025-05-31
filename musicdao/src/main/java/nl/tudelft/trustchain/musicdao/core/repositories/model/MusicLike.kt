package nl.tudelft.trustchain.musicdao.core.repositories.model

class MusicLike (
    val publicKey: String,
    val name: String,
    val likedSongs: List<String>, // Updated to store a list of liked song IDs
    val protocolVersion: String
) {

    // TODO: Use a better ID generation to reduce overlap
    companion object {
        fun musicLikeIdFromSong(track: Song): String {
            return track.title + "_" + track.artist
        }
    }
}
