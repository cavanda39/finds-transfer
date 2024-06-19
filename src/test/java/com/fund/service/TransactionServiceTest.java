package com.fund.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fund.client.ConversionResponse;
import com.fund.client.ConversionService;
import com.fund.controller.request.TransferRequest;
import com.fund.controller.response.TransactionDTO;
import com.fund.domain.Account;
import com.fund.domain.AccountRepository;
import com.fund.domain.Transaction;
import com.fund.domain.TransactionRepository;
import com.fund.exception.AccountException;
import com.fund.exception.ConversionException;
import com.fund.service.TransactionService;
import com.fund.util.TestUtil;

import reactor.core.publisher.Mono;

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
	void testOkNoConversion() throws AssertionError, InterruptedException, ExecutionException {
		Mockito.reset(conversionService);
		
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = new TransferRequest("source", "target", "EUR", BigDecimal.valueOf(5));
		
		TestUtil.assertTransferMoneyOk(service.transaferMoney(request));
		
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(5)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(5)), 0);
		
		List<Transaction> transactions = repo.findAll();
		assertEquals(2, transactions.size());
		
		transactions.stream().filter(t -> t.type().equals(Transaction.Type.CREDIT)).forEach(t -> {
			assertEquals(t.amount().compareTo(BigDecimal.valueOf(5)), 0);
			assertEquals(t.status(), Transaction.Status.COMPLETED);
		});
		transactions.stream().filter(t -> t.type().equals(Transaction.Type.DEBIT)).forEach(t -> {
			assertEquals(t.amount().compareTo(BigDecimal.valueOf(5)), 0);
			assertEquals(t.status(), Transaction.Status.COMPLETED);
		});
	}
	
	@Test
	void testOkNoConversionConcurrency() throws Exception{
		Mockito.reset(conversionService);
		
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = new TransferRequest("source", "target", "EUR", BigDecimal.valueOf(3));
		
		ExecutorService executorService = Executors.newFixedThreadPool(3);
        Callable<Mono<TransactionDTO>> task = () -> service.transaferMoney(request);
        
        Future<Mono<TransactionDTO>> future1 = executorService.submit(task);
        Future<Mono<TransactionDTO>> future2 = executorService.submit(task);
        Future<Mono<TransactionDTO>> future3 = executorService.submit(task);
        
        TestUtil.assertTransferMoneyOk(future1.get());
        TestUtil.assertTransferMoneyOk(future2.get());
        TestUtil.assertTransferMoneyOk(future3.get());
        
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.NANOSECONDS);
		
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(1)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(9)), 0);
		
		List<Transaction> transactions = repo.findAll();
		assertEquals(6, transactions.size());
		assertEquals(3, transactions.stream().filter(t -> t.type().equals(Transaction.Type.CREDIT)).count());
		assertEquals(3, transactions.stream().filter(t -> t.type().equals(Transaction.Type.DEBIT)).count());
		
		transactions.stream().filter(t -> t.type().equals(Transaction.Type.CREDIT)).forEach(t -> {
			assertEquals(t.amount().compareTo(BigDecimal.valueOf(3)), 0);
			assertEquals(t.status(), Transaction.Status.COMPLETED);
		});
		transactions.stream().filter(t -> t.type().equals(Transaction.Type.DEBIT)).forEach(t -> {
			assertEquals(t.amount().compareTo(BigDecimal.valueOf(3)), 0);
			assertEquals(t.status(), Transaction.Status.COMPLETED);
		});
	}
	
	@Test
	void testAmountNotEnoughConcurrency() throws Exception {
		Mockito.reset(conversionService);
		
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = new TransferRequest("source", "target", "EUR", BigDecimal.valueOf(3.75));
		
		ExecutorService executorService = Executors.newFixedThreadPool(3);
        Callable<Mono<TransactionDTO>> task = () -> service.transaferMoney(request);
        
        Future<Mono<TransactionDTO>> future1 = executorService.submit(task);
        Future<Mono<TransactionDTO>> future2 = executorService.submit(task);
        Future<Mono<TransactionDTO>> future3 = executorService.submit(task);
        
        TestUtil.assertTransferMoneyOk(future1.get());
        TestUtil.assertTransferMoneyOk(future2.get());
        TestUtil.assertTransferMoneyError(future3.get(), AccountException.class);
       
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.NANOSECONDS);
		
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(2.5)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(7.5)), 0);
		
		List<Transaction> transactions = repo.findAll();
		assertEquals(6, transactions.size());
		assertEquals(3, transactions.stream().filter(t -> t.type().equals(Transaction.Type.CREDIT)).count());
		assertEquals(3, transactions.stream().filter(t -> t.type().equals(Transaction.Type.DEBIT)).count());
		assertEquals(4, transactions.stream().filter(t -> t.status().equals(Transaction.Status.COMPLETED)).count());
		assertEquals(2, transactions.stream().filter(t -> t.status().equals(Transaction.Status.FAILED)).count());
		
		transactions.stream().filter(t -> t.type().equals(Transaction.Type.CREDIT)).forEach(t -> {
			assertEquals(t.amount().compareTo(BigDecimal.valueOf(3.75)), 0);
		});
		transactions.stream().filter(t -> t.type().equals(Transaction.Type.DEBIT)).forEach(t -> {
			assertEquals(t.amount().compareTo(BigDecimal.valueOf(3.75)), 0);
		});
	}
	
	@Test
	void testAmountNotEnough() {
		Mockito.reset(conversionService);
		
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = TestUtil.euroTransfer(10.2);
		
		TestUtil.assertTransferMoneyError(service.transaferMoney(request), AccountException.class);
		
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
		
		ConversionResponse response = TestUtil.responseOk(1.3);
		Mockito.when(conversionService.convertCurrency(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(response));
		
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetUsdAccount(0));
		TransferRequest request = TestUtil.euroTransfer(5);
		
		TestUtil.assertTransferMoneyOk(service.transaferMoney(request));
		
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(5)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(6.5)), 0);
		
		List<Transaction> transactions = repo.findAll();
		assertEquals(2, transactions.size());
		assertEquals(transactions.get(0).status(), Transaction.Status.COMPLETED);
		assertEquals(transactions.get(1).status(), Transaction.Status.COMPLETED);
		
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

		ConversionResponse response = TestUtil.responseGatewayError();
		Mockito.when(conversionService.convertCurrency(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(response));
		
		accountRepository.save(TestUtil.sourceEuroAccount(10));
		accountRepository.save(TestUtil.targetUsdAccount(0));
		TransferRequest request = TestUtil.euroTransfer(5);
		
		TestUtil.assertTransferMoneyError(service.transaferMoney(request), ConversionException.class);
		
		Account source = accountRepository.findByName("source");
		assertEquals(source.balance().compareTo(BigDecimal.valueOf(10)), 0);
		Account target = accountRepository.findByName("target");
		assertEquals(target.balance().compareTo(BigDecimal.valueOf(0)), 0);
		
		assertTrue(repo.findAll().isEmpty());
	}

}
