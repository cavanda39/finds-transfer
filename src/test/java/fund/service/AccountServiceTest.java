package fund.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		
		account = service.creditAccount("source", (BigDecimal.valueOf(7.034)));
		assertEquals(account.balance(), BigDecimal.valueOf(57.034));
		assertNotNull(account.updatedAt());
	}
	
	@Test
	void testBalanceDecrease() {
		account = service.chargeAccount("source", BigDecimal.valueOf(7.034));
		assertEquals(account.balance(), BigDecimal.valueOf(42.966));
		assertNotNull(account.updatedAt());
	}
	
	@Test
	void testBalanceDecreaseException() {
		assertThrows(AccountException.class, () -> account = service.chargeAccount("source", BigDecimal.valueOf(50.001)));
	}
	
}
