package uk.ac.city;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("load")
public class SparkServiceParameters {

  /**
   * Security property to be monitored. The supported properties are availability, integrity and privacy.
   */
    private String securityProperty;

    /**
     * Path of the location where the data to be processed is stored. This could be a distributed filesystem like HDFS.
     */
    private String input;

    /**
     * Path of the location where the data that will be produced will be stored. This could be a distributed filesystem like HDFS.
     */
    private String output;

    public SparkServiceParameters() {
    }

    public String getSecurityProperty() {
        return securityProperty;
    }

    public void setSecurityProperty(String securityProperty) {
        this.securityProperty = securityProperty;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
