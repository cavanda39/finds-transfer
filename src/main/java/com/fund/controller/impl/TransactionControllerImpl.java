package com.fund.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.fund.controller.TransactionController;
import com.fund.controller.request.TransferRequest;
import com.fund.controller.response.TransactionDTO;
import com.fund.service.TransactionService;
import com.fund.service.ValidatorService;

import reactor.core.publisher.Mono;

@RestController
final class TransactionControllerImpl implements TransactionController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionControllerImpl.class);
	
	private final ValidatorService validatorService;
	private final TransactionService service;
	
	@Autowired
	public TransactionControllerImpl(ValidatorService validatorService, TransactionService service) {
		this.validatorService = validatorService;
		this.service = service;
	}
	
	@Override
	public Mono<TransactionDTO> transferMoney(TransferRequest request) {
		LOGGER.info("transaferMoney()");
		return validatorService.validateRequest(request)
				.then(service.transaferMoney(request));
	}

}
