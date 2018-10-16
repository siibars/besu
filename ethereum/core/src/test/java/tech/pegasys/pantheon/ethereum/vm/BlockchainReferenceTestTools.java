package tech.pegasys.pantheon.ethereum.vm;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockImporter;
import tech.pegasys.pantheon.ethereum.core.MutableWorldState;
import tech.pegasys.pantheon.ethereum.mainnet.HeaderValidationMode;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSpec;
import tech.pegasys.pantheon.ethereum.rlp.RLPException;
import tech.pegasys.pantheon.testutil.JsonTestParameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;

public class BlockchainReferenceTestTools {
  private static final ReferenceTestProtocolSchedules REFERENCE_TEST_PROTOCOL_SCHEDULES =
      ReferenceTestProtocolSchedules.create();

  private static final List<String> NETWORKS_TO_RUN;

  static {
    final String networks =
        System.getProperty(
            "test.ethereum.blockchain.eips",
            "FrontierToHomesteadAt5,HomesteadToEIP150At5,HomesteadToDaoAt5,EIP158ToByzantiumAt5,"
                + "Frontier,Homestead,EIP150,EIP158,Byzantium,Constantinople");
    NETWORKS_TO_RUN = Arrays.asList(networks.split(","));
  }

  private static final JsonTestParameters<?, ?> params =
      JsonTestParameters.create(BlockchainReferenceTestCaseSpec.class)
          .generator(
              (testName, spec, collector) -> {
                final String eip = spec.getNetwork();
                collector.add(testName + "[" + eip + "]", spec, NETWORKS_TO_RUN.contains(eip));
              });

  static {
    if (NETWORKS_TO_RUN.isEmpty()) {
      params.blacklistAll();
    }

    // TODO: Determine and implement cross-chain validation prevention.
    params.blacklist(
        "ChainAtoChainB_BlockHash_(Frontier|Homestead|EIP150|EIP158|Byzantium|Constantinople)");
    // Known bad test.
    params.blacklist("RevertPrecompiledTouch_d0g0v0_(EIP158|Byzantium)");

    // Consumes a huge amount of memory
    params.blacklist("static_Call1MB1024Calldepth_d1g0v0_(Byzantium|Constantinople)");

    // Pantheon is incorrectly rejecting Uncle block timestamps in the future
    params.blacklist("futureUncleTimestampDifficultyDrop2");
    params.blacklist("futureUncleTimestampDifficultyDrop");

    // Needs investigation
    params.blacklist("RevertInCreateInInit_d0g0v0_Byzantium");
    params.blacklist("RevertInCreateInInit_d0g0v0_Constantinople");

    // Constantinople failures to investigate
    params.blacklist("badOpcodes_d115g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024PreCalls_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBombPreCall_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Callcode1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Callcode1024OOG_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Delegatecall1024OOG_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Callcode1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024PreCalls_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionStorage_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionStorage_d1g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("create2collisionStorage_d2g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("RevertInCreateInInitCreate2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CreateMessageReverted_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist(
        "returndatasize_following_successful_create_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CreateMessageRevertedOOGInInit_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist(
        "returndatacopy_0_0_following_successful_create_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024OOG_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024PreCalls_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBombPreCall_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Delegatecall1024_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024BalanceTooLow_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1MB1024Calldepth_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsThenRevert_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsDepthThenRevert2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsDepthThenRevert_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsDepthThenRevert3_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopDelegateCallsDepthThenRevert_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("LoopCallsThenRevert_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("Call1024OOG_d0g1v0_Constantinople\\[Constantinople\\]");
    params.blacklist("static_Call1024PreCalls2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("ABAcalls2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBombLog2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBomb0_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBombLog_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBomb2_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBomb3_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("ABAcalls3_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist("CallRecursiveBomb1_d0g0v0_Constantinople\\[Constantinople\\]");
    params.blacklist(
        "CallRecursiveBomb0_OOG_atMaxCallDepth_d0g0v0_Constantinople\\[Constantinople\\]");
  }

  public static Collection<Object[]> generateTestParametersForConfig(final String[] filePath) {
    return params.generate(filePath);
  }

  public static void executeTest(final BlockchainReferenceTestCaseSpec spec) {
    final MutableWorldState worldState =
        spec.getWorldStateArchive().getMutable(spec.getGenesisBlockHeader().getStateRoot());
    final BlockHeader genesisBlockHeader = spec.getGenesisBlockHeader();
    assertThat(worldState.rootHash()).isEqualTo(genesisBlockHeader.getStateRoot());

    final ProtocolSchedule<Void> schedule =
        REFERENCE_TEST_PROTOCOL_SCHEDULES.getByName(spec.getNetwork());

    final MutableBlockchain blockchain = spec.getBlockchain();
    final ProtocolContext<Void> context = spec.getProtocolContext();

    for (final BlockchainReferenceTestCaseSpec.CandidateBlock candidateBlock :
        spec.getCandidateBlocks()) {
      if (!candidateBlock.isExecutable()) {
        return;
      }

      try {
        final Block block = candidateBlock.getBlock();

        final ProtocolSpec<Void> protocolSpec =
            schedule.getByBlockNumber(block.getHeader().getNumber());
        final BlockImporter<Void> blockImporter = protocolSpec.getBlockImporter();
        final HeaderValidationMode validationMode =
            "NoProof".equalsIgnoreCase(spec.getSealEngine())
                ? HeaderValidationMode.LIGHT
                : HeaderValidationMode.FULL;
        final boolean imported =
            blockImporter.importBlock(context, block, validationMode, validationMode);

        assertThat(imported).isEqualTo(candidateBlock.isValid());
      } catch (final RLPException e) {
        Assert.assertFalse(candidateBlock.isValid());
      }
    }

    assertThat(blockchain.getChainHeadHash()).isEqualTo(spec.getLastBlockHash());
  }
}
