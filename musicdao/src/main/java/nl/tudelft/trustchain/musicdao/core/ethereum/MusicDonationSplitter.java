package nl.tudelft.trustchain.musicdao.core.ethereum;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.9.8.
 */
@SuppressWarnings("rawtypes")
public class MusicDonationSplitter extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b506106138061001c5f395ff3fe608060405260043610610041575f3560e01c80635fd8c710146100975780638bcd0915146100ad578063e3e93a4a146100c0578063f8b2cb4f146100fd575f5ffd5b366100935760405162461bcd60e51b815260206004820152601a60248201527f53656e64204554482076696120646f6e6174652829206f6e6c7900000000000060448201526064015b60405180910390fd5b5f5ffd5b3480156100a2575f5ffd5b506100ab610131565b005b6100ab6100bb3660046104cd565b610255565b3480156100cb575f5ffd5b506100eb6100da366004610539565b5f6020819052908152604090205481565b60405190815260200160405180910390f35b348015610108575f5ffd5b506100eb610117366004610539565b6001600160a01b03165f9081526020819052604090205490565b335f90815260208190526040902054806101835760405162461bcd60e51b81526020600482015260136024820152724e6f7468696e6720746f20776974686472617760681b604482015260640161008a565b6040515f908190339084908381818185875af1925050503d805f81146101c4576040519150601f19603f3d011682016040523d82523d5f602084013e6101c9565b606091505b50915091508161020d5760405162461bcd60e51b815260206004820152600f60248201526e151c985b9cd9995c8819985a5b1959608a1b604482015260640161008a565b335f818152602081815260408083209290925590518581527f26536799ace2c3dbe12e638ec3ade6b4173dcf1289be0a58d51a5003015649bd910160405180910390a2505050565b5f34116102925760405162461bcd60e51b815260206004820152600b60248201526a139bc8115512081cd95b9d60aa1b604482015260640161008a565b8281146102d55760405162461bcd60e51b81526020600482015260116024820152704d69736d6174636865642061727261797360781b604482015260640161008a565b5f805b848110156103f7575f8484838181106102f3576102f3610566565b90506020020135116103375760405162461bcd60e51b815260206004820152600d60248201526c125b9d985b1a59081cdc1b1a5d609a1b604482015260640161008a565b5f6103e885858481811061034d5761034d610566565b905060200201353461035f919061058e565b61036991906105ab565b9050805f5f89898681811061038057610380610566565b90506020020160208101906103959190610539565b6001600160a01b03166001600160a01b031681526020019081526020015f205f8282546103c291906105ca565b9091555085905084838181106103da576103da610566565b90506020020135836103ec91906105ca565b9250506001016102d8565b50806103e8146104495760405162461bcd60e51b815260206004820152601960248201527f53706c697473206d7573742073756d20746f203130302e302500000000000000604482015260640161008a565b60405134815233907f264f630d9efa0d07053a31163641d9fcc0adafc9d9e76f1c37c2ce3a558d2c529060200160405180910390a25050505050565b5f5f83601f840112610495575f5ffd5b50813567ffffffffffffffff8111156104ac575f5ffd5b6020830191508360208260051b85010111156104c6575f5ffd5b9250929050565b5f5f5f5f604085870312156104e0575f5ffd5b843567ffffffffffffffff8111156104f6575f5ffd5b61050287828801610485565b909550935050602085013567ffffffffffffffff811115610521575f5ffd5b61052d87828801610485565b95989497509550505050565b5f60208284031215610549575f5ffd5b81356001600160a01b038116811461055f575f5ffd5b9392505050565b634e487b7160e01b5f52603260045260245ffd5b634e487b7160e01b5f52601160045260245ffd5b80820281158282048414176105a5576105a561057a565b92915050565b5f826105c557634e487b7160e01b5f52601260045260245ffd5b500490565b808201808211156105a5576105a561057a56fea2646970667358221220732818f0630090796aa4529a95e314f0355e3ba3f40a020bbaec491292d30e9164736f6c634300081d0033";

    public static final String FUNC_ARTISTBALANCES = "artistBalances";

    public static final String FUNC_DONATE = "donate";

    public static final String FUNC_GETBALANCE = "getBalance";

    public static final String FUNC_WITHDRAWBALANCE = "withdrawBalance";

    public static final Event DONATIONRECEIVED_EVENT = new Event("DonationReceived",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event FUNDSDISTRIBUTED_EVENT = new Event("FundsDistributed",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected MusicDonationSplitter(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected MusicDonationSplitter(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected MusicDonationSplitter(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected MusicDonationSplitter(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<DonationReceivedEventResponse> getDonationReceivedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DONATIONRECEIVED_EVENT, transactionReceipt);
        ArrayList<DonationReceivedEventResponse> responses = new ArrayList<DonationReceivedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DonationReceivedEventResponse typedResponse = new DonationReceivedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.donor = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DonationReceivedEventResponse getDonationReceivedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DONATIONRECEIVED_EVENT, log);
        DonationReceivedEventResponse typedResponse = new DonationReceivedEventResponse();
        typedResponse.log = log;
        typedResponse.donor = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<DonationReceivedEventResponse> donationReceivedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDonationReceivedEventFromLog(log));
    }

    public Flowable<DonationReceivedEventResponse> donationReceivedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DONATIONRECEIVED_EVENT));
        return donationReceivedEventFlowable(filter);
    }

    public static List<FundsDistributedEventResponse> getFundsDistributedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(FUNDSDISTRIBUTED_EVENT, transactionReceipt);
        ArrayList<FundsDistributedEventResponse> responses = new ArrayList<FundsDistributedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            FundsDistributedEventResponse typedResponse = new FundsDistributedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.artist = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static FundsDistributedEventResponse getFundsDistributedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(FUNDSDISTRIBUTED_EVENT, log);
        FundsDistributedEventResponse typedResponse = new FundsDistributedEventResponse();
        typedResponse.log = log;
        typedResponse.artist = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<FundsDistributedEventResponse> fundsDistributedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getFundsDistributedEventFromLog(log));
    }

    public Flowable<FundsDistributedEventResponse> fundsDistributedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(FUNDSDISTRIBUTED_EVENT));
        return fundsDistributedEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> artistBalances(String param0) {
        final Function function = new Function(FUNC_ARTISTBALANCES,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> donate(List<String> artists, List<BigInteger> percentages, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_DONATE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                                org.web3j.abi.datatypes.Address.class,
                                org.web3j.abi.Utils.typeMap(artists, org.web3j.abi.datatypes.Address.class)),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                                org.web3j.abi.datatypes.generated.Uint256.class,
                                org.web3j.abi.Utils.typeMap(percentages, org.web3j.abi.datatypes.generated.Uint256.class))),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<BigInteger> getBalance(String artist) {
        final Function function = new Function(FUNC_GETBALANCE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, artist)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawBalance() {
        final Function function = new Function(
                FUNC_WITHDRAWBALANCE,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static MusicDonationSplitter load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new MusicDonationSplitter(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static MusicDonationSplitter load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new MusicDonationSplitter(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static MusicDonationSplitter load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new MusicDonationSplitter(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static MusicDonationSplitter load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new MusicDonationSplitter(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<MusicDonationSplitter> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(MusicDonationSplitter.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<MusicDonationSplitter> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MusicDonationSplitter.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<MusicDonationSplitter> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(MusicDonationSplitter.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<MusicDonationSplitter> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MusicDonationSplitter.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class DonationReceivedEventResponse extends BaseEventResponse {
        public String donor;

        public BigInteger amount;
    }

    public static class FundsDistributedEventResponse extends BaseEventResponse {
        public String artist;

        public BigInteger amount;
    }
}
