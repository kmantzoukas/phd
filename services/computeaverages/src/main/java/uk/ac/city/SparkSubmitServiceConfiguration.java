package uk.ac.city;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spark")
public class SparkSubmitServiceConfiguration {

    private String appName = "ComputeAveragesService";

    private String appClass = "uk.ac.city.services.ComputeAverage";

    private String appJar = "/home/abfc149/computeaverage_2.11-0.1-SNAPSHOT.jar";

    private String master = "spark://10.207.1.102:7077";

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppClass() {
        return appClass;
    }

    public void setAppClass(String appClass) {
        this.appClass = appClass;
    }

    public String getAppJar() {
        return appJar;
    }

    public void setAppJar(String appJar) {
        this.appJar = appJar;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }
}
