package fund.controller.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class TransferRequest {

	private final String sourceAccount;
	private final String targetAccount;
	private final String currency;
	private final BigDecimal amount;

	@JsonCreator
	public TransferRequest(@JsonProperty("sourceAccount") String sourceAccount,
			@JsonProperty("targetAccount") String targetAccount, @JsonProperty("currency") String currency,
			@JsonProperty("amount") BigDecimal amount) {
		this.sourceAccount = sourceAccount;
		this.targetAccount = targetAccount;
		this.currency = currency;
		this.amount = amount;
	}

	public String getSourceAccount() {
		return sourceAccount;
	}

	public String getTargetAccount() {
		return targetAccount;
	}

	public String getCurrency() {
		return currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return "TransferRequest [sourceAccount=" + sourceAccount + ", targetAccount=" + targetAccount + ", currency="
				+ currency + ", amount=" + amount + "]";
	}
	
}
