package com.chenile.puml.puml.ui;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InputModel {

    @NotBlank(message = "XML is mandatory")
    private String stmXml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "\n" +
            "<states>\n" +
            " <flow id='cart-flow' default='true'>\n" +
            "\n" +
            "  <manual-state id='CREATED' initialState='true' meta-mainPath=\"true\">\n" +
            "   <on eventId='close' newStateId='CLOSED'\n/>\n" +
            "   <on eventId='addItem' />\n" +
            "   <on eventId='userLogin' />\n" +
            "   <on eventId='initiatePayment' newStateId='PAYMENT_INITIATED' />\n" +
            "  </manual-state>\n" +
            "\n" +
            "  <manual-state id='PAYMENT_INITIATED'  meta-mainPath=\"true\">\n" +
            "   <on eventId=\"approve\"   meta-mainPath=\"true\"/>\n" +
            "   <on eventId=\"confirmPayment\" \n" +
            "     newStateId='TEST_STATE'   meta-mainPath=\"true\"/>\n" +
            "  </manual-state>\n" +
            "  \n" +
            "  <if id='TEST_STATE' condition='approved'\n" +
            "    then='confirm' else='reject'>\n" +
            "  <on eventId='confirm' newStateId='PAYMENT_CONFIRMED'  meta-mainPath=\"true\"/>\n" +
            "  <on eventId='reject' newStateId='PAYMENT_INITIATED'/>\n" +
            "      </if>\n" +
            "\n" +
            "  <manual-state id='PAYMENT_CONFIRMED'  meta-mainPath=\"true\"/>\n" +
            "  <manual-state id='CLOSED'/>\n" +
            " </flow>\n" +
            " \n" +
            "</states>";


    public String getEnablementProperties() {
        return enablementProperties;
    }

    public void setEnablementProperties(String enablementProperties) {
        this.enablementProperties = enablementProperties;
    }

    private String enablementProperties;

    public String getStylingProperties() {
        return stylingProperties;
    }

    public void setStylingProperties(String stylingProperties) {
        this.stylingProperties = stylingProperties;
    }

    private String stylingProperties;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String prefix;
    @NotBlank(message = "XML is mandatory")
    @Size(min = 1, max = 100)
    private String svg;

    public String getSvg() {
        return svg;
    }

    public void setSvg(String svg) {
        this.svg = svg;
    }

    public String getStmXml() {
        return stmXml;
    }

    public void setStmXml(String stmXml) {
        this.stmXml = stmXml;
    }
    private int testcaseId;


    @Override
    public String toString() {
        return "InputModel{" +
                "stmXml='" + stmXml + '\'' +
                ", svg='" + svg + '\'' +
                '}';
    }

    public int getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(int testcaseId) {
        this.testcaseId = testcaseId;
    }
}
