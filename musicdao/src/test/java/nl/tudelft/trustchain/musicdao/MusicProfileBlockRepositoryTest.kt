package nl.tudelft.trustchain.musicdao

import io.mockk.*
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.keyvault.PublicKey
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicCommunity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfile
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfileBlockRepository
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfileBlockRepository.Companion.MusicProfileCompanion
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfileBlockValidator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MusicProfileBlockRepositoryTest {
    private lateinit var repository: MusicProfileBlockRepository
    private lateinit var mockCommunity: MusicCommunity
    private lateinit var mockValidator: MusicProfileBlockValidator
    private lateinit var mockMusicProfile: MusicProfile
    private lateinit var mockTrustchainBlock: TrustChainBlock
    private lateinit var mockPublicKey: PublicKey

    @Before
    fun setup() {
        mockCommunity = mockk(relaxed = true)
        mockValidator = mockk()
        mockMusicProfile = mockk()
        mockTrustchainBlock = mockk()
        mockPublicKey = mockk()
        every { mockPublicKey.keyToBin() } returns byteArrayOf(0, 0, 0)
        every { mockCommunity.myPeer.publicKey } returns mockPublicKey
        repository = MusicProfileBlockRepository(mockCommunity, mockValidator)
    }

    @Test
    fun testValidateSuccess() {
        val songs = listOf("song1")
        val tags = emptyMap<String, List<String>>()
        val companion =
            MusicProfileCompanion(
                songs,
                tags,
                ethereumWalletAddress = ""
            )
        val expectedTransaction = slot<Map<String, String>>()

        every { mockValidator.validateTransaction(capture(expectedTransaction)) } returns true
        every {
            mockCommunity.createProposalBlock(
                blockType = MusicProfile.BLOCK_TYPE,
                transaction = any(),
                publicKey = any()
            )
        } returns mockTrustchainBlock

        val result = repository.create(companion)
        assertEquals(mockTrustchainBlock, result)

        val transaction = expectedTransaction.captured
        assertEquals(mockPublicKey.keyToBin().toHex(), transaction["publicKey"])
        assertEquals(Constants.PROTOCOL_VERSION, transaction["protocolVersion"])
    }

    @Test
    fun testValidateFail() {
        val songs = listOf("invalidSong")
        val tags = emptyMap<String, List<String>>()
        val create = MusicProfileCompanion(songs, tags, "")
        every { mockValidator.validateTransaction(any()) } returns false

        val result = repository.create(create)
        assertNull(result)
    }

    @Test
    fun testMappedBlocks() {
        mockkObject(MusicProfile)
        every { mockTrustchainBlock.transaction } returns
            mapOf(
                "likedSongs" to listOf("song1"),
                "publicKey" to "pubKey",
                "protocolVersion" to Constants.PROTOCOL_VERSION
            )

        every { mockValidator.validateTransaction(any()) } returns true
        every { mockCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE) } returns listOf(mockTrustchainBlock)
        every { MusicProfile.fromTrustChainTransaction(any()) } returns mockMusicProfile
        every { mockMusicProfile.likedSongs } returns listOf("song1")

        val result = repository.get("song1")
        assertEquals(1, result.size)
        assertEquals(mockMusicProfile.likedSongs, result.first().likedSongs)
    }

    @Test
    fun testFilterInvalid() {
        mockkObject(MusicProfile)
        every { mockValidator.validateTransaction(any()) } returns false
        every { mockCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE) } returns listOf(mockTrustchainBlock)
        every { mockTrustchainBlock.transaction } returns
            mapOf(
                "likedMusicId" to "invalidSong",
                "publicKey" to "pubKey",
                "protocolVersion" to Constants.PROTOCOL_VERSION
            )

        val result = repository.get("invalidSong")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testToBlock() {
        val transaction = mapOf("likedMusicId" to "id", "name" to "test", "protocolVersion" to "1", "publicKey" to "pk")
        every { mockTrustchainBlock.transaction } returns transaction
        mockkObject(MusicProfile)
        val expected = mockk<MusicProfile>()
        every { MusicProfile.fromTrustChainTransaction(transaction) } returns expected

        val result = repository.toBlock(mockTrustchainBlock)
        assertEquals(expected, result)
    }
}
