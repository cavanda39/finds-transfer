package fund.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fund.client.ConversionError;
import fund.client.ConversionResponse;
import fund.client.ConversionService;
import fund.controller.request.TransferRequest;
import fund.controller.response.TransactionDTO;
import fund.domain.Account;
import fund.domain.AccountRepository;
import fund.domain.Transaction;
import fund.domain.TransactionRepository;
import fund.exception.AccountException;
import fund.exception.ConversionException;
import fund.util.TestUtil;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransactionServiceTest {
	
	@Autowired
	private TransactionService service;
	
	@MockBean
	private ConversionService conversionService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private TransactionRepository repo;
	
	@BeforeEach
	void beforeEach() {
		accountRepository.deleteAll();
		repo.deleteAll();
	}
	
	@Test
	void testOkNoConversion() {
		Mockito.reset(conversionService);
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = new TransferRequest("source", "target", "EUR", BigDecimal.valueOf(5));
		
		StepVerifier.create(service.transaferMoney(request))
		.expectNextMatches(res -> res.getTransactionId() != null)
		.expectComplete()
		.verify();
		
//		repo.findAll().forEach(a -> {
//			System.out.println(a.amount());
//		});
		
//		List<Transaction> transactions = repo.findAll();
//		assertEquals(2, transactions.size());
		
//		Transaction creditTransaction = transactions.stream().filter(t -> t.type().equals(Transaction.Type.CREDIT))
//				.findFirst().orElseThrow(() -> new RuntimeException("transaction not saved"));
//		assertEquals(creditTransaction.amount().compareTo(BigDecimal.valueOf(5)), 0);
//		
//		Transaction debitTransaction = transactions.stream().filter(t -> t.type().equals(Transaction.Type.DEBIT))
//				.findFirst().orElseThrow(() -> new RuntimeException("transaction not saved"));
//		assertEquals(debitTransaction.amount().compareTo(BigDecimal.valueOf(5)), 0);
		
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(5)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(5)), 0);
		
		List<Transaction> transactions = repo.findAll();
		assertEquals(2, transactions.size());
		assertEquals(transactions.get(0).status(), Transaction.Status.CREATED);
		assertEquals(transactions.get(1).status(), Transaction.Status.CREATED);
//		
		Transaction creditTransaction = transactions.stream().filter(t -> t.type().equals(Transaction.Type.CREDIT))
				.findFirst().orElseThrow(() -> new RuntimeException("transaction not saved"));
		assertEquals(creditTransaction.amount().compareTo(BigDecimal.valueOf(5)), 0);
		
		Transaction debitTransaction = transactions.stream().filter(t -> t.type().equals(Transaction.Type.DEBIT))
				.findFirst().orElseThrow(() -> new RuntimeException("transaction not saved"));
		assertEquals(debitTransaction.amount().compareTo(BigDecimal.valueOf(5)), 0);
	}
	
	@Test
	void testAmountNotEnough() {
		Mockito.reset(conversionService);
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = TestUtil.euroTransfer(10.2);
		
		StepVerifier.create(service.transaferMoney(request))
		.expectError(AccountException.class)
		.verify();
		
		List<Transaction> transactions = repo.findAll();
		assertEquals(transactions.size(), 2);
		assertEquals(transactions.get(0).status(), Transaction.Status.FAILED);
		assertEquals(transactions.get(1).status(), Transaction.Status.FAILED);
		
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(10)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(0)), 0);
		
	}
	
	@Test
	void testOkConversion(){
		Mockito.reset(conversionService);
		ConversionResponse response = new ConversionResponse();
		response.setResult(1.3);
		Mockito.when(conversionService.convertCurrency(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(response));
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetUsdAccount(0));
		TransferRequest request = TestUtil.euroTransfer(5);
		
		StepVerifier.create(service.transaferMoney(request))
		.expectNextMatches(res -> res.getTransactionId() != null)
		.expectComplete()
		.verify();
		
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(5)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(6.5)), 0);
		
		List<Transaction> transactions = repo.findAll();
		assertEquals(2, transactions.size());
		
		Transaction creditTransaction = transactions.stream().filter(t -> t.type().equals(Transaction.Type.CREDIT))
				.findFirst().orElseThrow(() -> new RuntimeException("transaction not saved"));
		assertEquals(creditTransaction.amount().compareTo(BigDecimal.valueOf(6.5)), 0);
		
		Transaction debitransaction = transactions.stream().filter(t -> t.type().equals(Transaction.Type.DEBIT))
				.findFirst().orElseThrow(() -> new RuntimeException("transaction not saved"));
		assertEquals(debitransaction.amount().compareTo(BigDecimal.valueOf(5)), 0);
	}
	
	@Test
	void testConversionNotAvailable(){
		Mockito.reset(conversionService);
		ConversionResponse response = new ConversionResponse();
		ConversionError error = new ConversionError();
		error.setCode(503);
		error.setInfo("service not available");
		response.setError(error);
		Mockito.when(conversionService.convertCurrency(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(response));
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetUsdAccount(0));
		TransferRequest request = TestUtil.euroTransfer(5);
		
		StepVerifier.create(service.transaferMoney(request))
		.expectError(ConversionException.class)
		.verify();
		
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(10)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(0)), 0);
		
		assertTrue(repo.findAll().isEmpty());
	}

}
