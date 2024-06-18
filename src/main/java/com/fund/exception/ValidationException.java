package com.fund.exception;

import com.fund.exception.common.BusinessException;
import com.fund.exception.common.ExceptionType;
import com.google.common.collect.ImmutableMap;

public class ValidationException extends BusinessException {

	private static final long serialVersionUID = -1062710838679984120L;
	
	public enum ValidationExceptionType implements ExceptionType {
		
		ACCOUNT_NOT_FOUND_ERROR("0100", "0001", "Account not found");

        private final String category;
        private final String code;
        private final String message;

        ValidationExceptionType(String category, String code, String message) {
            this.code = code;
            this.category = category;
            this.message = message;
        }
	}

	public ValidationException(ExceptionType type, ImmutableMap<String, Object> params) {
		super(type, params);
	}
	
	public String getCode() {
        return ((ValidationExceptionType)this.getType()).code;
    }

    public String getCategory() {
        return ((ValidationExceptionType)this.getType()).category;
    }

    public String getMessage() {
        return ((ValidationExceptionType)this.getType()).message;
    }

}
