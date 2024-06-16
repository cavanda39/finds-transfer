package fund.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import fund.controller.request.TransferRequest;
import fund.controller.response.TransactionDTO;
import fund.domain.AccountRepository;
import fund.util.TestUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerTest {
	
	private static final String URL = "/api/v1/transaction/send";
	
	@Autowired
    private TestRestTemplate template;
	
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
		TransferRequest request = new TransferRequest("account", "idontexist", null, BigDecimal.valueOf(20));
		
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
