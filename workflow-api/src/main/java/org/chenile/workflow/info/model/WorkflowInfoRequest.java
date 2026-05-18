package org.chenile.workflow.info.model;

public class WorkflowInfoRequest {
    private String xmlText;
    private String stylingPropertiesText;
    private String enablementPropertiesText;
    private String prefix;

    public String getXmlText() {
        return xmlText;
    }

    public void setXmlText(String xmlText) {
        this.xmlText = xmlText;
    }

    public String getStylingPropertiesText() {
        return stylingPropertiesText;
    }

    public void setStylingPropertiesText(String stylingPropertiesText) {
        this.stylingPropertiesText = stylingPropertiesText;
    }

    public String getEnablementPropertiesText() {
        return enablementPropertiesText;
    }

    public void setEnablementPropertiesText(String enablementPropertiesText) {
        this.enablementPropertiesText = enablementPropertiesText;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
