package fund.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fund.controller.request.TransferRequest;
import fund.domain.Account;
import fund.domain.AccountRepository;
import fund.exception.ValidationException;
import fund.service.ValidatorService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ValidatorServiceTest {
	
	@Autowired
	private ValidatorService service;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@AfterEach
	void afterEach() {
		accountRepository.deleteAll();
	}
	
	// TODO add other test
	
	@Test
	void testOk(){
		accountRepository.save(Account.of("source", BigDecimal.valueOf(0), 1));
		accountRepository.save(Account.of("target", BigDecimal.valueOf(0), 2));
		TransferRequest request = new TransferRequest("source", "target", "EUR", BigDecimal.valueOf(20));
		service.validateRequest(request);
	}
	
	@Test
	void testMissingSourceAccount(){
		accountRepository.save(Account.of("target", BigDecimal.valueOf(0), 2));
		TransferRequest request = new TransferRequest("source", "target", "EUR", BigDecimal.valueOf(20));
		assertThrows(ValidationException.class, () -> service.validateRequest(request));
	}
	
	@Test
	void testMissingTargetAccount(){
		accountRepository.save(Account.of("source", BigDecimal.valueOf(0), 1));
		TransferRequest request = new TransferRequest("source", "target", "EUR", BigDecimal.valueOf(20));
		assertThrows(ValidationException.class, () -> service.validateRequest(request));
	}

}
