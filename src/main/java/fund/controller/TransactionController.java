package fund.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import fund.controller.request.TransferRequest;
import fund.controller.response.TransactionDTO;

@RequestMapping("api/v1/transaction")
public interface TransactionController {
	
	@PostMapping(value = "send", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<TransactionDTO> transferMoney(@RequestBody(required = true)TransferRequest request); 

}
