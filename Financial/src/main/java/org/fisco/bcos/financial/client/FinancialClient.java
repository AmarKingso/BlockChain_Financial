package org.fisco.bcos.financial.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fisco.bcos.financial.contract.Financial;
import org.fisco.bcos.financial.contract.Financial.TransactionEventResponse;
import org.fisco.bcos.financial.contract.Financial.TransferEventResponse;
import org.fisco.bcos.financial.contract.Financial.FinancingEventResponse;
import org.fisco.bcos.financial.contract.Financial.SettlementEventResponse;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple1;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class FinancialClient {
	
	static Logger logger = LoggerFactory.getLogger(FinancialClient.class);

	private Web3j web3j;
	
	private Credentials credentials;
	
	private static String adminKey = "";

	public Web3j getWeb3j() {
		return web3j;
	}

	public void setWeb3j(Web3j web3j) {
		this.web3j = web3j;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
	
	public void setAdminKey(String key) {
		adminKey = key;
	}
	
	public static String getAdminKey() {
		return adminKey;
	}
	
	public void recordAssetAddr(String address) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.setProperty("address", address);
		final Resource contractResource = new ClassPathResource("contract.properties");
		FileOutputStream fileOutputStream = new FileOutputStream(contractResource.getFile());
		prop.store(fileOutputStream, "contract address");
	}

	public String loadAssetAddr() throws Exception {
		// load Asset contact address from contract.properties
		Properties prop = new Properties();
		final Resource contractResource = new ClassPathResource("contract.properties");
		prop.load(contractResource.getInputStream());

		String contractAddress = prop.getProperty("address");
		if (contractAddress == null || contractAddress.trim().equals("")) {
			throw new Exception(" load Asset contract address failed, please deploy it first. ");
		}
		logger.info(" load Asset address from contract.properties, address is {}", contractAddress);
		return contractAddress;
	}
	
	public void initialize(String privKey) throws Exception {

		// init the Service
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		Service service = context.getBean(Service.class);
		service.run();

		ChannelEthereumService channelEthereumService = new ChannelEthereumService();
		channelEthereumService.setChannelService(service);
		Web3j web3j = Web3j.build(channelEthereumService, 1);

		// init Credentials
		Credentials credentials = GenCredential.create(privKey);
		setCredentials(credentials);
		setWeb3j(web3j);

		logger.debug(" web3j is " + web3j + " ,credentials is " + credentials);
	}
	
	private static BigInteger gasPrice = new BigInteger("30000000");
	private static BigInteger gasLimit = new BigInteger("30000000");
	
	public void deployAssetAndRecordAddr() {

		try {
			Financial finance = Financial.deploy(web3j, credentials, new StaticGasProvider(gasPrice, gasLimit)).send();
			System.out.println(" deploy Asset success, contract address is " + finance.getContractAddress());

			recordAssetAddr(finance.getContractAddress());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println(" deploy Asset contract failed, error message is  " + e.getMessage());
		}
	}
	
	public boolean issueAccount(String addr, String name, BigInteger property, BigInteger level) {
		try {
			String contractAddress = loadAssetAddr();

			Financial finance = Financial.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = finance.issue(addr, name, property, level).send();
			Tuple1<BigInteger> output = finance.getIssueOutput(receipt);
			if (output.getValue1().compareTo(new BigInteger("0")) == 0) {
				System.out.printf("do not have permission\n");
				return false;
			} 
			else if (output.getValue1().compareTo(new BigInteger("1")) == 0){
				System.out.printf("create a new account!\n");
				return true;
			}
			else{
				System.out.printf("the account existed!\n");
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" issueAccount exception, error message is {}", e.getMessage());
			System.out.printf("issue account failed, error message is %s\n", e.getMessage());
		}
		
		return false;
	}
	
	public boolean acountExist(String addr) {
		try {
			String contractAddress = loadAssetAddr();

			Financial finance = Financial.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = finance.isExist(addr).send();
			Tuple1<Boolean> output = finance.getIsExistOutput(receipt);
			return output.getValue1();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" issueAccount exception, error message is {}", e.getMessage());
			System.out.printf("issue account failed, error message is %s\n", e.getMessage());
		}
		
		return false;
	}
	
	public void transactionFinancial(String receiver, BigInteger amount) {
		try {
			String contractAddress;
			contractAddress = loadAssetAddr();
			
			Financial finance = Financial.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = finance.transaction(receiver, amount).send();
			List<TransactionEventResponse> response = finance.getTransactionEvents(receipt);
			if (!response.isEmpty()) {
				System.out.printf("completing transaction! %s give %s a receipt, amount is %s\n", response.get(0).from, response.get(0).to, response.get(0).amount.toString());
			}
			else {
				System.out.println("receiver doesn't exist.\n");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(" transactionFinancial exception, error message is {}", e.getMessage());
			System.out.printf("transaction failed, error message is %s\n", e.getMessage());
		}		
	}
	
	public void transferFinancial(String receiver, BigInteger amount) {
		try {
			String contractAddress;
			contractAddress = loadAssetAddr();
			
			Financial finance = Financial.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = finance.transfer(receiver, amount).send();
			List<TransferEventResponse> response = finance.getTransferEvents(receipt);
			if (!response.isEmpty()) {
				System.out.printf("Transfer successfully!Generate %d receipt: \n", response.size());
				for(int i = 0; i < response.size(); i++) {
					System.out.printf("The amount of the %dth receipt: %s\n", i, response.get(i).amount.toString());
				}
			}
			else {
				System.out.println("receiver doesn't exist.\n");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(" transferFinancial exception, error message is {}", e.getMessage());
			System.out.printf("transfer failed, error message is %s\n", e.getMessage());
		}		
	}
	
	public void financingFinancial(BigInteger amount) {
		try {
			String contractAddress;
			contractAddress = loadAssetAddr();
			
			Financial finance = Financial.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = finance.financing(amount).send();
			List<FinancingEventResponse> response = finance.getFinancingEvents(receipt);
			if (!response.isEmpty()) {
				if(response.get(0).flag.compareTo(new BigInteger("0")) == 0) {
					System.out.printf("Financing failed! You don't have enough credibility or a receipt from a reputable company.\n"
							+ "Your property is %s.\n", response.get(0).amount);
				}
				else if(response.get(0).flag.compareTo(new BigInteger("1")) == 0) {
					System.out.printf("Financing successfully! You have enough credibility.\n"
							+ "Your property is %s.\n", response.get(0).amount);
				}
				else {
					System.out.printf("Financing successfully! You have enough receipts from a reputable company.\n"
							+ "Your property is %s.\n", response.get(0).amount);
				}
			}
			else {
				System.out.println(" event log not found, maybe transaction not exec.\n");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(" financingFinancial exception, error message is {}", e.getMessage());
			System.out.printf("financing failed, error message is %s\n", e.getMessage());
		}		
	}
	
	public void settlementFinancial(String receiver) {
		try {
			String contractAddress;
			contractAddress = loadAssetAddr();
			
			Financial finance = Financial.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = finance.settlement(receiver).send();
			List<SettlementEventResponse> response = finance.getSettlementEvents(receipt);
			if (!response.isEmpty()) {
				System.out.printf("Successful settlement!Accomplished %d receipt for %s: \n", response.size(), response.get(0).to);
				for(int i = 0; i < response.size(); i++) {
					System.out.printf("The amount of the %dth receipt: %s. Your remaining property: %s.\n", i, response.get(i).amount.toString(), response.get(0).property.toString());
				}
			}
			else {
				System.out.println("receiver doesn't exist.\n");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(" transferFinancial exception, error message is {}", e.getMessage());
			System.out.printf("transfer failed, error message is %s\n", e.getMessage());
		}		
	}
}
