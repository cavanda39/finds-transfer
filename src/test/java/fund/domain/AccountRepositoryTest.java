package fund.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fund.exception.AccountException;

@ExtendWith(SpringExtension.class)
@DataJpaTest(showSql = false)
class AccountRepositoryTest {
	
	@Autowired
	private AccountRepository repo;
	
	Account account = Account.of("myAccount", BigDecimal.valueOf(50.00), 1);
	
	@Test
	void testFetchByName() {
		repo.save(account);
		Account fetchedAccount = repo.findByName("myAccount");
		Assertions.assertTrue(fetchedAccount.balance().compareTo(BigDecimal.valueOf(50.00)) == 0);
	}
	
	@Test
	void testBalanceIncrease() {
		account.increaseBalance(BigDecimal.valueOf(7.034));
		assertEquals(account.balance(), BigDecimal.valueOf(57.034));
		assertNotNull(account.updatedAt());
		repo.save(account);
		Account fetchedAccount = repo.findById(account.id()).orElseThrow(() -> new RuntimeException("account not found"));
		Assertions.assertTrue(fetchedAccount.balance().compareTo(BigDecimal.valueOf(57.034)) == 0);
	}
	
	@Test
	void testBalanceDecrease() {
		account.decreaseBalance(BigDecimal.valueOf(7.034));
		assertEquals(account.balance(), BigDecimal.valueOf(42.966));
		assertNotNull(account.updatedAt());
		repo.save(account);
		Account fetchedAccount = repo.findById(account.id()).orElseThrow(() -> new RuntimeException("account not found"));
		Assertions.assertTrue(fetchedAccount.balance().compareTo(BigDecimal.valueOf(42.966)) == 0);
	}
	
	@Test
	void testBalanceDecreaseException() {
		assertThrows(AccountException.class, () -> account.decreaseBalance(BigDecimal.valueOf(50.001)));
	}

}
