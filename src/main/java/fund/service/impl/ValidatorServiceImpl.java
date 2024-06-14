package fund.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fund.domain.AccountRepository;
import fund.service.ValidatorService;

@Service
@Transactional
final class ValidatorServiceImpl implements ValidatorService {
	
	@Autowired
	private AccountRepository accountRepository;
	
	public void validateRequest() {
		
	}

}
