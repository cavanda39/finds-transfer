package fund.client;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.ImmutableMap;

import fund.ExchangeRatesApiProperties;
import fund.exception.ConversionException;
import fund.exception.ConversionException.ConversionExceptionType;

@Component
public final class ConversionService {

	private static final String KEY = "access_key";
	private static final String FROM = "from";
	private static final String TO = "to";
	private static final String AMOUNT = "amount";

	private static final Logger LOGGER = LoggerFactory.getLogger(ConversionService.class);

	private final RestTemplate restTemplate;
	private final ExchangeRatesApiProperties properties;

	@Autowired
	public ConversionService(@Qualifier("restTemplateExchangeratesapi") RestTemplate restTemplate,
			ExchangeRatesApiProperties properties) {
		this.restTemplate = restTemplate;
		this.properties = properties;
	}

	public ConversionResponse convertCurrency(String sourceCurrency, String targetCurrency, BigDecimal amount) {
		LOGGER.info("convertCurrency: [{}], [{}], [{}]", sourceCurrency, targetCurrency, amount);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl(properties.getUrl())
				.queryParam(KEY, properties.getAccessKey())
				.queryParam(FROM, sourceCurrency)
				.queryParam(TO, targetCurrency)
				.queryParam(AMOUNT, amount);

		try {
			ConversionResponse response = restTemplate.getForObject(uriBuilder.toUriString(), ConversionResponse.class);
			LOGGER.info("conversion successful: [{}]", response.getResult());
			return response;
		} catch (HttpClientErrorException e) {
			LOGGER.error("unexpected error: [{}]", e.getMessage());
			throw new ConversionException(ConversionExceptionType.CONVERSION_ERROR,
					ImmutableMap.of("error converting currency:", sourceCurrency, "target currency", targetCurrency),
					e);
		}
	}

}
