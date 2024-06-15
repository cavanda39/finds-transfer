package fund.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fund.controller.request.TransferRequest;
import fund.domain.AccountRepository;
import fund.exception.ValidationException;
import fund.service.ValidatorService;
import fund.util.TestUtil;

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
	
	@Test
	void testOk(){
		accountRepository.save(TestUtil.sourceEuroAccount(0));
		accountRepository.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = TestUtil.euroTransfer(20);
		service.validateRequest(request);
	}
	
	@Test
	void testMissingSourceAccount(){
		accountRepository.save(TestUtil.targetEuroAccount(10));
		TransferRequest request = TestUtil.euroTransfer(20);
		assertThrows(ValidationException.class, () -> service.validateRequest(request));
	}
	
	@Test
	void testMissingTargetAccount(){
		accountRepository.save(TestUtil.sourceEuroAccount(0));
		TransferRequest request = TestUtil.euroTransfer(20);
		assertThrows(ValidationException.class, () -> service.validateRequest(request));
	}

}
