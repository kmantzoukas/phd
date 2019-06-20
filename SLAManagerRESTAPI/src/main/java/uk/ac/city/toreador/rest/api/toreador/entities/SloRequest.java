package uk.ac.city.toreador.rest.api.toreador.entities;

import java.util.Map;

public class SloRequest {

    private Asset asset;
    private SecurityProperty securityProperty;
    private Slotemplate sloTemplate;
    private Map<String, Object > parameters;
    private Boolean last;

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public SecurityProperty getSecurityProperty() {
        return securityProperty;
    }

    public void setSecurityProperty(SecurityProperty securityProperty) {
        this.securityProperty = securityProperty;
    }

    public Slotemplate getSloTemplate() {
        return sloTemplate;
    }

    public void setSloTemplate(Slotemplate sloTemplate) {
        this.sloTemplate = sloTemplate;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Boolean isLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }
}
