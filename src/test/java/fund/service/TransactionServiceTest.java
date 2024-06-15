package fund.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;

import fund.client.ConversionError;
import fund.client.ConversionResponse;
import fund.client.ConversionService;
import fund.controller.request.TransferRequest;
import fund.controller.response.TransactionDTO;
import fund.domain.Account;
import fund.domain.AccountRepository;
import fund.exception.AccountException;
import fund.exception.ConversionException;
import fund.util.TestUtil;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransactionServiceTest {
	
	@Autowired
	private TransactionService service;
	
	@MockBean
	private ConversionService conversionService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@BeforeEach
	void beforeEach() {
		accountRepository.deleteAll();
	}
	
	@Test
	void testOkNoConversion() {
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = new TransferRequest("source", "target", "EUR", BigDecimal.valueOf(5));
		TransactionDTO result = service.createTransaction(request);
		assertNotNull(result.getTransactionId());
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(5)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(5)), 0);
	}
	
	@Test
	void testAmountNotEnought() {
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = TestUtil.euroTransfer(10.2);
		assertThrows(AccountException.class, () -> service.createTransaction(request));
	}
	
	@Test
	void testOkConversion(){
		Mockito.reset(conversionService);
		ConversionResponse response = new ConversionResponse();
		response.setResult(1.3);
		Mockito.when(conversionService.convertCurrency(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetUsdAccount(0));
		TransferRequest request = TestUtil.euroTransfer(5);
		TransactionDTO result = service.createTransaction(request);
		assertNotNull(result.getTransactionId());
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(5)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(6.5)), 0);
	}
	
	@Test
	void testConversionNotAvailable(){
		Mockito.reset(conversionService);
		ConversionResponse response = new ConversionResponse();
		ConversionError error = new ConversionError();
		error.setCode(503);
		error.setInfo("service not available");
		response.setError(error);
		Mockito.when(conversionService.convertCurrency(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetUsdAccount(0));
		TransferRequest request = TestUtil.euroTransfer(5);
		assertThrows(ConversionException.class, () -> service.createTransaction(request));
	}

}
