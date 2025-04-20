package org.chenile.workflow.puml;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Allows for styling the State Diagrams in Plant UML.
 * The state diagram can be styled using an XML Properties file or a normal properties
 * file if the delimiter is set to something other than double-equals
 *
 */
public class PumlStyler {

    public static final String EQUALS = "==";


    public static class StyleRules {
        public List<StyleRule> rules = new ArrayList<>();
    }
    public static class StyleRule {
        public String id;
        public String expression;
        public StyleElements style;
    }
    public static class StyleElements {
        public int thickness; // in pixels
        public String color; // any valid PUML color code or Hex code
        public String lineStyle;// can be dotted or bold
        public String border ;
    }

    public StyleRules styleRules = new StyleRules();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Use a json file to load the properties.
     * @param inputStream the input stream from the json file
     * @throws Exception if an error is encountered while processing the file
     */
    public void load(InputStream inputStream) throws Exception{
        this.styleRules = objectMapper.readValue(inputStream, StyleRules.class);
    }

    public void addRule(StyleRule rule) {
        this.styleRules.rules.add(rule);
    }

    public void addRules(InputStream inputStream) throws Exception{
        StyleRules styleRules = objectMapper.readValue(inputStream, StyleRules.class);
        this.styleRules.rules.addAll(styleRules.rules);
    }

    public void clear(){
        styleRules.rules.clear();
    }

    /**
     * In Plant UML state diagrams, if we have to change the style for a state then
     * we have to write a construct that looks like below:<br/>
     * <code>
     * state A #White##[bold]Black
     * </code><br/>
     * <p>In this example, White is the background color and Black is the border color.
     * Bold is the line style of the border line. </p>
     * This method emits the styling string (in the form "#White##[bold]Black" or a blank
     * string if no styling string is specified for the given metadata.
     * @param md metadata
     * @return a styling string of the form indicated above.
     */
    public String getStateStyle(Map<String,String> md){
        StyleRule rule = getStyle(md);
        if (rule == null) return "";
        StyleElements elements = rule.style;
        String s = " <<" + rule.id + ">> ";
        if(elements.lineStyle != null){
            s += " ##[" + elements.lineStyle + "]";
        }
        return s;
        /*
        String s = " ";
        if (elements.color != null)s += "#" + elements.color;
        boolean doubleHash = false;
        if (elements.lineStyle != null) {
            s += "##[" + elements.lineStyle + "]" ;
            doubleHash = true;
        }
        if (elements.border != null){
            if (!doubleHash) {
                s += "##";
                doubleHash = true;
            }
            s += elements.border;
        }
        s += " " ;
        return s;
         */
    }

    /**
     * In Plant UML state diagrams, if we have to change the style for a connection between
     * two states, we have to write a construct that looks like below:<br/>
     * <code>
     * A -[thickness=x,#white]-> B
     * </code><br/>
     * This method emits the styling string (in the form "[thickness=2,#white]" or a blank
     * string if no styling string is specified for the given metadata.
     * @param md metadata
     * @return a styling string of the form indicated above.
     */
    public String getConnectionStyle(Map<String,String> md) {
        StyleRule rule = getStyle(md);
        if (rule == null) return "";
        StyleElements elements = rule.style;
        return "[" +
                ((elements.thickness > 0)? "thickness=" + elements.thickness + "," : "")
                +((elements.color != null)?  "#" + elements.color:"")
                + "]";
    }

    private StyleRule getStyle(Map<String,String> md) {
        for (StyleRule rule : styleRules.rules) {
            String[] arr = rule.expression.split(EQUALS);
            if (arr[1].equals(md.get(arr[0]))) {
                return rule;
            }
        }
        return null;
    }
    private int ruleIndex = 1;
    public String generateStereoTypes(){
        StringBuilder s = new StringBuilder();
        for (StyleRule rule : styleRules.rules) {
            if (rule.id == null) rule.id = "__rule__" + ruleIndex++;

            if(rule.style.border != null){
                s.append("BorderColor<<").append(rule.id).append(">> ").append(rule.style.border).append("\n");
            }
            if(rule.style.color != null){
                s.append("BackgroundColor<<").append(rule.id).append(">> ").append(rule.style.color).append("\n");
            }
            if(rule.style.thickness != 0){
                s.append("BorderThickness<<").append(rule.id).append(">> ").append(rule.style.thickness).append("\n");
            }
            /**
            if(rule.style.lineStyle != null){
                s.append("BorderStyle<<").append(rule.id).append(">> ").append(rule.style.lineStyle).append("\n");
            }
            **/
        }
        return s.toString();
    }
}
