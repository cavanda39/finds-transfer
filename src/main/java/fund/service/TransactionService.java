package fund.service;

import static reactor.core.publisher.Mono.just;
import static reactor.util.function.Tuples.of;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import fund.client.ConversionService;
import fund.controller.request.TransferRequest;
import fund.controller.response.TransactionDTO;
import fund.domain.Account;
import fund.domain.Transaction;
import fund.domain.TransactionRepository;
import fund.exception.ConversionException;
import fund.exception.ConversionException.ConversionExceptionType;
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
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(req -> just(accountService.findAccount(req.getTargetAccount())))
				.flatMap(targetAccount -> convertAmount(targetAccount, request))
				.flatMap(amount -> saveTransactions(amount, request))
				.flatMap(transactions -> updateAccounts(transactions));
//				.flatMap(amount -> saveDebitTransaction(amount, request))
//				.flatMap(debitTuple -> debitAccount(debitTuple, request))
//				.flatMap(creditTuple -> creditTransaction(creditTuple, request))
//				.flatMap(creditTransaction -> creditAccount(creditTransaction, request));
	}
	
	private Mono<Tuple2<Transaction, Transaction>> saveTransactions(Tuple2<BigDecimal, String> creditAmount, TransferRequest request){
		Transaction debitTransaction = Transaction.debitTransaction(request.getAmount(), request.getCurrency(), request.getSourceAccount(), request.getTargetAccount());
		repository.save(debitTransaction);
		Transaction creditTransaction = Transaction.creditTransaction(creditAmount.getT1(), creditAmount.getT2(), request.getTargetAccount(), request.getSourceAccount());
		repository.save(creditTransaction);
		return just(of(debitTransaction, creditTransaction));
	}
	
	private Mono<TransactionDTO> updateAccounts(Tuple2<Transaction, Transaction> transactions){
		return accountService.chargeAccount(transactions.getT1().parentAccount(), transactions.getT1().amount())
		.doOnError(error -> {
			//TODO enhance logges
			LOGGER.error("error charging account");
					removeTransactions(transactions.getT1(), transactions.getT2());
//					.flatMap(a -> removeTransaction(transactions.getT2()))		
//					.doAfterTerminate(() -> removeTransaction(transactions.getT2()));
		})
		.flatMap(a -> accountService.creditAccount(transactions.getT2().parentAccount(), transactions.getT2().amount()))
		
//		.doAfterTerminate(() -> accountService.creditAccount(transactions.getT2().parentAccount(), transactions.getT2().amount()))
		.flatMap(a -> just(TransactionDTO.of(transactions.getT1().id())));
	}
	
	private Mono<Void> removeTransactions(Transaction debitransaction, Transaction creditTransaction){
		LOGGER.info("transaction failed(), [{}], [{}]", debitransaction, creditTransaction);
		debitransaction.failed();
		creditTransaction.failed();
		repository.save(debitransaction);
		repository.save(creditTransaction);
		return Mono.empty();
	}
	
	private Mono<TransactionDTO> creditAccount(Transaction creditTransaction, TransferRequest request){
		accountService.creditAccount(request.getTargetAccount(), creditTransaction.amount());
		return just(TransactionDTO.of(creditTransaction.id()));
	}
	
	private Mono<Transaction> creditTransaction(Tuple2<Account, BigDecimal> creditTuple, TransferRequest request){
		Transaction creditTransaction = Transaction.creditTransaction(creditTuple.getT2(), creditTuple.getT1().currency(), request.getTargetAccount(), request.getSourceAccount());
		repository.save(creditTransaction);
		return just(creditTransaction);
	}
	
//	private Mono<Tuple2<Account, BigDecimal>> debitAccount(Tuple2<Transaction, BigDecimal> debitTuple, TransferRequest request){
//		return accountService.chargeAccount(debitTuple.getT1().parentAccount(), request.getAmount())
//				.doOnError(error -> {
//					 removeTransaction(debitTuple.getT1());
//				})
//				.map(sourceAccount -> of(sourceAccount, debitTuple.getT2()))
//		;
//	}
	
	private Mono<Tuple2<Transaction, BigDecimal>> saveDebitTransaction(Tuple2<BigDecimal, String> amount, TransferRequest request){
		Transaction debitTransaction = Transaction.debitTransaction(request.getAmount(), request.getCurrency(), request.getSourceAccount(), request.getTargetAccount());
		repository.save(debitTransaction);
		return just(of(debitTransaction, amount.getT1()));
	}
	
	private Mono<Tuple2<BigDecimal, String>> convertAmount(Account targetAccount, TransferRequest request) {
		if (!targetAccount.currency().equalsIgnoreCase(request.getCurrency())) {
			// result handled here because in future we might perform different action (e.g. fetch exchange rate from different service)
			return conversionService
					.convertCurrency(request.getCurrency(), targetAccount.currency(), request.getAmount())
					.flatMap(conversionResult -> {
						if (conversionResult.getError() != null) {
							LOGGER.error("conversion process not successful: [{}], [{}]",
									conversionResult.getError().getCode(), conversionResult.getError().getInfo());
							return Mono.error(() -> new ConversionException(ConversionExceptionType.CONVERSION_ERROR,
									ImmutableMap.of("error converting currency:", request.getCurrency(),
											"target currency", targetAccount.currency(), "error message",
											conversionResult.getError().getInfo())));
						}
						BigDecimal exchangeRate = BigDecimal.valueOf(conversionResult.getResult());
						return just(of(exchangeRate.multiply(request.getAmount()), targetAccount.currency()));
					});
		} else {
			return just(of(request.getAmount(), request.getCurrency()));
		}
	}
	
	public Mono<Transaction> saveDebitTransaction(TransferRequest request){
		Transaction debitTransaction = Transaction.debitTransaction(request.getAmount(), request.getCurrency(), request.getSourceAccount(), request.getTargetAccount());
		return just(repository.save(debitTransaction));
	}
	
}
