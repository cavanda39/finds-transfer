package fund.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@Entity
public class Account {
	
	protected Account() {}
	
	@Id
	private String id;
	
	@Column(unique = true)
	private String name;
	
	@Column
	private BigDecimal balance;
	
	@Column
	private long ownerId;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Transaction> transactions;
	
	private Account(String name, BigDecimal balance, long ownerId) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.balance = balance;
		this.ownerId = ownerId;
		this.createdAt = new Date();
		this.transactions = Lists.newArrayList();
	}
	
	public String id() {
		return id;
	}

	public String name() {
		return name;
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

	public ImmutableList<List<Transaction>> transactions() {
		return ImmutableList.of(transactions);
	}
	
	public void increaseBalance(BigDecimal amount) {
		this.balance = this.balance.add(amount);
		this.updatedAt = new Date();
	}
	
	public void decreaseBalance(BigDecimal amount) {
		if(!(this.balance.compareTo(amount) == -1)) {
			this.balance = this.balance.subtract(amount);
			this.updatedAt = new Date();
		}
	}
	
	public void addTransaction(Transaction transaction) {
		this.transactions.add(transaction);
	}

	public static Account of(String name, BigDecimal balance, long ownerId) {
		return new Account(name, balance, ownerId);
	}

}
