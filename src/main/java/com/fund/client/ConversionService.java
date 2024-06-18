package com.fund.client;

import java.math.BigDecimal;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fund.ExchangeRatesApiProperties;
import com.fund.exception.ConversionException;
import com.fund.exception.ConversionException.ConversionExceptionType;
import com.google.common.collect.ImmutableMap;

import reactor.core.publisher.Mono;

@Component
public final class ConversionService {

	private static final String KEY = "access_key";
	private static final String FROM = "from";
	private static final String TO = "to";
	private static final String AMOUNT = "amount";

	private static final Logger LOGGER = LoggerFactory.getLogger(ConversionService.class);

	private final WebClient webClient;
	private final ExchangeRatesApiProperties properties;

	@Autowired
	public ConversionService(WebClient webClient,
			ExchangeRatesApiProperties properties) {
		this.webClient = webClient;
		this.properties = properties;
	}

	public Mono<ConversionResponse> convertCurrency(String sourceCurrency, String targetCurrency, BigDecimal amount) {
		LOGGER.info("convertCurrency: [{}], [{}], [{}]", sourceCurrency, targetCurrency, amount);
		
			return webClient.get()
					.uri(builder -> builder.path(properties.getUrl())
							.queryParam(KEY, properties.getAccessKey())
							.queryParam(FROM, sourceCurrency)
							.queryParam(TO, targetCurrency)
							.queryParam(AMOUNT, amount)
							.build())
					.retrieve()
					.onStatus(status -> !(status.is2xxSuccessful()),
							clientErrorMonoFunction(sourceCurrency, targetCurrency))
					.bodyToMono(ConversionResponse.class);
	}
	
	private Function<ClientResponse, Mono<? extends Throwable>> clientErrorMonoFunction(String sourceCurrency, String targetCurrency) {
        return (ClientResponse clientResponse) -> {
        	return clientResponse.bodyToMono(String.class)
        			.flatMap(body -> {
        				return Mono.error(() -> new ConversionException(ConversionExceptionType.CONVERSION_ERROR,
                				ImmutableMap.of("error converting currency:", sourceCurrency, "target currency", targetCurrency, "error", body)));
        			});
        };
    }

}
