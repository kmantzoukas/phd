package uk.ac.city;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:scdf.properties")
@ConfigurationProperties(prefix = "scdf")
public class SCDFConfigurationProperties {


    private String host;
    private int port;
    private String taskDefinitionUrl;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTaskDefinitionUrl() {
        return taskDefinitionUrl;
    }

    public void setTaskDefinitionUrl(String taskDefinitionUrl) {
        this.taskDefinitionUrl = taskDefinitionUrl;
    }
}