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
    public String equals = EQUALS;

    public static class StyleRules {
        public List<StyleRule> rules = new ArrayList<>();
    }
    public static class StyleRule {
        public String expression;
        public StyleElements style;
    }
    public static class StyleElements {
        public int thickness; // in pixels
        public String color; // any valid PUML color code or Hex code
        public String lineStyle;// can be dotted or bold
    }

    public StyleRules styleRules = new StyleRules();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Use a json file to load the properties. The equals delimiter is switched to : automatically
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
        StyleElements elements = getStyle(md);
        if (elements == null) return "";
        String s = " ";
        if (elements.color != null)s += "#" + elements.color;
        if (elements.lineStyle != null) s += "##[" + elements.lineStyle + "]";
        s += " " ;
        return s;
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
        StyleElements elements = getStyle(md);
        if (elements == null) return "";
        return "[" +
                ((elements.thickness > 0)? "thickness=" + elements.thickness + "," : "")
                +((elements.color != null)?  "#" + elements.color:"")
                + "]";
    }

    private StyleElements getStyle(Map<String,String> md) {
        for (StyleRule rule : styleRules.rules) {
            String[] arr = rule.expression.split(equals);
            if (arr[1].equals(md.get(arr[0]))) {
                return rule.style;
            }
        }
        return null;
    }
}
