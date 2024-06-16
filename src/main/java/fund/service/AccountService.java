package fund.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fund.domain.Account;
import fund.domain.AccountRepository;
import fund.exception.AccountException;
import reactor.core.publisher.Mono;

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
	
	public Mono<Account> chargeAccount(String accountName, BigDecimal amount) {
		Account account = repository.findByName(accountName);
		try {
			account.decreaseBalance(amount);
		} catch (AccountException e) {
			return Mono.error(e);
		}
		return Mono.just(repository.save(account));
	}
	
	public Mono<Account> creditAccount(String accountName, BigDecimal amount) {
		Account account = repository.findByName(accountName);
		account.increaseBalance(amount);
		return Mono.just(repository.save(account));
	}
	
	
}
