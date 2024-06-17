package fund.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	public static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
	
	private final AccountRepository repository;
	
	@Autowired
	public AccountService(AccountRepository repository) {
		this.repository = repository;
	}
	
	public Mono<Account> findAccount(String accountName) {
		return Mono.just(repository.findByName(accountName));
	}
	
	public Mono<Account> chargeAccount(String accountName, BigDecimal amount) {
		Account account = repository.findByName(accountName);
		try {
			account.decreaseBalance(amount);
		} catch (AccountException e) {
			LOGGER.error("error charging account: [{}]", account.name());
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
