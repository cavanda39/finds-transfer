package fund.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fund.domain.Account;
import fund.domain.AccountRepository;
import fund.exception.AccountException;
import reactor.test.StepVerifier;
import fund.util.TestUtil;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AccountServiceTest {

	@Autowired
	private AccountRepository repo;
	
	@Autowired
	private AccountService service;
	
	Account account = TestUtil.sourceEuroAccount(50.00);
	
	@BeforeEach
	void beforeEach() {
		repo.deleteAll();
		repo.save(account);
	}
	
	@Test
	void testBalanceIncrease() {
		StepVerifier.create(service.creditAccount("source", BigDecimal.valueOf(7.034)))
		.expectNextMatches(res -> res.balance().compareTo(BigDecimal.valueOf(57.034)) == 0)
		.expectComplete()
		.verify();
	}
	
	@Test
	void testBalanceDecrease() {
		StepVerifier.create(service.chargeAccount("source", BigDecimal.valueOf(7.034)))
		.expectNextMatches(res -> res.balance().compareTo(BigDecimal.valueOf(42.966)) == 0)
		.expectComplete()
		.verify();
	}
	
	@Test
	void testBalanceDecreaseException() {
		StepVerifier.create(service.chargeAccount("source", BigDecimal.valueOf(50.001)))
		.expectError(AccountException.class)
		.verify();
	}
	
}
