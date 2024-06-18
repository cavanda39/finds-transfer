package com.fund.client;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ConversionResponse {
	
	private ConversionError error;
	private double result;

	public double getResult() {
		return result;
	}

	public void setResult(double result) {
		this.result = result;
	}

	public ConversionError getError() {
		return error;
	}

	public void setError(ConversionError error) {
		this.error = error;
	}
	
}
