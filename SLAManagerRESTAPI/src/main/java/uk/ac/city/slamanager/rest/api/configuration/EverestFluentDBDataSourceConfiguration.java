package uk.ac.city.slamanager.rest.api.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.logging.Logger;

@Configuration
@PropertySource({"classpath:everestfluent.db.properties"})
public class EverestFluentDBDataSourceConfiguration {

	private int maxUploadSizeInMb = 5 * 1024 * 1024;
	private static final Logger logger = Logger.getLogger(EverestFluentDBDataSourceConfiguration.class.getName());


	@Autowired
	Environment env;

	@Bean
	public DataSource everestDataSourceBean() throws PropertyVetoException {

		DriverManagerDataSource dataSource = new DriverManagerDataSource();

		dataSource.setDriverClassName(env.getProperty("db.driver"));
		dataSource.setUrl(env.getProperty("db.url"));
		dataSource.setUsername(env.getProperty("db.username"));
		dataSource.setPassword(env.getProperty("db.password"));

		return dataSource;
	}

	@Bean
	JdbcTemplate jdbcTemplate(DataSource source){
		return new JdbcTemplate(source);
	}
}