package uk.ac.city.slamanager.rest.api.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	 
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] {
                EverestDataSourceConfiguration.class,
                SLAManagerDatasourceConfiguration.class,
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
