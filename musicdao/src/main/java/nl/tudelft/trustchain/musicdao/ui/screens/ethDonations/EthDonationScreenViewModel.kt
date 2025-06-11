package nl.tudelft.trustchain.musicdao.ui.screens.ethDonations

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import nl.tudelft.trustchain.musicdao.core.ethereum.EthereumWalletManager
import javax.inject.Inject

@HiltViewModel
class EthDonationScreenViewModel @Inject constructor(private val ethWalletManager: EthereumWalletManager): ViewModel() {

    fun connectWallet(privateKey: String, password: String): String? {
        val address = ethWalletManager.createWalletFromExistingHexPK(privateKey, password)?.address
        Log.d("ETHSmartContracts","Generated ETH wallet from existing PK with address: $address}");
        return address
    }

    fun generateWallet(password: String): String? {
        val address = ethWalletManager.createWallet(password)?.address
        Log.d("ETHSmartContracts","Generated a new ETH wallet with address: $address}");
        return address
    }
}
