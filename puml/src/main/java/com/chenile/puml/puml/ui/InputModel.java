package com.chenile.puml.puml.ui;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InputModel {

    @NotBlank(message = "XML is mandatory")
    private String stmXml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "\n" +
            "<states>\n" +
            " <flow id='CART_FLOW' default='true'>\n" +
            "\n" +
            "  <manual-state id='CREATED' initialState='true' >\n" +
            "   <on eventId='close' newStateId='CLOSED'\n/>\n" +
            "   <on eventId='addItem' />\n" +
            "   <on eventId='userLogin' />\n" +
            "   <on eventId='initiatePayment' newStateId='PAYMENT_INITIATED' />\n" +
            "  </manual-state>\n" +
            "\n" +
            "  <manual-state id='PAYMENT_INITIATED' >\n" +
            "   <on eventId=\"approve\" />\n" +
            "   <on eventId=\"confirmPayment\" \n" +
            "     newStateId='TEST_STATE' />\n" +
            "  </manual-state>\n" +
            "  \n" +
            "  <if id='TEST_STATE' condition='approved'\n" +
            "    then='confirm' else='reject'>\n" +
            "  <on eventId='confirm' newStateId='PAYMENT_CONFIRMED' />\n" +
            "  <on eventId='reject' newStateId='PAYMENT_INITIATED'/>\n" +
            "      </if>\n" +
            "\n" +
            "  <manual-state id='PAYMENT_CONFIRMED' />\n" +
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

    private String enablementProperties = """
            cart.CART_FLOW.PAYMENT_INITIATED.meta.some_name=some_value
            cart.state.add.XXX.in=CART_FLOW
            cart.CART_FLOW.PAYMENT_INITIATED.confirmPayment.meta.some_name=some_value
            """;

    public String getStylingProperties() {
        return stylingProperties;
    }

    public void setStylingProperties(String stylingProperties) {
        this.stylingProperties = stylingProperties;
    }

    private String stylingProperties = """
            {
               "rules": [
               {
                "expression": "some_name==some_value",
                "style": {"color": "Fuchsia","thickness": 2.5}
               }
              ]
            }
            """;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String prefix = "cart";
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
