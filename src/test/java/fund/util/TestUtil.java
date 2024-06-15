package fund.util;

import java.math.BigDecimal;

import fund.controller.request.TransferRequest;
import fund.domain.Account;

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

}
