package fund.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fund.domain.AccountRepository;
import fund.domain.Account;

@Service
@Transactional(readOnly = false)
public class AccountService {
	
	private final AccountRepository repository;
	
	@Autowired
	public AccountService(AccountRepository repository) {
		this.repository = repository;
	}
	
	public Account findAccount(String accountName) {
		return repository.findByName(accountName);
	}
	
	public Account chargeAccount(String accountName, BigDecimal amount) {
		Account account = repository.findByName(accountName);
		account.decreaseBalance(amount);
		repository.save(account);
		return account;
	}
	
	public Account creditAccount(String accountName, BigDecimal amount) {
		Account account = repository.findByName(accountName);
		account.increaseBalance(amount);
		repository.save(account);
		return account;
	}
	
	
}
