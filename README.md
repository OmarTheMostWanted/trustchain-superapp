# TrustChain Super App 
[![Build Status](https://github.com/Tribler/trustchain-superapp/workflows/build/badge.svg)](https://github.com/Tribler/trustchain-superapp/actions) [![ktlint](https://img.shields.io/badge/ktlint%20code--style-%E2%9D%A4-FF4081)](https://pinterest.github.io/ktlint/)

This repository contains a collection of Android apps built on top of [IPv8](https://github.com/Tribler/kotlin-ipv8) (our P2P networking stack) and [TrustChain](https://github.com/Tribler/kotlin-ipv8/blob/master/doc/TrustChainCommunity.md) (a scalable, distributed, pair-wise ledger). All applications are built into a single APK, following the concept of [super apps](https://home.kpmg/xx/en/home/insights/2019/06/super-app-or-super-disruption.html) – an emerging trend that provides an ecosystem for multiple services within a single all-in-one app experience.

## Build Instructions

### Clone
Clone the repository **including the submodule** with the following command:
```
git clone --recurse-submodules <URL>
```

If you have already cloned the repository and forgot to include the `--recurse-submodules` flag, you can initialize the submodule with the following command:
```
git submodule update --init --recursive
```
You can also update the submodule with this command.

### Build
If you want to build an APK, run the following command:
```
./gradlew :app:assembleDebug
```
The resulting APK will be stored in `app/build/outputs/apk/debug/app-debug.apk`.

### Install
You can also build and automatically install the app on all connected Android devices with a single command:
```
./gradlew :app:installDebug
```
*Note: It is required to have an Android device connected with USB debugging enabled before running this command.*

### Check
Run the Gradle check task to verify that the project is correctly set up and that tests pass:
```
./gradlew check
```
*Note: this task is also run on the CI, so ensure that it passes before making a PR.*  

### Tests
Run unit tests:
```
./gradlew test
```

Run instrumented tests:
```
./gradlew connectedAndroidTest
```

### Code style
[Ktlint](https://ktlint.github.io/) is used to enforce a consistent code style across the whole project. It is recommended to install the [ktlint plugin](https://plugins.jetbrains.com/plugin/15057-ktlint) for your IDE to get real-time feedback.

Check code style:
```
./gradlew ktlintCheck
```

Apply linter:
```
./gradlew ktlintFormat
```

---

## Adding Your Own App
If you want to add your own app to the TrustChain Super App, you can follow the tutorial in the [AppTutorial.md](doc/AppTutorial.md) document.

---

## Apps

### Debug

**Debug** shows various information related to connectivity, including:

- The list of bootstrap servers and their health. The server is considered to be alive if we receive a response from it within the last 120 seconds.
- The number of connected peers in the loaded overlays.
- The LAN address estimated from the network interface and the WAN address estimated from the packets received from peers outside of our LAN.
- The public key and member ID (SHA-1 hash of the public key)
- TrustChain statistics (the number of stored blocks and the length of our chain)

<img src="https://raw.githubusercontent.com/Tribler/kotlin-ipv8/master/doc/demo-android-debug.png" width="180">

### MusicDAO
In short, the MusicDAO  is an IPv8 app where users can share and discover tracks on the trustchain. Track streaming, downloading, and seeking interactions are done using JLibtorrent.

A user can publish a Release (which is an album/EP/single/...), after which the app creates a magnet link referring to these audio tracks. Then, the app creates a proposal block for the Trustchain which contains some metadata (release date, title, ...) this metadata is submitted by the user with a dialog. When a signed block is discovered (currently self-signed), the app tries to obtain the file list using JLibtorrent. Each file can be streamed independently by clicking the _play_ button.

<img src="doc/musicdao/screen2.png" width="280"> <img src="doc/musicdao/screen1.png" width="280"> <img src="doc/musicdao/screen3.png" width="280">

**Videos**

Video 1: <a href="doc/musicdao/thesis2.mp4">Load example.</a> This uses a default magnet link for an album that has a decent amount of peers. The user submits the metadata and the block gets proposed and signed. Then playback.

Video 2: <a href="doc/musicdao/thesis3.mp4">Share track.</a> Note: as a fresh magnet link is generated in this video, there is only 1 peer. For this reason, it will be difficult to obtain the metadata of the magnet link (cold start issues) so the video stops there.

## Class of 2025 Spotify 1 Team 1
The two main functionalities added were the leaderboard panel and the Ethereum donation functionality within MusicDAO. These features provide a live leaderboard that ranks songs based on likes and shows popular tags (new features) and enable decentralized donations to multiple artists using a single Ethereum smart contract.

### Ethereum Donation Functionality

#### Overview

MusicDAO allows users to donate Ether (ETH) directly to multiple artists using a custom smart contract, [`MusicDonationSplitter.sol`](src/main/java/nl/tudelft/trustchain/musicdao/core/solidity/MusicDonationSplitter.sol), deployed on the Sepolia test network. Donations can be split among multiple artists in customizable percentages, and artists can withdraw their contract's balance at any time. The functionality allows for configurable percentages, but the current UI only allows an equal split among selected wallets.
#### Wallet 
The wallet file is stored locally and contains the private key encrypted by the local password. It is created by methods `createWallet` or `createWalletFromExistingHexPK` and accessed by `getWalletCredentials`. It allows the user to sign transactions and prove ownership of their Ethereum address.


#### Smart Contract

- **Location:** [`MusicDonationSplitter.sol`](src/main/java/nl/tudelft/trustchain/musicdao/core/solidity/MusicDonationSplitter.sol)
- **Key Features:**
  - Accepts ETH donations and splits them among specified artists.
  - Each artist’s balance is tracked and can be withdrawn.
  - Emits events for donations and withdrawals.

#### Contract Methods

- `donate(address[] artists, uint256[] percentages)`: Accepts ETH and splits it according to the provided percentages (must sum to 1000, i.e., 100%).
- `getBalance(address artist)`: Returns the current balance for an artist.
- `withdrawBalance()`: Allows an artist to withdraw their contract share to their native balance.

### Integration in Android
All Ethereum functionality is handled through the `ETH` panel. A secure environment is enforced, by requesting the password every time the user navigates away from the screen and by having the private key hidden from view.
#### Wallet Management

- **Class:** [`EthereumWalletManager`](src/main/java/nl/tudelft/trustchain/musicdao/core/ethereum/EthereumWalletManager.kt)
- **Features:**
  - Create a new wallet by providing a password or import an existing wallet connecting with the private key. A wallet address is assigned, currently linked to profile's public key.
  - Retrieve wallet credentials from local storage for signing transactions or connecting from different devices (private key).
  - Query native ETH balance and contract balance.
  - Donate ETH to artists by providing the corresponding address.
  - Withdraw the user's accumulated contract balance.

#### Donation Flow

1. **User selects wallet addresses and donation amount.**
2. **App splits the donation**.
3. **App calls** [`donateToArtist`](src/main/java/nl/tudelft/trustchain/musicdao/core/ethereum/EthereumWalletManager.kt) to send the transaction to the smart contract.
4. **Artists can withdraw** their balance using [`getMoneyFromSmartContract`](src/main/java/nl/tudelft/trustchain/musicdao/core/ethereum/EthereumWalletManager.kt).

### Leaderboard
The leaderboard displays the most liked songs along with the like count and the song's top tags (maximum 3). It automatically refreshes driving engagement and showing a song's popularity. Tags are a new useful feature, which adds an extra information granularity that could help the user filter the database based on certain characteristics, but also eliminate unwanted noise introduced by the open and decentralized nature of the application.

- **Data Model:**

	-   **Likes:**  Stored as  `MusicLike`  objects. Queries the `MusicLikeEntity` table.
	-   **Tags:**  Aggregated per song using  `TagCount`. Queries the `MusicTagEntity` table.
	
- **Features:**
	- Like and unlike any downloaded song.
	- Add music genre tags (e.g. "Rock", "Jazz") from an existing array.
	- Play songs directly from the leaderboard.      
	
Likes and tags are stored in the app's SQLite database managed with the Room library. See `CacheDatabase.kt` for full database schema.

### Developer Notes
- The Ethereum wallet files are stored in the app’s private storage.
- Smart contracts are deployed on the Sepolia testnet.
- For more information on how to deploy a smart contract see `DeployContract.kt` for instructions.
