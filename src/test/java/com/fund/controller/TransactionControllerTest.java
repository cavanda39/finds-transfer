package com.fund.controller;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fund.controller.request.TransferRequest;
import com.fund.domain.AccountRepository;
import com.fund.util.TestUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerTest {
	
	private static final String URL = "/api/v1/transaction/send";
	
	@Autowired
    private WebTestClient webTestClient;
	
	@Autowired
	private AccountRepository repo;
	
	@BeforeEach
	void beforeEach() {
		repo.deleteAll();
	}
	
	@Test
	void testValidationException() {
		repo.save(TestUtil.sourceEuroAccount(100));
		repo.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = new TransferRequest("account", "idontexist", "EUR", BigDecimal.valueOf(20));
		
		webTestClient.post()
		.uri(URL)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(request)
		.exchange()
		.expectStatus()
		.is4xxClientError()
		.expectHeader()
		.contentType(MediaType.APPLICATION_PROBLEM_JSON)
		.expectBody()
		.jsonPath("code")
		.isEqualTo("0100/0001")
		.jsonPath("status")
		.isEqualTo("BAD_REQUEST")
		.jsonPath("title")
		.isEqualTo("Account not found")
		;
	}
	
	@Test
	void testOk() {
		repo.save(TestUtil.sourceEuroAccount(100));
		repo.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = TestUtil.euroTransfer(35.765);
		
		webTestClient.post()
		.uri(URL)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(request)
		.exchange()
		.expectStatus()
		.is2xxSuccessful()
		.expectBody()
		.jsonPath("transactionId")
		.isNotEmpty()
		;
	}
	
	@Test
	void testNotEnoughMoney() {
		repo.save(TestUtil.sourceEuroAccount(100));
		repo.save(TestUtil.targetEuroAccount(0));
		TransferRequest request = TestUtil.euroTransfer(101);
		
		webTestClient.post()
		.uri(URL)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(request)
		.exchange()
		.expectStatus()
		.is4xxClientError()
		.expectHeader()
		.contentType(MediaType.APPLICATION_PROBLEM_JSON)
		.expectBody()
		.jsonPath("code")
		.isEqualTo("0200/0001")
		.jsonPath("status")
		.isEqualTo("BAD_REQUEST")
		.jsonPath("title")
		.isEqualTo("Balance is not enough")
		;
	}
	
}
