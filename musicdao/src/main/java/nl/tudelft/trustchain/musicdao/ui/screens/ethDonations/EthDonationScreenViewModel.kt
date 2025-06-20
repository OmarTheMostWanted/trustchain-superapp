package nl.tudelft.trustchain.musicdao.ui.screens.ethDonations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.tudelft.trustchain.musicdao.core.ethereum.EthereumWalletManager
import nl.tudelft.trustchain.musicdao.core.repositories.MusicProfileRepository
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.tudelft.trustchain.musicdao.core.cache.entities.ArtistEntity
import org.web3j.crypto.Credentials
import org.web3j.utils.Convert
import java.math.BigInteger

@HiltViewModel
class EthDonationScreenViewModel @Inject constructor(
    private val ethWalletManager: EthereumWalletManager,
    private val musicProfileRepository: MusicProfileRepository
    ): ViewModel() {

    private var credentials: Credentials? = null

    private val _ethBalance = MutableStateFlow<BigInteger?>(null)
    val ethBalance: StateFlow<BigInteger?> = _ethBalance

    private val _nativeEthBalance = MutableStateFlow<BigInteger?>(null)
    val nativeEthBalance: StateFlow<BigInteger?> = _nativeEthBalance

    val myWalletAddress = musicProfileRepository
        .getEthereumWalletAddress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val _otherArtists = MutableStateFlow<List<ArtistEntity>>(emptyList())
    val otherArtists: StateFlow<List<ArtistEntity>> = _otherArtists

    private val _shouldPromptForWalletPassword = MutableStateFlow(false)
    val shouldPromptForWalletPassword: StateFlow<Boolean> = _shouldPromptForWalletPassword

    private val _transactionMessage = MutableStateFlow<String?>(null)
    val transactionMessage: StateFlow<String?> = _transactionMessage


    init {
        if (ethWalletManager.doesWalletExists()) {
            _shouldPromptForWalletPassword.value = true
        }
        viewModelScope.launch {
            val artists = musicProfileRepository.getAllArtists()
            Log.d("ETH_DEBUG", "Loaded artists: $artists")
            _otherArtists.value = artists
        }
    }

    fun onWalletPasswordEntered(password: String): Boolean {
        val creds = ethWalletManager.getWalletCredentials(password)
        val address = creds?.address
        if (creds != null && address != null) {
            credentials = creds
            viewModelScope.launch {
                musicProfileRepository.updateEthereumWalletAddress(address)
                fetchNativeEthBalance(address)
                fetchBalance(address, creds)
            }
            _shouldPromptForWalletPassword.value = false
            return true
        } else {
            _shouldPromptForWalletPassword.value = true
            return false
        }
    }

    fun connectWallet(privateKey: String, password: String): String? {
        val creds = ethWalletManager.createWalletFromExistingHexPK(privateKey, password)
        val address = creds?.address
        if (address != null) {
            credentials = creds
            viewModelScope.launch {
                musicProfileRepository.updateEthereumWalletAddress(address)
                fetchNativeEthBalance(address)
                fetchBalance(address, creds)
            }
            _shouldPromptForWalletPassword.value = false
            return address
        }
        _shouldPromptForWalletPassword.value = true
        return null
    }

    fun generateWallet(password: String): String? {
        val creds = ethWalletManager.createWallet(password)
        val address = creds?.address
        if (address != null) {
            credentials = creds
            viewModelScope.launch {
                musicProfileRepository.updateEthereumWalletAddress(address)
                fetchNativeEthBalance(address)
                fetchBalance(address, creds)
            }
            _shouldPromptForWalletPassword.value = false
            return address
        }
        _shouldPromptForWalletPassword.value = true
        return null
    }

    private fun fetchBalance(address: String, creds: Credentials) {
        ethWalletManager.getBalanceForAccount(address, creds)?.thenAccept { balance ->
            _ethBalance.value = balance
        }
    }

    fun getPrivateKey(): String? = credentials?.ecKeyPair?.privateKey?.toString(16)

    private fun fetchNativeEthBalance(address: String) {
        Log.d("EthFetch", "Fetching Sepolia balance for: $address")
        ethWalletManager.getNativeEthBalance(address).thenAccept { balance ->
            Log.d("EthFetch", "Sepolia balance: $balance")
            _nativeEthBalance.value = balance
        }
    }


    fun donateToArtists(artistAddresses: List<String>, amountEth: Double) {
        val creds = credentials ?: return
        if (artistAddresses.isEmpty()) return

        val splitPerArtist = 1000L / artistAddresses.size
        val splits = artistAddresses.map { it to splitPerArtist }.toMutableList()
        if (artistAddresses.size > 1) {
            val remainder = 1000L - splitPerArtist * artistAddresses.size
            splits[splits.size - 1] = splits.last().first to (splitPerArtist + remainder)
        }

        val amountWei = Convert.toWei(amountEth.toString(), Convert.Unit.ETHER).toLong()
        ethWalletManager.donateToArtist(splits, amountWei, creds)
            ?.whenComplete { receipt, throwable ->
                if (throwable != null) {
                    _transactionMessage.value = "Transaction failed: ${throwable.message}"
                } else {
                    _transactionMessage.value = "Transaction successful!"
                }
            }
    }

    fun withdrawFunds() {
        val creds = credentials ?: return
        ethWalletManager.getMoneyFromSmartContract(creds)
            ?.whenComplete { receipt, throwable ->
                if (throwable != null) {
                    val msg = "Withdrawal failed: ${throwable.message}"
                    Log.d("ETH_WITHDRAW", msg, throwable) // <-- This line logs the error with stacktrace
                    _transactionMessage.value = msg
                } else {
                    _transactionMessage.value = "Withdrawal successful!"
                }
            }
    }


    fun dismissWalletDialog() {
        _shouldPromptForWalletPassword.value = false
    }

    fun clearTransactionMessage() {
        _transactionMessage.value = null
    }

}
