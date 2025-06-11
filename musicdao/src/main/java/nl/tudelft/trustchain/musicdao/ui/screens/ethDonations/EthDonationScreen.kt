package nl.tudelft.trustchain.musicdao.ui.screens.ethDonations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun EthDonationScreen(navController: NavController, viewModel: EthDonationScreenViewModel) {
    var showConnectDialog by remember { mutableStateOf<Boolean>(false) }
    var showGenerateDialog by remember { mutableStateOf<Boolean>(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = { showConnectDialog = true }) {
            Text("Connect an ETH Wallet")
        }

        Button(onClick = { showGenerateDialog = true }) {
            Text("Generate a New ETH Wallet")
        }
    }

    if (showConnectDialog) {
        ConnectWalletDialog(
            onDismiss = { showConnectDialog = false },
            onConnect = { privateKey, password ->
                viewModel.connectWallet(privateKey, password)
                showConnectDialog = false
            }
        )
    }

    if (showGenerateDialog) {
        GenerateWalletDialog(
            onDismiss = { showGenerateDialog = false },
            onGenerate = { password ->
                viewModel.generateWallet(password)
                showGenerateDialog = false
            }
        )
    }
}

@Composable
fun ConnectWalletDialog(
    onDismiss: () -> Unit,
    onConnect: (privateKey: String, password: String) -> Unit
) {
    var privateKey by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect Wallet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = privateKey,
                    onValueChange = { privateKey = it },
                    label = { Text("Private Key") }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConnect(privateKey, password) }) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun GenerateWalletDialog(
    onDismiss: () -> Unit,
    onGenerate: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Wallet") },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
        },
        confirmButton = {
            Button(onClick = { onGenerate(password) }) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
