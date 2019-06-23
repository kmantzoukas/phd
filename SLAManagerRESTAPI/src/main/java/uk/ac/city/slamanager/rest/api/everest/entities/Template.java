package uk.ac.city.slamanager.rest.api.everest.entities;

import javax.persistence.*;
import java.sql.Blob;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "template", catalog = "everest")
public class Template {

    private Integer templatePK;
    private String templateId;
    private String formulaId;
    private Integer mode;
    private String status;
    private String type;
    private Integer active;
    private Integer removable;
    private Integer updated;
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

    @Column(name = "templateId", unique = true, nullable = false)
    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Column(name = "formulaId", unique = true, nullable = false)
    public String getFormulaId() {
        return formulaId;
    }

    public void setFormulaId(String formulaId) {
        this.formulaId = formulaId;
    }

    @Column(name = "mode", unique = true, nullable = false)
    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    @Column(name = "status", unique = true, nullable = false)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "type", unique = true, nullable = false)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "active", unique = true, nullable = false)
    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    @Column(name = "removable", unique = true, nullable = false)
    public Integer getRemovable() {
        return removable;
    }

    public void setRemovable(Integer removable) {
        this.removable = removable;
    }

    @Column(name = "updated", unique = true, nullable = false)
    public Integer getUpdated() {
        return updated;
    }

    public void setUpdated(Integer updated) {
        this.updated = updated;
    }

    @Column(name = "templateObject", unique = true, nullable = false)
    public Blob getTemplateObject() {
        return templateObject;
    }

    public void setTemplateObject(Blob templateObject) {
        this.templateObject = templateObject;
    }

    @Column(name = "templateString", unique = true, nullable = false)
    public String getTemplateString() {
        return templateString;
    }

    public void setTemplateString(String templateString) {
        this.templateString = templateString;
    }
}
