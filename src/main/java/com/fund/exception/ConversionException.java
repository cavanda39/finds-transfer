package com.fund.exception;

import com.fund.exception.ValidationException.ValidationExceptionType;
import com.fund.exception.common.BusinessException;
import com.fund.exception.common.ExceptionType;
import com.google.common.collect.ImmutableMap;

public class ConversionException extends BusinessException {

	private static final long serialVersionUID = -1895311380336814407L;

	public enum ConversionExceptionType implements ExceptionType {

		CONVERSION_ERROR("0300", "0001", "Error converting currency");

		private final String category;
		private final String code;
		private final String message;

		ConversionExceptionType(String category, String code, String message) {
			this.code = code;
			this.category = category;
			this.message = message;
		}
	}

	public ConversionException(ExceptionType type, ImmutableMap<String, Object> params, Throwable cause) {
		super(type, params, cause);
	}

	public ConversionException(ExceptionType type, ImmutableMap<String, Object> params) {
		super(type, params);
	}

	public String getCode() {
		return ((ConversionExceptionType) this.getType()).code;
	}

	public String getCategory() {
		return ((ConversionExceptionType) this.getType()).category;
	}

	public String getMessage() {
		return ((ConversionExceptionType) this.getType()).message;
	}

}
