# TrustChain Super App
## Music app
In short, the Music app is an IPv8 app where users can share and discover tracks on the trustchain. Track streaming, downloading, and seeking interactions are done using JLibtorrent.

A user can publish a Release (which is an album/EP/single/...), after which the app creates a magnet link referring to these audio tracks. Then, the app creates a proposal block for the Trustchain which contains some metadata (release date, title, ...) this metadata is submitted by the user with a dialog. When a signed block is discovered (currently self-signed), the app tries to obtain the file list using JLibtorrent. Each file can be streamed independently by clicking the _play_ button.

## Class of 2025 Spotify 1 Team 1
The two main functionalities added were the leaderboard panel and the Ethereum donation functionality within MusicDAO. These features provide a live leaderboard that ranks songs based on likes and shows popular tags (new features) and enable decentralized donations to multiple artists using a single Ethereum smart contract.

### Ethereum Donation Functionality

#### Overview

MusicDAO allows users to donate Ether (ETH) directly to multiple artists using a custom smart contract, [`MusicDonationSplitter.sol`](musicdao/src/main/java/nl/tudelft/trustchain/musicdao/core/solidity/MusicDonationSplitter.sol), deployed on the Sepolia test network. Donations can be split among multiple artists in customizable percentages, and artists can withdraw their contract's balance at any time. The functionality allows for configurable percentages, but the current UI only allows an equal split among selected wallets.
#### Wallet
The wallet file is stored locally and contains the private key encrypted by the local password. It is created by methods `createWallet` or `createWalletFromExistingHexPK` and accessed by `getWalletCredentials`. It allows the user to sign transactions and prove ownership of their Ethereum address.


#### Smart Contract

- **Location:** [`MusicDonationSplitter.sol`](musicdao/src/main/java/nl/tudelft/trustchain/musicdao/core/solidity/MusicDonationSplitter.sol)
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

- **Class:** [`EthereumWalletManager`](musicdao/src/main/java/nl/tudelft/trustchain/musicdao/core/ethereum/EthereumWalletManager.kt)
- **Features:**
  - Create a new wallet by providing a password or import an existing wallet connecting with the private key. A wallet address is assigned, currently linked to profile's public key.
  - Retrieve wallet credentials from local storage for signing transactions or connecting from different devices (private key).
  - Query native ETH balance and contract balance.
  - Donate ETH to artists by providing the corresponding address.
  - Withdraw the user's accumulated contract balance.

#### Donation Flow

1. **User selects wallet addresses and donation amount.**
2. **App splits the donation**.
3. **App calls** [`donateToArtist`](musicdao/src/main/java/nl/tudelft/trustchain/musicdao/core/ethereum/EthereumWalletManager.kt) to send the transaction to the smart contract.
4. **Artists can withdraw** their balance using [`getMoneyFromSmartContract`](musicdao/src/main/java/nl/tudelft/trustchain/musicdao/core/ethereum/EthereumWalletManager.kt).

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
