package uk.ac.city;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;

import java.io.*;
import java.util.Properties;

@SpringBootApplication
@EnableTask
@EnableConfigurationProperties({SparkServiceParameters.class,SparkSubmitServiceConfiguration.class})
public class ComputeAveragesService implements CommandLineRunner{

  private final static String TEMPLATE_PATH = "spark-submit-template.vm";
  private static Logger log = LoggerFactory.getLogger(ComputeAveragesService.class);

  @Autowired
  private SparkServiceParameters parameters;

  @Autowired
  private SparkSubmitServiceConfiguration serviceConfig;

  public static void main(String[] args) {

    SpringApplication.run(ComputeAveragesService.class, args);
  }

  @Override
  public void run(String... args) throws Exception {

      Properties properties = new Properties();
      properties.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
      properties.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
      properties.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
      properties.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
      VelocityEngine engine = new VelocityEngine(properties);

      VelocityContext context = new VelocityContext();

      Template template = engine.getTemplate(TEMPLATE_PATH);
      context.put("parameters", parameters);
      context.put("appName", serviceConfig.getAppName());
      context.put("appClass", serviceConfig.getAppClass());
      context.put("appJar", serviceConfig.getAppJar());
      context.put("master", serviceConfig.getMaster());

      StringWriter writer = new StringWriter();
      template.merge(context,writer);

      File executable = File.createTempFile("toreador-spark-submit@", ".sh");

      log.info("----> " + executable.getCanonicalFile());

      FileUtils.writeStringToFile(executable, writer.toString().replaceAll("\r\n", "\n"), "UTF-8");

  }

}
