package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LastTaskExecution {

    @JsonProperty("executionId")
    private int executionId;
    @JsonProperty("exitCode")
    private int exitCode;
    @JsonProperty("taskName")
    private String taskName;
    @JsonProperty("startTime")
    private String startTime;
    @JsonProperty("endTime")
    private String endTime;
    @JsonProperty("exitMessage")
    private Object exitMessage;
    @JsonProperty("arguments")
    private List<String> arguments = null;
    @JsonProperty("jobExecutionIds")
    private Object jobExecutionIds;
    @JsonProperty("errorMessage")
    private Object errorMessage;
    @JsonProperty("externalExecutionId")
    private String externalExecutionId;
    @JsonProperty("taskExecutionStatus")
    private String taskExecutionStatus;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("executionId")
    public int getExecutionId() {
        return executionId;
    }

    @JsonProperty("executionId")
    public void setExecutionId(int executionId) {
        this.executionId = executionId;
    }

    @JsonProperty("exitCode")
    public int getExitCode() {
        return exitCode;
    }

    @JsonProperty("exitCode")
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @JsonProperty("taskName")
    public String getTaskName() {
        return taskName;
    }

    @JsonProperty("taskName")
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @JsonProperty("startTime")
    public String getStartTime() {
        return startTime;
    }

    @JsonProperty("startTime")
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @JsonProperty("endTime")
    public String getEndTime() {
        return endTime;
    }

    @JsonProperty("endTime")
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @JsonProperty("exitMessage")
    public Object getExitMessage() {
        return exitMessage;
    }

    @JsonProperty("exitMessage")
    public void setExitMessage(Object exitMessage) {
        this.exitMessage = exitMessage;
    }

    @JsonProperty("arguments")
    public List<String> getArguments() {
        return arguments;
    }

    @JsonProperty("arguments")
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    @JsonProperty("jobExecutionIds")
    public Object getJobExecutionIds() {
        return jobExecutionIds;
    }

    @JsonProperty("jobExecutionIds")
    public void setJobExecutionIds(Object jobExecutionIds) {
        this.jobExecutionIds = jobExecutionIds;
    }

    @JsonProperty("errorMessage")
    public Object getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty("errorMessage")
    public void setErrorMessage(Object errorMessage) {
        this.errorMessage = errorMessage;
    }

    @JsonProperty("externalExecutionId")
    public String getExternalExecutionId() {
        return externalExecutionId;
    }

    @JsonProperty("externalExecutionId")
    public void setExternalExecutionId(String externalExecutionId) {
        this.externalExecutionId = externalExecutionId;
    }

    @JsonProperty("taskExecutionStatus")
    public String getTaskExecutionStatus() {
        return taskExecutionStatus;
    }

    @JsonProperty("taskExecutionStatus")
    public void setTaskExecutionStatus(String taskExecutionStatus) {
        this.taskExecutionStatus = taskExecutionStatus;
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