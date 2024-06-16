package fund.client;

import java.math.BigDecimal;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import com.google.common.collect.ImmutableMap;

import fund.ExchangeRatesApiProperties;
import fund.exception.ConversionException;
import fund.exception.ConversionException.ConversionExceptionType;
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
							getClientResponseMonoFunction(sourceCurrency, targetCurrency))
					.bodyToMono(ConversionResponse.class);
//					.flatMap(response -> {
//						if(response.getError() != null) {
//							LOGGER.error("conversion process not successful: [{}], [{}]", response.getError().getCode(),
//									response.getError().getInfo());
//							return Mono.error(() -> 
//							new ConversionException(ConversionExceptionType.CONVERSION_ERROR,
//									ImmutableMap.of("error converting currency:", sourceCurrency, "target currency",
//									targetCurrency, "error message", response.getError().getInfo()))
//									);
//						}
//						return Mono.just(BigDecimal.valueOf(response.getResult()));
//					});
					
		

//		UriComponentsBuilder uriBuilder = UriComponentsBuilder
//				.fromHttpUrl(properties.getUrl())
//				.queryParam(KEY, properties.getAccessKey())
//				.queryParam(FROM, sourceCurrency)
//				.queryParam(TO, targetCurrency)
//				.queryParam(AMOUNT, amount);
//
//		try {
//			ConversionResponse response = restTemplate.getForObject(uriBuilder.toUriString(), ConversionResponse.class);
//			LOGGER.info("conversion successful: [{}]", response.getResult());
//			return response;
//		} catch (HttpClientErrorException e) {
//			LOGGER.error("unexpected error: [{}]", e.getMessage());
//			throw new ConversionException(ConversionExceptionType.CONVERSION_ERROR,
//					ImmutableMap.of("error converting currency:", sourceCurrency, "target currency", targetCurrency),
//					e);
//		}
	}
	
	private Function<ClientResponse, Mono<? extends Throwable>> getClientResponseMonoFunction(String sourceCurrency, String targetCurrency) {
        return (ClientResponse clientResponse) -> {
        	return clientResponse.bodyToMono(String.class)
        			.flatMap(body -> {
        				return Mono.error(() -> new ConversionException(ConversionExceptionType.CONVERSION_ERROR,
                				ImmutableMap.of("error converting currency:", sourceCurrency, "target currency", targetCurrency, "error", body)));
        			});
        };
    }

}
