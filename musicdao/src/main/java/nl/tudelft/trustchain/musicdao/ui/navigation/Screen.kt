package nl.tudelft.trustchain.musicdao.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")

    object Leaderboard : Screen("leaderboard")

    object Release : Screen("release/{releaseId}?title={title}&artist={artist}") {

        // If a specific song (title + artist) is provided, include them as query parameters.
        // Otherwise, just navigate to the album with its ID.
        fun createRoute(releaseId: String, title: String? = null, artist: String? = null): String {
            return if (title != null && artist != null) {
                // Include song metadata in the route to support auto-play (if provided)
                "release/$releaseId?title=$title&artist=$artist"
            } else {
                // Default navigation to the album without song selection
                "release/$releaseId"
            }
        }
    }

    object Search : Screen("search")

    object Settings : Screen("settings")

    object Debug : Screen("debug")

    object FullPlayerScreen : Screen("fullPlayerScreen")

    object CreatorMenu : Screen("me")

    object MyProfile : Screen("me/profile")

    object EditProfile : Screen("me/edit")

    object BitcoinWallet : Screen("me/wallet")

    object DiscoverArtists : Screen("artists")

    object Profile : Screen("profile/{publicKey}") {
        fun createRoute(publicKey: String) = "profile/$publicKey"
    }

    object Donate : Screen("profile/{publicKey}/donate") {
        fun createRoute(publicKey: String) = "profile/$publicKey/donate"
    }

    object DaoRoute : Screen("DaoRoute")

    object DaoDetailRoute : Screen("dao/{daoId}/detail") {
        fun createRoute(daoId: String) = "dao/$daoId/detail"
    }

    object ProposalDetailRoute : Screen("proposal/{proposalId}/detail") {
        fun createRoute(proposalId: String) = "proposal/$proposalId/detail"
    }

    object NewProposalRoute : Screen("proposal/{daoId}/new") {
        fun createRoute(daoId: String) = "proposal/$daoId/new"
    }

    object NewDaoRoute : Screen("dao/new")

    object CreateRelease : Screen("release/create")
}
