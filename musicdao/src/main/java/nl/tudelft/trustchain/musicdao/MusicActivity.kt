package nl.tudelft.trustchain.musicdao

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.lifecycleScope
import nl.tudelft.trustchain.musicdao.core.ipv8.SetupMusicCommunity
import nl.tudelft.trustchain.musicdao.core.repositories.AlbumRepository
import nl.tudelft.trustchain.musicdao.core.repositories.ArtistRepository
import nl.tudelft.trustchain.musicdao.core.repositories.MusicGossipingService
import nl.tudelft.trustchain.musicdao.core.repositories.album.BatchPublisher
import nl.tudelft.trustchain.musicdao.core.torrent.TorrentEngine
import nl.tudelft.trustchain.musicdao.core.wallet.WalletService
import nl.tudelft.trustchain.musicdao.ui.MusicDAOApp
import nl.tudelft.trustchain.musicdao.ui.screens.profile.ProfileScreenViewModel
import com.frostwire.jlibtorrent.SessionManager
import com.google.common.util.concurrent.Service
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.*
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.coin.WalletManager
import nl.tudelft.trustchain.musicdao.core.ethereum.EthereumWalletManager
import nl.tudelft.trustchain.musicdao.core.repositories.MusicProfileRepository
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import javax.inject.Inject

/**
 * This maintains the interactions between the UI and seeding/trust-chain
 */
@AndroidEntryPoint
class MusicActivity : AppCompatActivity() {
    @Inject
    lateinit var albumRepository: AlbumRepository

    @Inject
    lateinit var artistRepository: ArtistRepository

    @Inject
    lateinit var profileRepository: MusicProfileRepository

    @OptIn(DelicateCoroutinesApi::class)
    @Inject
    lateinit var torrentEngine: TorrentEngine

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var walletService: WalletService

    @Inject
    lateinit var walletManager: WalletManager

    @Inject
    lateinit var batchPublisher: BatchPublisher

    @Inject
    lateinit var setupMusicCommunity: SetupMusicCommunity

    @Inject
    lateinit var database: CacheDatabase

    @Inject
    lateinit var ethWalletManager: EthereumWalletManager

    lateinit var mService: MusicGossipingService
    var mBound: Boolean = false

    @DelicateCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.provide(this)
        @Suppress("DEPRECATION")
        lifecycleScope.launchWhenStarted {
            setupMusicCommunity.registerListeners()
            albumRepository.refreshCache()
            profileRepository.refreshCache()
            torrentEngine.seedStrategy()

            val json = assets.open("pandacd.txt").bufferedReader().use { it.readText() }
            val gson = Gson()

            val type = object : TypeToken<List<CCAlbumParsed>>() {}.type
            val albums: List<CCAlbumParsed> = gson.fromJson(json, type)
            val albumEntries =  albums.map { album ->
                AlbumEntity(
                    id = album.releaseId,
                    magnet = album.magnet,
                    title = album.title,
                    artist = album.artist,
                    publisher = album.publisher,
                    releaseDate = album.releaseDate,
                    songs = listOf(),
                    cover = null,
                    root = null,
                    isDownloaded = false,
                    infoHash = TorrentEngine.magnetToInfoHash(album.magnet),
                    torrentPath = null
                )
            }
            for (albumEntry in albumEntries) {
                Log.d("MusicDao", "Adding cc album ${albumEntry.title}")
                database.dao.insert(albumEntry)
            }
        }
        iterativelyFetchReleasesAndMusicLikes()
        BouncyCastleInitializer.ensureProvider()

        // Creating an ETH wallet
        if (ethWalletManager.doesWalletExists()) {
            Log.d("ETHSmartContracts", "Wallet already exists")
            val credentials = ethWalletManager.getWalletCredentials("Password")
            if (credentials != null) {
                Log.d("ETHSmartContracts", "Wallet address: ${credentials.address}")
            }
        } else {
            Log.d("ETHSmartContracts", "Generating a wallet")
            ethWalletManager.createWallet("Password")
        }


        Intent(this, MusicGossipingService::class.java).also { intent ->
            startService(intent)
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }

        Log.d(
            "MusicDao2",
            "DEBUG: $walletManager"
        )

        Log.d(
            "MusicDao2",
            "${walletManager.kit.state()}"
        )

        if (walletManager.kit.state() == Service.State.RUNNING) {
            val scope = CoroutineScope(Dispatchers.IO)
            val address = walletService.protocolAddress().toString()
            Log.d("MusicDao2", "onSetupCompletedListener (1)")

            scope.launch {
                val me = artistRepository.getMyself()
                Log.d("MusicDao2", "onSetupCompletedListener (2)")
                if (me != null) {
                    if (me.bitcoinAddress != address) {
                        Log.d("MusicDao2", "onSetupCompletedListener (3)")
                        artistRepository.edit(me.name, address, me.socials, me.biography)
                    }
                } else {
                    Log.d("MusicDao2", "onSetupCompletedListener (4)")
                    artistRepository.edit("Artist ${(0..10_000).random()}", address, "Socials", "Biography")
                }
            }
        }

        walletManager.addOnSetupCompletedListener {
            val scope = CoroutineScope(Dispatchers.IO)
            val address = walletService.protocolAddress().toString()
            Log.d("MusicDao2", "onSetupCompletedListener (1)")

            scope.launch {
                val me = artistRepository.getMyself()
                Log.d("MusicDao2", "onSetupCompletedListener (2)")
                if (me != null) {
                    if (me.bitcoinAddress != address) {
                        Log.d("MusicDao2", "onSetupCompletedListener (3)")
                        artistRepository.edit(me.name, address, me.socials, me.biography)
                    }
                } else {
                    Log.d("MusicDao2", "onSetupCompletedListener (4)")
                    artistRepository.edit(
                        "Artist ${(0..10_000).random()}",
                        address,
                        "Socials",
                        "Biography"
                    )
                }
            }
        }

        walletService.addOnSetupCompletedListener {
            val scope = CoroutineScope(Dispatchers.IO)
            val address = walletService.protocolAddress().toString()
            Log.d("MusicDao2", "onSetupCompletedListener (1)")

            scope.launch {
                val me = artistRepository.getMyself()
                Log.d("MusicDao2", "onSetupCompletedListener (2)")
                if (me != null) {
                    if (me.bitcoinAddress != address) {
                        Log.d("MusicDao2", "onSetupCompletedListener (3)")
                        artistRepository.edit(me.name, address, me.socials, me.biography)
                    }
                } else {
                    Log.d("MusicDao2", "onSetupCompletedListener (4)")
                    artistRepository.edit("Artist ${(0..10_000).random()}", address, "Socials", "Biography")
                }
            }
        }

        setContent {
            MusicDAOApp()
        }
    }

    /**
     * On discovering a half block, with tag publish_release, agree it immediately (for now). In the
     * future there will be logic added here to determine whether an upload was done by the correct
     * artist/label (artist passport).
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mBound) {
            unbindService(mConnection)
        }
    }

    object BouncyCastleInitializer {
        fun ensureProvider() {
            val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            if (provider == null || provider.javaClass != BouncyCastleProvider::class.java) {
                // Remove the wrong one and add the correct one
                Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
                Security.addProvider(BouncyCastleProvider())
            }
        }
    }

    private val mConnection =
        object : ServiceConnection {
            // Called when the connection with the service is established
            override fun onServiceConnected(
                className: ComponentName,
                service: IBinder
            ) {
                val binder = service as MusicGossipingService.LocalBinder
                mService = binder.getService()
                mBound = true
            }

            // Called when the connection with the service disconnects unexpectedly
            override fun onServiceDisconnected(className: ComponentName) {
                mBound = false
            }
        }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        val uris = uriListFromLocalFiles(intent = data!!)
        AppContainer.currentCallback(uris)
    }

    private fun uriListFromLocalFiles(intent: Intent): List<Uri> {
        // This should be reached when the chooseFile intent is completed and the user selected
        // an audio file
        val uriList = mutableListOf<Uri>()
        val singleFileUri = intent.data
        if (singleFileUri != null) {
            // Only one file is selected
            uriList.add(singleFileUri)
        }
        val clipData = intent.clipData
        if (clipData != null) {
            // Multiple files are selected
            val count = clipData.itemCount
            for (i in 0 until count) {
                val uri = clipData.getItemAt(i).uri
                uriList.add(uri)
            }
        }
        return uriList
    }

    private fun iterativelyFetchReleasesAndMusicLikes() {
        @Suppress("DEPRECATION")
        lifecycleScope.launchWhenStarted {
            while (isActive) {
                albumRepository.refreshCache()
                profileRepository.refreshCache()
                delay(3000)
            }
        }
    }

    override fun startActivityForResult(
        intent: Intent?,
        requestCode: Int
    ) {
        require(!(requestCode != -1 && requestCode and -0x10000 != 0)) { "Can only use lower 16 bits for requestCode" }
        @Suppress("DEPRECATION")
        super.startActivityForResult(intent, requestCode)
    }

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        // fun noteDetailViewModelFactory(): ReleaseScreenViewModel.ReleaseScreenViewModelFactory

        fun profileScreenViewModelFactory(): ProfileScreenViewModel.ProfileScreenViewModelFactory
    }

    data class CCAlbumParsed(
        val releaseId: String,
        val magnet: String,
        val title: String,
        val artist: String,
        val publisher: String,
        val releaseDate: String
    )
}
