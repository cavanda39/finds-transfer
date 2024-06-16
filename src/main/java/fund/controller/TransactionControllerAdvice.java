package fund.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import fund.exception.AccountException;
import fund.exception.ConversionException;
import fund.exception.ValidationException;
import fund.exception.common.BusinessException;
import fund.problem.Problem;
import fund.problem.ProblemBuilder;
import reactor.core.publisher.Mono;

@RestControllerAdvice(assignableTypes = TransactionController.class)
public final class TransactionControllerAdvice {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionControllerAdvice.class);
	
	@ExceptionHandler(ValidationException.class)
	public Mono<ResponseEntity<Problem>> on(ValidationException ex, ServerWebExchange exchange) {
		LOGGER.error("ValidationException: [{}]", ex.toString());
		String code = ex.getCategory().concat("/").concat(ex.getCode());
		return buildProblem(code, HttpStatus.BAD_REQUEST, ex, exchange);
	}
	
	@ExceptionHandler(AccountException.class)
	public Mono<ResponseEntity<Problem>> on(AccountException ex, ServerWebExchange exchange) {
		LOGGER.error("AccountException: [{}]", ex.toString());
		String code = ex.getCategory().concat("/").concat(ex.getCode());
		return buildProblem(code, HttpStatus.BAD_REQUEST, ex, exchange);
	}
	
	@ExceptionHandler(ConversionException.class)
	public Mono<ResponseEntity<Problem>> on(ConversionException ex, ServerWebExchange exchange) {
		LOGGER.error("ConversionException: [{}]", ex.toString());
		String code = ex.getCategory().concat("/").concat(ex.getCode());
		return buildProblem(code, HttpStatus.INTERNAL_SERVER_ERROR, ex, exchange);
	}
	
	private Mono<ResponseEntity<Problem>> buildProblem(String code, HttpStatus status, BusinessException ex, ServerWebExchange exchange){
		String title = ex.getMessage();
		String detail = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
		
		return Mono.just(new ProblemBuilder()
				.withType(exchange.getRequest().getURI())
				.withTitle(title)
				.with(ex.getParams())
				.withStatus(status)
				.withCode(code)
				.withDetail(detail)
				.build()
				.toResponseEntity());
	}
	
}
