package fund.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.common.collect.ImmutableMap;

import fund.exception.AccountException;
import fund.exception.AccountException.AccountExceptionType;

@Entity
public class Account {
	
	protected Account() {}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true)
	private String name;
	
	@Column(nullable = false)
	private String currency;
	
	@Column
	private BigDecimal balance;
	
	@Column(nullable = false)
	private long ownerId;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	private Account(String name, String currency, BigDecimal balance, long ownerId) {
		this.name = name;
		this.currency = currency;
		this.balance = balance;
		this.ownerId = ownerId;
		this.createdAt = new Date();
	}
	
	public Long id() {
		return id;
	}

	public String name() {
		return name;
	}
	
	public String currency() {
		return currency;
	}

	public BigDecimal balance() {
		return balance;
	}

	public long ownerId() {
		return ownerId;
	}

	public Date createdAt() {
		return createdAt;
	}

	public Date updatedAt() {
		return updatedAt;
	}

	public void increaseBalance(BigDecimal amount) {
		this.balance = this.balance.add(amount);
		this.updatedAt = new Date();
	}
	
	public void decreaseBalance(BigDecimal amount) {
		if ((this.balance.compareTo(amount) == -1)) {
			// TODO check if exception here is ok
			throw new AccountException(AccountExceptionType.NEGATIVE_AMOUNT_ERROR,
					ImmutableMap.of("error message", "balance is not enough to support the transaction",
							"current balance", this.balance, "import", amount));
		}
		this.balance = this.balance.subtract(amount);
		this.updatedAt = new Date();
	}
	
	public static Account of(String name, String currency, BigDecimal balance, long ownerId) {
		return new Account(name, currency, balance, ownerId);
	}

	@Override
	public String toString() {
		return "Account [id=" + id + ", name=" + name + ", currency=" + currency + ", balance=" + balance + ", ownerId="
				+ ownerId + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
}
