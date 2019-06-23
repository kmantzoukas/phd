package uk.ac.city.slamanager.rest.api.everest.entities;


import javax.persistence.*;
import java.sql.Blob;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "templatedefined", catalog = "everest")
public class TemplateDefined {

    private Integer templatePK;
    private String templateId;
    private String formulaId;
    private Integer mode;
    private String status;
    private String type;
    private Boolean active;
    private Boolean removable;
    private Boolean updated;
    private Blob templateObject;
    private String templateString;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "templatePK", unique = true, nullable = false)
    public Integer getTemplatePK() {
        return templatePK;
    }

    public void setTemplatePK(Integer templatePK) {
        this.templatePK = templatePK;
    }

    @Column(name = "templateId")
    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Column(name = "formulaId")
    public String getFormulaId() {
        return formulaId;
    }

    public void setFormulaId(String formulaId) {
        this.formulaId = formulaId;
    }

    @Column(name = "mode")
    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "active")
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Column(name = "removable")
    public Boolean getRemovable() {
        return removable;
    }

    public void setRemovable(Boolean removable) {
        this.removable = removable;
    }

    @Column(name = "updated")
    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }

    @Column(name = "templateObject")
    public Blob getTemplateObject() {
        return templateObject;
    }

    public void setTemplateObject(Blob templateObject) {
        this.templateObject = templateObject;
    }

    @Column(name = "templateString")
    public String getTemplateString() {
        return templateString;
    }

    public void setTemplateString(String templateString) {
        this.templateString = templateString;
    }
}
