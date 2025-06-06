package nl.tudelft.trustchain.musicdao

import io.mockk.*
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.keyvault.PublicKey
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicCommunity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlock
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlockRepository
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlockRepository.Companion.CreateMusicLikeBlock
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlockValidator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MusicLikeBlockRepositoryTest {
    private lateinit var repository: MusicLikeBlockRepository
    private lateinit var mockCommunity: MusicCommunity
    private lateinit var mockValidator: MusicLikeBlockValidator
    private lateinit var mockMusicLikeBlock: MusicLikeBlock
    private lateinit var mockTrustchainBlock: TrustChainBlock
    private lateinit var mockPublicKey: PublicKey

    @Before
    fun setup() {
        mockCommunity = mockk(relaxed = true)
        mockValidator = mockk()
        mockMusicLikeBlock = mockk()
        mockTrustchainBlock = mockk()
        mockPublicKey = mockk()
        every { mockPublicKey.keyToBin() } returns byteArrayOf(0, 0, 0)
        every { mockCommunity.myPeer.publicKey } returns mockPublicKey
        repository = MusicLikeBlockRepository(mockCommunity, mockValidator)
    }

    @Test
    fun testValidateSuccess() {
        val create = CreateMusicLikeBlock("song1")
        val expectedTransaction = slot<Map<String, String>>()

        every { mockValidator.validateTransaction(capture(expectedTransaction)) } returns true
        every {
            mockCommunity.createProposalBlock(
                blockType = MusicLikeBlock.BLOCK_TYPE,
                transaction = any(),
                publicKey = any()
            )
        } returns mockTrustchainBlock

        val result = repository.create(create)
        assertEquals(mockTrustchainBlock, result)

        val transaction = expectedTransaction.captured
        assertEquals(mockPublicKey.keyToBin().toHex(), transaction["publicKey"])
        assertEquals("song1", transaction["likedMusicId"])
        assertEquals(Constants.PROTOCOL_VERSION, transaction["protocolVersion"])
    }

    @Test
    fun testValidateFail() {
        val create = CreateMusicLikeBlock("invalidSong")
        every { mockValidator.validateTransaction(any()) } returns false

        val result = repository.create(create)
        assertNull(result)
    }

    @Test
    fun testMappedBlocks() {
        mockkObject(MusicLikeBlock.Companion)
        every { mockTrustchainBlock.transaction } returns
            mapOf(
                "likedMusicId" to "song1",
                "publicKey" to "pubKey",
                "protocolVersion" to Constants.PROTOCOL_VERSION
            )

        every { mockValidator.validateTransaction(any()) } returns true
        every { mockCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE) } returns listOf(mockTrustchainBlock)
        every { MusicLikeBlock.fromTrustChainTransaction(any()) } returns mockMusicLikeBlock
        every { mockMusicLikeBlock.likedMusicId } returns "song1"

        val result = repository.get("song1")
        assertEquals(1, result.size)
        assertEquals(mockMusicLikeBlock, result.first())
    }

    @Test
    fun testFilterInvalid() {
        mockkObject(MusicLikeBlock.Companion)
        every { mockValidator.validateTransaction(any()) } returns false
        every { mockCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE) } returns listOf(mockTrustchainBlock)
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
    fun testGetAllKnownSongLikes() {
        mockkObject(MusicLikeBlock.Companion)
        val mockMusicLikeBlock = mockk<MusicLikeBlock>()
        every { mockValidator.validateTransaction(any()) } returns true
        every { mockCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE) } returns listOf(mockTrustchainBlock)
        every { MusicLikeBlock.fromTrustChainTransaction(any()) } returns mockMusicLikeBlock
        every { mockTrustchainBlock.transaction } returns
            mapOf(
                "likedMusicId" to "song1",
                "publicKey" to "pubKey",
                "protocolVersion" to Constants.PROTOCOL_VERSION
            )

        val result = repository.getAllKnownSongLikes()
        assertEquals(1, result.size)
        assertEquals(mockMusicLikeBlock, result.first())
    }

    @Test
    fun testToBlock() {
        val transaction = mapOf("likedMusicId" to "id", "name" to "test", "protocolVersion" to "1", "publicKey" to "pk")
        every { mockTrustchainBlock.transaction } returns transaction
        mockkObject(MusicLikeBlock.Companion)
        val expected = mockk<MusicLikeBlock>()
        every { MusicLikeBlock.fromTrustChainTransaction(transaction) } returns expected

        val result = repository.toBlock(mockTrustchainBlock)
        assertEquals(expected, result)
    }
}
