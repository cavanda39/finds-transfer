package fund.controller.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class TransactionDTO {
	
	private String transactionId;
	
	@JsonCreator
	TransactionDTO(@JsonProperty("transactionId")String transactionId){
		this.transactionId = transactionId;
	}
	
	public String getTransactionId() {
		return transactionId;
	}

	public static TransactionDTO of(String transactionId) {
		return new TransactionDTO(transactionId);
	}

	@Override
	public String toString() {
		return "TransactionDTO [transactionId=" + transactionId + "]";
	}
	
	
}
