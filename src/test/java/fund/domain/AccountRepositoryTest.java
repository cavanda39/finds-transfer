package fund.domain;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fund.exception.AccountException;
import fund.util.TestUtil;

@ExtendWith(SpringExtension.class)
@DataJpaTest(showSql = false)
class AccountRepositoryTest {
	
	@Autowired
	private AccountRepository repo;
	
	Account account = TestUtil.sourceEuroAccount(50.00);
	
	@Test
	void testFetchByName() {
		repo.save(TestUtil.sourceEuroAccount(50.00));
		Account fetchedAccount = repo.findByName("source");
		Assertions.assertTrue(fetchedAccount.balance().compareTo(BigDecimal.valueOf(50.00)) == 0);
	}
	

}
