/**
 * 
 */
package fund.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import fund.infrastructure.hibernate.HibernateSchema;


/**
 * @author a.polcaro
 *
 */
@Profile("dev")
@Configuration
class DbConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DbConfig.class);

	@Bean
	protected CommandLineRunner checkSchema(){
		return (args)->{
			final StringBuilder builder = new StringBuilder();
			builder.append(System.lineSeparator());
			builder.append("*************************************").append(System.lineSeparator());
			builder.append("*            UPDATE SCRIPT          *").append(System.lineSeparator());
			builder.append("*************************************").append(System.lineSeparator());
			builder.append(System.lineSeparator());
			final boolean hasChanges = HibernateSchema.generateUpdateScriptUsing(builder);
			if (hasChanges) {
				LOGGER.warn(builder.toString());
			}
		};
	}
	
}
