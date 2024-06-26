package com.fund.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fund.controller.request.TransferRequest;
import com.fund.domain.AccountRepository;
import com.fund.exception.ValidationException;
import com.fund.exception.ValidationException.ValidationExceptionType;
import com.google.common.collect.ImmutableMap;

import reactor.core.publisher.Mono;

@Service
@Transactional(readOnly = true)
public class ValidatorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorService.class);
	
	private final AccountRepository accountRepository;
	
	@Autowired
	public ValidatorService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}
	
	// we consider amount and currency already validated by front end 
	public Mono<Void> validateRequest(TransferRequest request) {
		LOGGER.info("validate request(), [{}]", request);
		
		if(accountRepository.findByName(request.getSourceAccount()) == null) {
			LOGGER.error("source account not found: [{}]", request.getSourceAccount());
			throw new ValidationException(ValidationExceptionType.ACCOUNT_NOT_FOUND_ERROR,
					ImmutableMap.of("source account not found", request.getSourceAccount()));
		}
		
		if(accountRepository.findByName(request.getTargetAccount()) == null) {
			LOGGER.error("target account not found: [{}]", request.getTargetAccount());
			throw new ValidationException(ValidationExceptionType.ACCOUNT_NOT_FOUND_ERROR,
					ImmutableMap.of("target account not found", request.getTargetAccount()));
		}
		
		return Mono.empty();
	}

}
