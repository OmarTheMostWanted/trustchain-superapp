package nl.tudelft.trustchain.musicdao.ui.screens.ethDonations

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nl.tudelft.trustchain.musicdao.core.cache.entities.ArtistEntity

@Composable
fun EthDonationScreen(navController: NavController, viewModel: EthDonationScreenViewModel) {
    val otherArtists by viewModel.otherArtists.collectAsState()
    val shouldPromptForWalletPassword by viewModel.shouldPromptForWalletPassword.collectAsState()
    val context = LocalContext.current
    val transactionMessage by viewModel.transactionMessage.collectAsState()

    if (shouldPromptForWalletPassword) {
        MultiOptionWalletDialog(viewModel, navController)
    }

    LaunchedEffect(transactionMessage) {
        transactionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearTransactionMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Account details: all wallet/account info and actions here
        WalletAccountDetails(viewModel = viewModel)

        DonateAndWithdrawSection(
            otherArtists = otherArtists,
            onDonate = { selectedArtists, eth ->
                viewModel.donateToArtists(
                    selectedArtists.map { it.ethereumAddress!! },
                    eth
                )
            },
            onWithdraw = { viewModel.withdrawFunds() }
        )

    }
}

@Composable
fun MultiOptionWalletDialog(viewModel: EthDonationScreenViewModel, navController: NavController) {
    var mode by remember { mutableStateOf("unlock") } // "unlock", "connect", "generate"
    var unlockPassword by remember { mutableStateOf("") }
    var connectPrivateKey by remember { mutableStateOf("") }
    var connectPassword by remember { mutableStateOf("") }
    var generatePassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { viewModel.dismissWalletDialog()
            navController.navigate("home") },
        title = { Text("Wallet Configuration") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Vertically stacked mode buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { mode = "unlock" },
                        enabled = mode != "unlock",
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Unlock Existing Wallet") }
                    Button(
                        onClick = { mode = "connect" },
                        enabled = mode != "connect",
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Connect with Private Key") }
                    Button(
                        onClick = { mode = "generate" },
                        enabled = mode != "generate",
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Generate New Wallet") }
                }
                // Only a small space below mode buttons
                when (mode) {
                    "unlock" -> {
                        OutlinedTextField(
                            value = unlockPassword,
                            onValueChange = { unlockPassword = it; errorText = null },
                            label = { Text("Wallet Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                    "connect" -> {
                        // Tightly group the two fields
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = connectPrivateKey,
                                onValueChange = { connectPrivateKey = it; errorText = null },
                                label = { Text("Private Key") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = connectPassword,
                                onValueChange = { connectPassword = it; errorText = null },
                                label = { Text("Set Wallet Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                    "generate" -> {
                        OutlinedTextField(
                            value = generatePassword,
                            onValueChange = { generatePassword = it; errorText = null },
                            label = { Text("Set Wallet Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
                if (errorText != null) {
                    Text(
                        errorText!!,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            when (mode) {
                "unlock" -> Button(
                    onClick = {
                        val result = viewModel.onWalletPasswordEntered(unlockPassword)
                        if (!result) {
                            errorText = "Invalid password. Try again."
                        }
                        unlockPassword = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Unlock") }
                "connect" -> Button(
                    onClick = {
                        val addr = viewModel.connectWallet(connectPrivateKey, connectPassword)
                        if (addr == null) {
                            errorText = "Failed to connect. Check your private key and password."
                        }
                        connectPrivateKey = ""; connectPassword = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Connect") }
                "generate" -> Button(
                    onClick = {
                        val addr = viewModel.generateWallet(generatePassword)
                        if (addr == null) {
                            errorText = "Failed to generate wallet."
                        }
                        generatePassword = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Generate") }
            }
        }
    )
}

@Composable
fun WalletAccountDetails(viewModel: EthDonationScreenViewModel) {
    val walletAddress by viewModel.myWalletAddress.collectAsState()
    val ethBalance by viewModel.ethBalance.collectAsState()
    var showPrivateKey by remember { mutableStateOf(false) }
    val nativeEthBalance by viewModel.nativeEthBalance.collectAsState()
    val privateKey = viewModel.getPrivateKey()
    val clipboardManager = LocalClipboardManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Wallet Address: ${walletAddress ?: "Not connected"}",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "ETH Balance: ${
                nativeEthBalance?.toBigDecimal()
                    ?.movePointLeft(18)
                    ?.setScale(6, java.math.RoundingMode.DOWN)
                    ?.toPlainString() ?: "Not fetched"
            } ETH",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Contract ETH Balance: ${ethBalance?.toBigDecimal()?.movePointLeft(18)?.setScale(6, java.math.RoundingMode.DOWN)?.toPlainString() ?: "Locked (Must log in)"} ETH",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (privateKey != null) {
            if (showPrivateKey) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = privateKey,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { clipboardManager.setText(AnnotatedString(privateKey)) }) {
                        Text("Copy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { showPrivateKey = false }) {
                        Text("Hide")
                    }
                }
            } else {
                Button(onClick = { showPrivateKey = true }) {
                    Text("Show Private Key")
                }
            }
        }
    }
}

@Composable
fun DonateAndWithdrawSection(
    otherArtists: List<ArtistEntity>,
    onDonate: (List<ArtistEntity>, Double) -> Unit,
    onWithdraw: () -> Unit
) {
    var selectedArtistIndices by remember { mutableStateOf(setOf<Int>()) }
    var amountEth by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Donate to other artists", style = MaterialTheme.typography.h6)

        if (otherArtists.isEmpty()) {
            Text("No other artists available.")
        } else {
            MultiSelectDropdown(
                items = otherArtists.map { it.ethereumAddress ?: "" },
                selectedIndices = selectedArtistIndices,
                onSelectionChanged = { selectedArtistIndices = it },
                label = "Select wallet addresses"
            )

            OutlinedTextField(
                value = amountEth,
                onValueChange = { amountEth = it },
                label = { Text("Amount (ETH)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                enabled = amountEth.toDoubleOrNull() != null
                    && amountEth.toDoubleOrNull()!! > 0
                    && selectedArtistIndices.isNotEmpty(),
                onClick = {
                    val selectedArtists = selectedArtistIndices.map { otherArtists[it] }
                    val eth = amountEth.toDoubleOrNull() ?: 0.0
                    onDonate(selectedArtists, eth)
                    amountEth = ""
                }
            ) { Text("Donate") }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Button(onClick = { onWithdraw() }, modifier = Modifier.fillMaxWidth()) {
            Text("Withdraw My Funds")
        }
    }
}

@Composable
fun MultiSelectDropdown(
    items: List<String>,
    selectedIndices: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit,
    label: String = "Select wallet addresses"
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(label)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(onClick = { /* Do nothing, just checkbox */ }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedIndices.contains(index),
                            onCheckedChange = {
                                val newSet = selectedIndices.toMutableSet()
                                if (it) newSet.add(index) else newSet.remove(index)
                                onSelectionChanged(newSet)
                            }
                        )
                        Text(item)
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    artists: List<ArtistEntity>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedArtist = artists.getOrNull(selectedIndex)
    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedArtist?.ethereumAddress ?: "Select artist")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            artists.forEachIndexed { index, artist ->
                DropdownMenuItem(onClick = {
                    onSelected(index)
                    expanded = false
                }) {
                    Text(artist.ethereumAddress ?: "")
                }
            }
        }
    }
}
