package uk.ac.city;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:rest.properties")
@ConfigurationProperties(prefix = "rest")
public class RestTemplateProperties {
    /*
    * Username for the basic authentication
    */
    private String username;
    /*
    * Password for the basic authentication
    */
    private String password;
    /*
    * Host where the REST api is accepting requests for the creation of the SLA project
    */
    private String host;
    /*
    * Port for the REST api services
    */
    private int port;
    /*
    * URL where the post operation should be performed to create the SLA project
    */
    private String url;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
