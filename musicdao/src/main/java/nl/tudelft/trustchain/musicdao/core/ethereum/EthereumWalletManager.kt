package nl.tudelft.trustchain.musicdao.core.ethereum

import android.content.Context
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.WalletUtils
import org.web3j.crypto.exception.CipherException
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Convert
import java.io.File
import java.math.BigInteger
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * Creates and looks-up credentials for Ethereum wallets.
 */
class EthereumWalletManager @Inject constructor(context: Context) {


    private val walletFileDirectory = File(context.filesDir,  "ETHWallet")

    //TODO: Move them to be read from a config instead of being hard-coded
    private val SMART_CONTRACT_ADDRESS = "0x1f4571a069e1bad7e63674f541764cee1737de2a" //A contract has been deployed to this address
    private val SEPOLIA_NODE_API_URL = "https://sepolia.infura.io/v3/6b09f261321a4397a51c79ab490e67ac" //Artjom's Infura API key
    private val web3 = Web3j.build(HttpService(SEPOLIA_NODE_API_URL))
    private val gasProvider = getGasProvider()

    init {
        // Ensure the wallet directory exists
        walletFileDirectory.mkdir()
    }

    /**
     * Makes a new local wallet encrypted by the local password that stores the given private key.
     * Since ETH address is fully determined by the PK, there is no need to provide address separately.
     * WILL OVERWRITE ANY EXISTING WALLET
     */
    public fun createWalletFromExistingHexPK(privateKeyInHex: String, localPassword: String): Credentials? {
        val credentials = getCredentialsFromHexPK(privateKeyInHex)

        clearWalletDirectory()
        if (credentials != null) {
            try {
                WalletUtils.generateWalletFile(localPassword, credentials.ecKeyPair, walletFileDirectory, false)
            } catch (e: CipherException) {
                return null
            }
        }
        return credentials
    }

    /**
     * Creates a new wallet locally encrypted by the given password.
     * WILL OVERWRITE ANY EXISTING WALLET
     */
    public fun createWallet(localPassword: String): Credentials? {
        clearWalletDirectory()
        WalletUtils.generateLightNewWalletFile(localPassword, walletFileDirectory)
        return getWalletCredentials(localPassword)
    }

    /**
     * Checks if wallet has been instantiated
     */
    public fun doesWalletExists(): Boolean {
        return walletFileDirectory.listFiles()?.isNotEmpty() ?: false
    }

    /**
     * Gets credentials of a previously stored local wallet.
     * Returns null if the wallet does not exist or if password is wrong.
     */
    public fun getWalletCredentials(localPassword: String): Credentials? {
        if (!doesWalletExists()) {
            return null
        }
        val credentials: Credentials?;
        // Will return null if the password is incorrect
        try {
            credentials = WalletUtils.loadCredentials(localPassword, walletFileDirectory.listFiles()!!.first())
        } catch (e: CipherException) {
            return null
        }
        return credentials
    }

    private fun getCredentialsFromHexPK(hexPK: String): Credentials? {
        val privateKeyBigInt = BigInteger(hexPK, 16)
        val keyPair = ECKeyPair.create(privateKeyBigInt) ?: return null
        return Credentials.create(keyPair)
    }

    private fun clearWalletDirectory() {
        for (file in walletFileDirectory.listFiles()!!) file.delete()
    }


    /**
     * Functions for interacting with the the smart contract.
     */
    private fun getGasProvider(): StaticGasProvider {
        //TODO: When actually deploying, switch to a dynamic gas provider
        return StaticGasProvider(
            Convert.toWei("20", Convert.Unit.GWEI).toBigInteger(),
            BigInteger.valueOf(600_000)
            )
    }

    /**
     * Returns the current balance for the account on the smart contract.
     */
    public fun getBalanceForAccount(address: String, credentials: Credentials): CompletableFuture<BigInteger>? {
        val contract = MusicDonationSplitter.load(SMART_CONTRACT_ADDRESS, web3, credentials, gasProvider)
        return contract.getBalance(address).sendAsync()
    }

    /**
     * Send a donation to artists. The split consists of pairs (Address, Percentage).
     * The sum of the percentages should be 1000.
     * Donation amount is in wei.
     */
    public fun donateToArtist(splits: List<Pair<String, Long>>, donationAmount: Long, credentials: Credentials): CompletableFuture<TransactionReceipt>? {
        val contract = MusicDonationSplitter.load(SMART_CONTRACT_ADDRESS, web3, credentials, gasProvider)
        return contract.donate(splits.map { it.first }, splits.map { BigInteger.valueOf(it.second) }, BigInteger.valueOf(donationAmount)).sendAsync()
    }

    /**
     * Transfers balance from the smart contract to the account requesting the transfer.
     * Only transfers the amount meant for the account requesting the transfer.
     */
    public fun getMoneyFromSmartContract(credentials: Credentials): CompletableFuture<TransactionReceipt>? {
        val contract = MusicDonationSplitter.load(SMART_CONTRACT_ADDRESS, web3, credentials, gasProvider)
        return contract.withdrawBalance().sendAsync()
    }




}
