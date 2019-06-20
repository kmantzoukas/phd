package uk.ac.city.toreador.rest.api.configuration;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.logging.Logger;

@EnableJpaRepositories(basePackages = { "uk.ac.city.toreador.rest.api.toreador.repositories" },
        entityManagerFactoryRef = "toreadorEntityManagerFactory",
        transactionManagerRef = "toreadorTransactionManager")
@PropertySource({"classpath:toreador.db.properties"})
public class ToreadorDatasourceConfiguration {

	private int maxUploadSizeInMb = 5 * 1024 * 1024;
	private static final Logger logger = Logger.getLogger(ToreadorDatasourceConfiguration.class.getName());

	@Autowired
	Environment env;

	@Primary
	@Bean
	public DataSource toreadorDataSourceBean() throws PropertyVetoException {

		ComboPooledDataSource cpds = new ComboPooledDataSource();

		cpds.setDriverClass(env.getProperty("db.driver"));
		cpds.setJdbcUrl(env.getProperty("db.url"));
		cpds.setUser(env.getProperty("db.username"));
		cpds.setPassword(env.getProperty("db.password"));

		return cpds;
	}

	@Primary
	@Bean
	public LocalContainerEntityManagerFactoryBean toreadorEntityManagerFactory()
			throws PropertyVetoException {

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabase(Database.MYSQL);
		vendorAdapter.setShowSql(Boolean.FALSE);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setPackagesToScan(new String[] { "uk.ac.city.toreador.rest.api.toreador.entities" });
		factory.setDataSource(toreadorDataSourceBean());
		factory.setJpaVendorAdapter(vendorAdapter);

		return factory;
	}

	@Primary
	@Bean
	public PlatformTransactionManager toreadorTransactionManager() throws PropertyVetoException {

		final JpaTransactionManager txManager = new JpaTransactionManager();

		txManager.setEntityManagerFactory(toreadorEntityManagerFactory().getObject());
		txManager.setDataSource(toreadorDataSourceBean());

		return txManager;
	}
}