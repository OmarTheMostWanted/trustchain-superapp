package nl.tudelft.trustchain.musicdao.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data object Release : Screen("release/{releaseId}") {
        fun createRoute(releaseId: String) = "release/$releaseId"
    }

    data object Search : Screen("search")

    data object Settings : Screen("settings")

    data object Debug : Screen("debug")

    data object FullPlayerScreen : Screen("fullPlayerScreen")

    data object CreatorMenu : Screen("me")

    data object MyProfile : Screen("me/profile")

    data object EditProfile : Screen("me/edit")

    data object BitcoinWallet : Screen("me/wallet")

    data object DiscoverArtists : Screen("artists")

    data object Profile : Screen("profile/{publicKey}") {
        fun createRoute(publicKey: String) = "profile/$publicKey"
    }

    data object Donate : Screen("profile/{publicKey}/donate") {
        fun createRoute(publicKey: String) = "profile/$publicKey/donate"
    }

    data object Vote : Screen("vote")

//    data object ProposalDetailRoute : Screen("proposal/{proposalId}/detail") {
//        fun createRoute(proposalId: String) = "proposal/$proposalId/detail"
//    }
//
//    data object NewProposalRoute : Screen("proposal/{daoId}/new") {
//        fun createRoute(daoId: String) = "proposal/$daoId/new"
//    }
//
    data object CreateRelease : Screen("release/create")
}
