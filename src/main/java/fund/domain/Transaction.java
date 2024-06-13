package fund.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Transaction {
	
	protected Transaction() {}
	
	public static enum Type{
		CREDIT, DEBIT;
	}
	
	@Id
	private String id;
	
	@Column
	private BigDecimal amount;
	
	@Column
	private String currency;
	
	@Column
	@Enumerated(EnumType.STRING)
	private Type type;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@Column
	private String parentAccount;
	
	@Column
	private String externalAccount;
	
	private Transaction(BigDecimal amount, String currency, Type type, String parentAccount, String externalAccount) {
		this.id = UUID.randomUUID().toString();
		this.amount = amount;
		this.currency = currency;
		this.type = type;
		this.createdAt = new Date();
		this.parentAccount = parentAccount;
		this.externalAccount = externalAccount;
	}
	
	
	public String id() {
		return id;
	}

	public BigDecimal amount() {
		return amount;
	}

	public String currency() {
		return currency;
	}

	public Type type() {
		return type;
	}

	public Date createdAt() {
		return createdAt;
	}

	public String parentAccount() {
		return parentAccount;
	}

	public String externalAccount() {
		return externalAccount;
	}

	public static Transaction creditTransaction(BigDecimal amount, String currency, String parentAccount, String externalAccount) {
		return new Transaction(amount,currency, Type.CREDIT, parentAccount, externalAccount);
	}
	
	public static Transaction debitTransaction(BigDecimal amount, String currency, String parentAccount, String externalAccount) {
		return new Transaction(amount,currency, Type.DEBIT, parentAccount, externalAccount);
	}
	
}
