package nl.tudelft.trustchain.musicdao

import io.mockk.*
import kotlinx.coroutines.runBlocking
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
    private lateinit var mockBlock: TrustChainBlock
    private lateinit var mockPublicKey: PublicKey

    @Before
    fun setup() {
        mockCommunity = mockk(relaxed = true)
        mockValidator = mockk()
        mockBlock = mockk()
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
        } returns mockBlock

        val result = repository.create(create)
        assertEquals(mockBlock, result)

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
        val mockTrustBlock = mockk<TrustChainBlock>()
        val mockMusicLikeBlock = mockk<MusicLikeBlock>()
        every { mockValidator.validateTransaction(any()) } returns true
        every { mockCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE) } returns listOf(mockTrustBlock)
        every { MusicLikeBlock.fromTrustChainTransaction(any()) } returns mockMusicLikeBlock
        every { mockMusicLikeBlock.likedMusicId } returns "song1"

        val result = repository.get("song1")
        assertEquals(1, result.size)
        assertEquals(mockMusicLikeBlock, result.first())
    }

    @Test
    fun testFilterInvalid() {
        every { mockValidator.validateTransaction(any()) } returns false
        every { mockCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE) } returns listOf(mockk())

        val result = repository.get("invalidSongId")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testGetAllKnownSongLikes() {
        val mockTrustBlock = mockk<TrustChainBlock>()
        val mockMusicLikeBlock = mockk<MusicLikeBlock>()
        every { mockValidator.validateTransaction(any()) } returns true
        every { mockCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE) } returns listOf(mockTrustBlock)
        every { MusicLikeBlock.fromTrustChainTransaction(any()) } returns mockMusicLikeBlock

        val result = repository.getAllKnownSongLikes()
        assertEquals(1, result.size)
        assertEquals(mockMusicLikeBlock, result.first())
    }

    @Test
    fun testGetOrCrawl() =
        runBlocking {
            val mockMusicLikeBlock = mockk<MusicLikeBlock>()
            mockkObject(MusicLikeBlock.Companion)
            every { mockValidator.validateTransaction(any()) } returns true
            every { mockCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE) } returns listOf(mockk())
            every { MusicLikeBlock.fromTrustChainTransaction(any()) } returns mockMusicLikeBlock
            coEvery { mockCommunity.network.getVerifiedByPublicKeyBin(any()) } returns null

            val result = repository.getOrCrawl("song1")
            assertEquals(1, result.size)
        }

    @Test
    fun testToBlock() {
        val transaction = mapOf("likedMusicId" to "id", "name" to "test", "protocolVersion" to "1", "publicKey" to "pk")
        every { mockBlock.transaction } returns transaction
        mockkObject(MusicLikeBlock.Companion)
        val expected = mockk<MusicLikeBlock>()
        every { MusicLikeBlock.fromTrustChainTransaction(transaction) } returns expected

        val result = repository.toBlock(mockBlock)
        assertEquals(expected, result)
    }
}
