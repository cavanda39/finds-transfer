package fund;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({ExchangeRatesApiProperties.class})
class FundAutoConfiguration {
	
	@Bean
	public RestTemplate restTemplateExchangeratesapi() {
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
	}

}
