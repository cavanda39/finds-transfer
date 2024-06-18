package com.fund.problem;

import java.net.URI;
import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.ImmutableMap;

public interface Problem {
	
	ResponseEntity<Problem> toResponseEntity();
	
	HttpStatus getStatus();
	
	URI getType();

	String getInstance();

	String getTitle();
	
	String getDetail();

	ImmutableMap<String, Object> getParams();

	String getCode();
	
	OffsetDateTime getTimestamp();

}
