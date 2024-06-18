package com.fund.service;

import static reactor.core.publisher.Mono.just;
import static reactor.util.function.Tuples.of;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fund.client.ConversionService;
import com.fund.controller.request.TransferRequest;
import com.fund.controller.response.TransactionDTO;
import com.fund.domain.Account;
import com.fund.domain.Transaction;
import com.fund.domain.TransactionRepository;
import com.fund.exception.ConversionException;
import com.fund.exception.ConversionException.ConversionExceptionType;
import com.google.common.collect.ImmutableMap;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

@Service
@Transactional(readOnly = false)
public class TransactionService {
	
private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
	
	private final TransactionRepository repository;
	private final ConversionService conversionService;
	private final AccountService accountService;
	
	@Autowired
	public TransactionService(TransactionRepository repository, ConversionService conversionService, AccountService accountService) {
		this.repository = repository;
		this.conversionService = conversionService;
		this.accountService = accountService;
	}
	
	public Mono<TransactionDTO> transaferMoney(TransferRequest request) {
		LOGGER.info("transaferMoney: [{}]", request);
		return just(request)
				.then(accountService.findAccount(request.getTargetAccount()))
				.flatMap(targetAccount -> convertAmount(targetAccount, request))
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(amount -> saveTransactions(amount, request))
				.flatMap(transactions -> updateAccounts(transactions))
				.flatMap(transactions -> completeTransactions(transactions))
				.map(transactions -> TransactionDTO.of(transactions.getT1().id()));
	}
	
	private Mono<Tuple2<Transaction, Transaction>> saveTransactions(Tuple2<BigDecimal, String> creditAmount, TransferRequest request){
		return Mono.zip(saveDebitTransaction(request), saveCreditTransaction(creditAmount, request));
	}
	
	private Mono<Tuple2<Transaction, Transaction>> completeTransactions(Tuple2<Transaction, Transaction> transactions){
		return Mono.zip(completeTransaction(transactions.getT1()), completeTransaction(transactions.getT2()));
	}
	
	private Mono<Tuple2<Transaction, Transaction>> updateAccounts(Tuple2<Transaction, Transaction> transactions) {
		return accountService.chargeAccount(transactions.getT1().parentAccount(), transactions.getT1().amount())
				.doOnError(error -> {
					// TODO enhance logges
					LOGGER.error("error charging account: [{}]. Proceed to rollback transactions", error.getMessage());
					cancelTransactions(transactions.getT1(), transactions.getT2());
				})
//		.then(accountService.creditAccount(transactions.getT2().parentAccount(), transactions.getT2().amount()))
				.flatMap(account -> accountService.creditAccount(transactions.getT2().parentAccount(),
						transactions.getT2().amount()))

//		.doAfterTerminate(() -> accountService.creditAccount(transactions.getT2().parentAccount(), transactions.getT2().amount()))
				.flatMap(account -> just(transactions));
	}
	
	private Mono<Transaction> completeTransaction(Transaction transaction){
		LOGGER.info("transaction completed(), [{}]", transaction);
		transaction.completed();
		return just(repository.save(transaction));
	}
	
	private Mono<Void> cancelTransactions(Transaction debitransaction, Transaction creditTransaction){
		LOGGER.info("transactions failed(), [{}], [{}]", debitransaction, creditTransaction);
		debitransaction.failed();
		creditTransaction.failed();
		repository.save(debitransaction);
		repository.save(creditTransaction);
		return Mono.empty();
	}
	
	private  Mono<Transaction> saveDebitTransaction(TransferRequest request){
		Transaction debitTransaction = Transaction.debitTransaction(request.getAmount(), request.getCurrency(), request.getSourceAccount(), request.getTargetAccount());
		return just(repository.save(debitTransaction));
	}
	
	private Mono<Transaction> saveCreditTransaction(Tuple2<BigDecimal, String> creditAmount, TransferRequest request){
		Transaction creditTransaction = Transaction.creditTransaction(creditAmount.getT1(), creditAmount.getT2(), request.getTargetAccount(), request.getSourceAccount());
		return just(repository.save(creditTransaction));
	}
	
	private Mono<Tuple2<BigDecimal, String>> convertAmount(Account targetAccount, TransferRequest request) {
		if (!targetAccount.currency().equalsIgnoreCase(request.getCurrency())) {
			return conversionService.convertCurrency(request.getCurrency(), targetAccount.currency(), request.getAmount())
					.flatMap(conversionResult -> {
						// result handled here because in future we might perform different action (e.g. fetch exchange rate from different service)
						if (conversionResult.getError() != null) {
							LOGGER.error("conversion process not successful: [{}], [{}]",
									conversionResult.getError().getCode(), conversionResult.getError().getInfo());
							return Mono.error(() -> new ConversionException(ConversionExceptionType.CONVERSION_ERROR,
									ImmutableMap.of("error converting currency:", request.getCurrency(),
											"target currency", targetAccount.currency(), "error message",
											conversionResult.getError().getInfo())));
						}
						LOGGER.info("exchange rate: [{}]", conversionResult.getResult());
						BigDecimal exchangeRate = BigDecimal.valueOf(conversionResult.getResult());
						return just(of(exchangeRate.multiply(request.getAmount()), targetAccount.currency()));
					});
		} else {
			return just(of(request.getAmount(), request.getCurrency()));
		}
	}
	
}
