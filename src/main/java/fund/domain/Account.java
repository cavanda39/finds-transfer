package fund.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
	
	private Account(String name, BigDecimal balance, long ownerId) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.balance = balance;
		this.ownerId = ownerId;
		this.createdAt = new Date();
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
	
	public static Account of(String name, BigDecimal balance, long ownerId) {
		return new Account(name, balance, ownerId);
	}

}
