package fund.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import fund.controller.request.TransferRequest;
import fund.domain.Account;
import fund.domain.AccountRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerTest {
	
	private static final String URL = "/api/v1/transaction/send";
	
	@Autowired
    private TestRestTemplate template;
	
	@Autowired
	private AccountRepository repo;
	
	@Test
	void testValidationException() {
		repo.save(Account.of("account", BigDecimal.valueOf(100), 1));
		repo.save(Account.of("target", BigDecimal.valueOf(100), 1));
		TransferRequest request = new TransferRequest("account", "tatrget", null, BigDecimal.valueOf(20));
		HttpEntity<TransferRequest> entity = new HttpEntity<TransferRequest>(request, new HttpHeaders());
		ResponseEntity<Map> result = template.exchange(URL, HttpMethod.POST, entity, Map.class);
		Map<String, Object> problem = result.getBody();
		assertTrue(problem.get("code").equals("0100/0001"));
		assertTrue(problem.get("status").equals("BAD_REQUEST"));
		assertTrue(problem.get("title").equals("Account not found"));
		
	}

}
