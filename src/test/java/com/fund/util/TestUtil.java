package com.fund.util;

import java.math.BigDecimal;

import com.fund.client.ConversionError;
import com.fund.client.ConversionResponse;
import com.fund.controller.request.TransferRequest;
import com.fund.controller.response.TransactionDTO;
import com.fund.domain.Account;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TestUtil {
	
	public static Account sourceEuroAccount(double amount) {
		return Account.of("source", "EUR", BigDecimal.valueOf(amount), 1);
	}
	
	public static Account targetEuroAccount(double amount) {
		return Account.of("target", "EUR", BigDecimal.valueOf(amount), 2);
	}
	
	public static Account targetUsdAccount(double amount) {
		return Account.of("target", "USD", BigDecimal.valueOf(amount), 2);
	}
	
	public static TransferRequest euroTransfer(double amount) {
		return new TransferRequest("source", "target", "EUR", BigDecimal.valueOf(amount));
	}
	
	public static ConversionResponse responseOk(double amount) {
		ConversionResponse response = new ConversionResponse();
		response.setResult(amount);
		return response;
	}
	
	public static ConversionResponse responseGatewayError() {
		ConversionResponse response = new ConversionResponse();
		ConversionError error = new ConversionError();
		error.setCode(503);
		error.setInfo("service not available");
		response.setError(error);
		return response;
	}
	
	public static void assertTransferMoneyOk(Mono<TransactionDTO> function) {
		StepVerifier.create(function)
		.expectNextMatches(res -> res.getTransactionId() != null)
		.expectComplete()
		.verify();
	}
	
	public static void assertTransferMoneyError(Mono<TransactionDTO> function, Class<? extends Throwable> exceptionClass) {
		StepVerifier.create(function)
		.expectError(exceptionClass)
		.verify();
	}

}
