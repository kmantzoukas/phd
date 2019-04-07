package uk.ac.city.toreador.rest.api.configuration;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.util.Properties;
import java.util.logging.Logger;

@ComponentScan(basePackages = { "uk.ac.city.toreador.rest.api" })
@Configuration
public class SpringConfiguration {

	private int maxUploadSizeInMb = 5 * 1024 * 1024;
	private static final Logger logger = Logger.getLogger(SpringConfiguration.class.getName());


	@Autowired
	Environment env;

	@Bean
	public VelocityEngine velocityEngine() {
		Properties properties = new Properties();
		properties.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
		properties.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
		properties.setProperty(RuntimeConstants.RESOURCE_LOADER, "file,classpath");
		properties.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		VelocityEngine engine = new VelocityEngine(properties);

		return engine;
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		
		CommonsMultipartResolver cmr = new CommonsMultipartResolver();
		cmr.setMaxUploadSize(maxUploadSizeInMb);
		
		return cmr;
	}

}