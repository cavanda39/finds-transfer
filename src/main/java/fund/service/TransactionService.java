package fund.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import fund.client.ConversionResponse;
import fund.client.ConversionService;
import fund.controller.request.TransferRequest;
import fund.controller.response.TransactionDTO;
import fund.domain.Account;
import fund.domain.AccountRepository;
import fund.domain.Transaction;
import fund.domain.TransactionRepository;
import fund.exception.ConversionException;
import fund.exception.ConversionException.ConversionExceptionType;
import reactor.core.publisher.Mono;
import static reactor.core.publisher.Mono.just;
import static reactor.util.function.Tuples.of;
import reactor.core.scheduler.Schedulers;

@Service
@Transactional(readOnly = false)
public class TransactionService {
	
private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
	
	private final AccountRepository accountRepository;
	private final TransactionRepository repository;
	private final ConversionService conversionService;
	private final AccountService accountService;
	
	@Autowired
	public TransactionService(AccountRepository accountRepository, TransactionRepository repository, ConversionService conversionService, AccountService accountService) {
		this.accountRepository = accountRepository;
		this.repository = repository;
		this.conversionService = conversionService;
		this.accountService = accountService;
	}
	
	public Mono<TransactionDTO> transaferMoney(TransferRequest request) {
		LOGGER.info("transaferMoney: [{}]", request);
		
		return just(request)
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(req -> just(accountService.findAccount(req.getTargetAccount())))
				.flatMap(targetAccount -> {
					if (!targetAccount.currency().equalsIgnoreCase(request.getCurrency())) {
						// result handled here because in future we might perform different action (e.g. fetch exchange rate from different service)
						return conversionService
								.convertCurrency(request.getCurrency(), targetAccount.currency(), request.getAmount())
								.flatMap(conversionResult -> {
									if (conversionResult.getError() != null) {
										LOGGER.error("conversion process not successful: [{}], [{}]",
												conversionResult.getError().getCode(),
												conversionResult.getError().getInfo());
										return Mono.error(() -> new ConversionException(
												ConversionExceptionType.CONVERSION_ERROR,
												ImmutableMap.of("error converting currency:", request.getCurrency(),
														"target currency", targetAccount.currency(), "error message",
														conversionResult.getError().getInfo())));

									}
									BigDecimal exchangeRate = BigDecimal.valueOf(conversionResult.getResult());
									return just(exchangeRate.multiply(request.getAmount()));
								});
					} else {
						return just(request.getAmount());
					}

				})
				.flatMap(amount -> {
					Transaction debitTransaction = Transaction.debitTransaction(request.getAmount(), request.getCurrency(), request.getSourceAccount(), request.getTargetAccount());
					repository.save(debitTransaction);
					return just(of(debitTransaction, amount));
				})
				.flatMap(debitTuple -> just(accountService.chargeAccount(debitTuple.getT1().parentAccount(), request.getAmount()))
						.map(sourceAccount -> of(sourceAccount, debitTuple.getT2()))
						)
				.flatMap(creditTuple -> {
					Transaction creditTransaction = Transaction.creditTransaction(creditTuple.getT2(), creditTuple.getT1().currency(), request.getTargetAccount(), request.getSourceAccount());
					repository.save(creditTransaction);
					return just(creditTransaction);
				})
				.flatMap(creditTransaction -> {
					accountService.creditAccount(request.getTargetAccount(), creditTransaction.amount());
					return just(TransactionDTO.of(creditTransaction.id()));
				});
				
				
		
//		Account targetAccount = accountRepository.findByName(request.getTargetAccount());
//		Account sourceAccount = accountRepository.findByName(request.getSourceAccount());
//		BigDecimal transactionAmount = request.getAmount();
//		
//		if (!targetAccount.currency().equalsIgnoreCase(request.getCurrency())) {
//			LOGGER.trace("different currency: [{}], [{}]", targetAccount.currency(), request.getCurrency());
//			
////			ConversionResponse response = conversionService.convertCurrency(request.getCurrency(),
////					targetAccount.currency(), transactionAmount);
////			
////			// exception handled here because in future we might decide to retrieve the exchange rate elsewhere
////			if (response.getError() != null) {
////				LOGGER.error("conversion process not successful: [{}], [{}]", response.getError().getCode(),
////						response.getError().getInfo());
////				throw new ConversionException(ConversionExceptionType.CONVERSION_ERROR,
////						ImmutableMap.of("error converting currency:", request.getCurrency(), "target currency",
////								targetAccount.currency(), "error message", response.getError().getInfo()));
////			}
////			transactionAmount = transactionAmount.multiply(BigDecimal.valueOf(response.getResult()));
//		}
//		
//		Transaction debitTransaction = Transaction.debitTransaction(request.getAmount(), request.getCurrency(), sourceAccount.name(), targetAccount.name());
//		sourceAccount.decreaseBalance(request.getAmount());
//		repository.save(debitTransaction);
//		
//		Transaction creditTransaction = Transaction.creditTransaction(transactionAmount, targetAccount.currency(), targetAccount.name(), sourceAccount.name());
//		targetAccount.increaseBalance(transactionAmount);
//		repository.save(creditTransaction);
//		
//		accountRepository.save(sourceAccount);
//		accountRepository.save(targetAccount);
//		
//		return null;
//				TransactionDTO.of(debitTransaction.id());
	}
	
//	public 

}
