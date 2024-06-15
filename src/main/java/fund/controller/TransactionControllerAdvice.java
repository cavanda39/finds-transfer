package fund.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fund.exception.AccountException;
import fund.exception.ValidationException;
import fund.exception.common.BusinessException;
import fund.problem.Problem;
import fund.problem.ProblemBuilder;
import fund.exception.ConversionException;

@RestControllerAdvice(assignableTypes = TransactionController.class)
public final class TransactionControllerAdvice {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionControllerAdvice.class);
	
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<Problem> on(ValidationException ex, HttpServletRequest request) {
		LOGGER.error("ValidationException: [{}]", ex.toString());
		String code = ex.getCategory().concat("/").concat(ex.getCode());
		return buildProblem(code, HttpStatus.BAD_REQUEST, ex, request);
	}
	
	@ExceptionHandler(AccountException.class)
	public ResponseEntity<Problem> on(AccountException ex, HttpServletRequest request) {
		LOGGER.error("AccountException: [{}]", ex.toString());
		String code = ex.getCategory().concat("/").concat(ex.getCode());
		return buildProblem(code, HttpStatus.BAD_REQUEST, ex, request);
	}
	
	@ExceptionHandler(ConversionException.class)
	public ResponseEntity<Problem> on(ConversionException ex, HttpServletRequest request) {
		LOGGER.error("ConversionException: [{}]", ex.toString());
		String code = ex.getCategory().concat("/").concat(ex.getCode());
		return buildProblem(code, HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
	}
	
	private ResponseEntity<Problem> buildProblem(String code, HttpStatus status, BusinessException ex, HttpServletRequest request){
		String title = ex.getMessage();
		String detail = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
		
		return new ProblemBuilder()
				.with(request)
				.with(request)
				.withType(URI.create(request.getRequestURI()))
				.withTitle(title)
				.with(ex.getParams())
				.withStatus(status)
				.withCode(code)
				.withDetail(detail)
				.build()
				.toResponseEntity();
	}
	
}
