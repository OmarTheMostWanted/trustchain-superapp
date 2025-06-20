package nl.tudelft.trustchain.musicdao.core.ethereum

import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Convert
import java.math.BigInteger

/**
 * If you want to deploy a new contract, do the following:
 * 1) Modify MusicDonationSplitter.sol
 * 2) Use web3j gradle plugin to generate the java wrapper for interacting with a smart contract:
 * - You can use web3j CLI
 * - Or use a separate gradle project with org.web3j:web3j-gradle-plugin and run generateContractWrappers
 * (this project can not use this plugin, as it is incompatible with Android Gradle Plugin)
 * 3) Put the generated wrapper into the same directory as this file
 * 4) Specify a private key for the account that will deploy the contract (change PUT_YOUR_PRIVATE_KEY_HERE)
 * - Note that you need to have some ETH on the account to pay the gas fee. You can get it from here:
 * https://sepolia-faucet.pk910.de/#/
 * 5) Run this file
 * 6) Copy the address into EthereumWalletManager
 */
fun main() {
    val web3 = Web3j.build(HttpService("https://sepolia.infura.io/v3/6b09f261321a4397a51c79ab490e67ac")) // Artjom's Infura API key

    val privateKeyBigInt = BigInteger("PUT_YOUR_PRIVATE_KEY_HERE", 16)
    val keyPair = ECKeyPair.create(privateKeyBigInt)
    val credentials = Credentials.create(keyPair)

    println(credentials.address)

    val gasProvider = StaticGasProvider(
        Convert.toWei("20", Convert.Unit.GWEI).toBigInteger(),
        BigInteger.valueOf(600_000)
    )

    val contract = MusicDonationSplitter.deploy(
        web3, credentials, gasProvider
    ).send()

    println("Contract deployed at: ${contract.contractAddress}")
}
