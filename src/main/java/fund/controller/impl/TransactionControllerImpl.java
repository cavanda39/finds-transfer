package fund.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import fund.controller.TransactionController;
import fund.controller.request.TransferRequest;
import fund.controller.response.TransactionDTO;
import fund.service.ValidatorService;

@RestController
final class TransactionControllerImpl implements TransactionController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionControllerImpl.class);
	
	private final ValidatorService validatorService;
	
	public TransactionControllerImpl(ValidatorService validatorService) {
		this.validatorService = validatorService;
	}
	
	@Override
	public ResponseEntity<TransactionDTO> transferMoney(TransferRequest request) {
		LOGGER.info("transaferMoney()");
		validatorService.validateRequest(request);
		return ResponseEntity.ok().body(TransactionDTO.of("test"));
	}

}
