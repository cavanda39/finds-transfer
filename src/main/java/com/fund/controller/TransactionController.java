package com.fund.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fund.controller.request.TransferRequest;
import com.fund.controller.response.TransactionDTO;

import reactor.core.publisher.Mono;

@RequestMapping("api/v1/transaction")
public interface TransactionController {
	
	@PostMapping(value = "send", produces = MediaType.APPLICATION_JSON_VALUE)
	Mono<TransactionDTO> transferMoney(@RequestBody(required = true)TransferRequest request); 

}
