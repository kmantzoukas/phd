package uk.ac.city.toreador.rest.api.toreador.entities;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "dslText",
        "composed",
        "lastTaskExecution",
        "status",
        "_links"
})
public class TaskDefinition {

    @JsonProperty("name")
    private String name;
    @JsonProperty("dslText")
    private String dslText;
    @JsonProperty("composed")
    private boolean composed;
    @JsonProperty("lastTaskExecution")
    private LastTaskExecution lastTaskExecution;
    @JsonProperty("status")
    private String status;
    @JsonProperty("_links")
    private Links links;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("dslText")
    public String getDslText() {
        return dslText;
    }

    @JsonProperty("dslText")
    public void setDslText(String dslText) {
        this.dslText = dslText;
    }

    @JsonProperty("composed")
    public boolean isComposed() {
        return composed;
    }

    @JsonProperty("composed")
    public void setComposed(boolean composed) {
        this.composed = composed;
    }

    @JsonProperty("lastTaskExecution")
    public LastTaskExecution getLastTaskExecution() {
        return lastTaskExecution;
    }

    @JsonProperty("lastTaskExecution")
    public void setLastTaskExecution(LastTaskExecution lastTaskExecution) {
        this.lastTaskExecution = lastTaskExecution;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("_links")
    public Links getLinks() {
        return links;
    }

    @JsonProperty("_links")
    public void setLinks(Links links) {
        this.links = links;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}