package fund.exception;

import com.google.common.collect.ImmutableMap;

import fund.exception.common.BusinessException;
import fund.exception.common.ExceptionType;

public class AccountException extends BusinessException{

	private static final long serialVersionUID = 7806603299160766195L;

	public enum AccountExceptionType implements ExceptionType {
		
		NEGATIVE_AMOUNT_ERROR("0200", "0001", "Balance is not enough");

        private final String category;
        private final String code;
        private final String message;

        AccountExceptionType(String category, String code, String message) {
            this.code = code;
            this.category = category;
            this.message = message;
        }
	}
	
	public AccountException(ExceptionType type, ImmutableMap<String, Object> params) {
		super(type, params);
	}
	
	public String getCode() {
        return ((AccountException.AccountExceptionType)this.getType()).code;
    }

    public String getCategory() {
        return ((AccountException.AccountExceptionType)this.getType()).category;
    }

    public String getMessage() {
        return ((AccountException.AccountExceptionType)this.getType()).message;
    }

}
