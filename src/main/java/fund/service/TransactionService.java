package fund.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import fund.client.ConversionResponse;
import fund.client.ConversionService;
import fund.controller.request.TransferRequest;
import fund.controller.response.TransactionDTO;
import fund.domain.AccountRepository;
import fund.domain.Transaction;
import fund.domain.TransactionRepository;
import fund.exception.ConversionException;
import fund.exception.ConversionException.ConversionExceptionType;
import fund.domain.Account;

@Service
@Transactional(readOnly = false)
public class TransactionService {
	
private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
	
	private final AccountRepository accountRepository;
	private final TransactionRepository repository;
	private final ConversionService conversionService;
	
	@Autowired
	public TransactionService(AccountRepository accountRepository, TransactionRepository repository, ConversionService conversionService) {
		this.accountRepository = accountRepository;
		this.repository = repository;
		this.conversionService = conversionService;
	}
	
	public TransactionDTO createTransaction(TransferRequest request) {
		LOGGER.info("createTransaction: [{}]", request);
		
		Account targetAccount = accountRepository.findByName(request.getTargetAccount());
		Account sourceAccount = accountRepository.findByName(request.getSourceAccount());
		BigDecimal transactionAmount = request.getAmount();
		
		if (!targetAccount.currency().equalsIgnoreCase(request.getCurrency())) {
			LOGGER.trace("different currency: [{}], [{}]", targetAccount.currency(), request.getCurrency());
			
			ConversionResponse response = conversionService.convertCurrency(request.getCurrency(),
					targetAccount.currency(), transactionAmount);
			
			// exception handled here because in future we might decide to retrieve the exchange rate elsewhere
			if (response.getError() != null) {
				LOGGER.error("conversion process not successful: [{}], [{}]", response.getError().getCode(),
						response.getError().getInfo());
				throw new ConversionException(ConversionExceptionType.CONVERSION_ERROR,
						ImmutableMap.of("error converting currency:", request.getCurrency(), "target currency",
								targetAccount.currency(), "error message", response.getError().getInfo()));
			}
			transactionAmount = transactionAmount.multiply(BigDecimal.valueOf(response.getResult()));
		}
		
		Transaction debitTransaction = Transaction.debitTransaction(request.getAmount(), request.getCurrency(), sourceAccount.name(), targetAccount.name());
		sourceAccount.decreaseBalance(request.getAmount());
		repository.save(debitTransaction);
		
		Transaction creditTransaction = Transaction.creditTransaction(transactionAmount, targetAccount.currency(), targetAccount.name(), sourceAccount.name());
		targetAccount.increaseBalance(transactionAmount);
		repository.save(creditTransaction);
		
		accountRepository.save(sourceAccount);
		accountRepository.save(targetAccount);
		
		return TransactionDTO.of(debitTransaction.id());
	}

}
