package fund;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties({ExchangeRatesApiProperties.class})
public class FundAutoConfiguration {
	
	@Bean
    WebClient webClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        return webClientBuilder.exchangeStrategies(
                ExchangeStrategies.builder().codecs(configurer -> {
                    configurer.customCodecs().register(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON, MediaType.APPLICATION_PROBLEM_JSON));
                }).build())
                .build();
    }
	
}
