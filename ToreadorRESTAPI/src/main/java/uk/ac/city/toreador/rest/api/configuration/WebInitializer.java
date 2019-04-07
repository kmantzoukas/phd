package uk.ac.city.toreador.rest.api.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	 
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] {
                EverestDataSourceConfiguration.class,
                ToreadorDatasourceConfiguration.class,
                WebConfig.class,
                SpringConfiguration.class,
                SecurityConfiguration.class,
                EverestFluentDBDataSourceConfiguration.class};
    }
  
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }
  
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
 
}
