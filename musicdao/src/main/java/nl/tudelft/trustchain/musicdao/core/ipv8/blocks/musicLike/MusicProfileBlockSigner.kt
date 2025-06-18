package nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike

import nl.tudelft.ipv8.attestation.trustchain.BlockSigner
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicCommunity
import javax.inject.Inject

<<<<<<< HEAD:musicdao/src/main/java/nl/tudelft/trustchain/musicdao/core/ipv8/blocks/musicLike/MusicLikeBlockSigner.kt
class MusicLikeBlockSigner
    @Inject
    constructor(
        val musicCommunity: MusicCommunity
    ) : BlockSigner {
        override fun onSignatureRequest(block: TrustChainBlock) {
            musicCommunity.createAgreementBlock(block, mapOf<Any?, Any?>())
        }

        companion object {
            const val BLOCK_TYPE = MusicLikeBlock.BLOCK_TYPE
        }
    }
=======
class MusicProfileBlockSigner
@Inject
constructor(val musicCommunity: MusicCommunity) :
    BlockSigner {
    override fun onSignatureRequest(block: TrustChainBlock) {
        musicCommunity.createAgreementBlock(block, mapOf<Any?, Any?>())
    }
    companion object {
        const val BLOCK_TYPE = MusicProfile.BLOCK_TYPE
    }
}
>>>>>>> master:musicdao/src/main/java/nl/tudelft/trustchain/musicdao/core/ipv8/blocks/musicLike/MusicProfileBlockSigner.kt
