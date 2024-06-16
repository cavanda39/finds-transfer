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
	
	public static enum Type {
		CREDIT, DEBIT;
	}
	
	public static enum Status {
		CREATED, FAILED, COMPLETED;
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
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column
	private String parentAccount;
	
	@Column
	private String externalAccount;
	
	private Transaction(BigDecimal amount, String currency, Type type, String parentAccount, String externalAccount) {
		this.id = UUID.randomUUID().toString();
		this.amount = amount;
		this.currency = currency;
		this.status = Status.CREATED;
		this.type = type;
		this.createdAt = new Date();
		this.parentAccount = parentAccount;
		this.externalAccount = externalAccount;
		this.updatedAt = null;
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
	
	public Status status() {
		return status;
	}

	public Date createdAt() {
		return createdAt;
	}
	
	public Date updatedAt() {
		return updatedAt;
	}

	public String parentAccount() {
		return parentAccount;
	}

	public String externalAccount() {
		return externalAccount;
	}
	
	public void failed() {
		this.status = Status.FAILED;
		this.updatedAt = new Date();
	}
	
	public void completed() {
		this.status = Status.COMPLETED;
		this.updatedAt = new Date();
	}

	public static Transaction creditTransaction(BigDecimal amount, String currency, String parentAccount, String externalAccount) {
		return new Transaction(amount,currency, Type.CREDIT, parentAccount, externalAccount);
	}
	
	public static Transaction debitTransaction(BigDecimal amount, String currency, String parentAccount, String externalAccount) {
		return new Transaction(amount,currency, Type.DEBIT, parentAccount, externalAccount);
	}

	@Override
	public String toString() {
		return "Transaction [id=" + id + ", amount=" + amount + ", currency=" + currency + ", type=" + type
				+ ", status=" + status + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", parentAccount="
				+ parentAccount + ", externalAccount=" + externalAccount + "]";
	}
	
}
