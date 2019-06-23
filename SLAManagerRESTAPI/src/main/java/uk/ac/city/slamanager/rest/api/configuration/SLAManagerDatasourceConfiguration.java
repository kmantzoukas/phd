package uk.ac.city.slamanager.rest.api.configuration;

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

@EnableJpaRepositories(basePackages = { "uk.ac.city.slamanager.rest.api.repositories" },
        entityManagerFactoryRef = "slamanagerEntityManagerFactory",
        transactionManagerRef = "slamanagerTransactionManager")
@PropertySource({"classpath:slamanager.db.properties"})
public class SLAManagerDatasourceConfiguration {

	private int maxUploadSizeInMb = 5 * 1024 * 1024;
	private static final Logger logger = Logger.getLogger(SLAManagerDatasourceConfiguration.class.getName());

	@Autowired
	Environment env;

	@Primary
	@Bean
	public DataSource slamanagerDataSource() throws PropertyVetoException {

		ComboPooledDataSource cpds = new ComboPooledDataSource();

		cpds.setDriverClass(env.getProperty("db.driver"));
		cpds.setJdbcUrl(env.getProperty("db.url"));
		cpds.setUser(env.getProperty("db.username"));
		cpds.setPassword(env.getProperty("db.password"));

		return cpds;
	}

	@Primary
	@Bean
	public LocalContainerEntityManagerFactoryBean slamanagerEntityManagerFactory()
			throws PropertyVetoException {

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabase(Database.MYSQL);
		vendorAdapter.setShowSql(Boolean.FALSE);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setPackagesToScan(new String[] { "uk.ac.city.slamanager.rest.api.entities" });
		factory.setDataSource(slamanagerDataSource());
		factory.setJpaVendorAdapter(vendorAdapter);

		return factory;
	}

	@Primary
	@Bean
	public PlatformTransactionManager slamanagerTransactionManager() throws PropertyVetoException {

		final JpaTransactionManager txManager = new JpaTransactionManager();

		txManager.setEntityManagerFactory(slamanagerEntityManagerFactory().getObject());
		txManager.setDataSource(slamanagerDataSource());

		return txManager;
	}
}