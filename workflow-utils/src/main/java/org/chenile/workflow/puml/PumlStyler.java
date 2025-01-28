package org.chenile.workflow.puml;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class PumlStyler {
    public static class StyleElements {
        public int thickness; // in pixels
        public String color; // any valid PUML color code or Hex code
        public String lineStyle;// can be dotted or bold
        public String toString() {
            try {
                return objectMapper.writeValueAsString(this);
            }catch(Exception e) { return "";}
        }
    }
    private static final ObjectMapper objectMapper = new ObjectMapper();
    Properties props = new Properties();

    public void loadFromXML(InputStream inputStream) throws Exception{
        props.loadFromXML(inputStream);
    }

    public void setStyle(String expr, String value){
        props.setProperty(expr,value);
    }

    public void clear(){
        props.clear();
    }

    public void loadFromXML(String s) throws Exception{
         loadFromXML(new ByteArrayInputStream(s.getBytes()));
    }

    public String getStyle(StateDescriptor sd){
        StyleElements elements = null;
        elements = getStyle(sd.getMetadata());
        if (elements == null) return "";
        String s = " ";
        if (elements.color != null)s += "#" + elements.color;
        if (elements.lineStyle != null) s += "##[" + elements.lineStyle + "]";
        s += " " ;
        return s;
    }

    public String getStyle(Transition t) {
        StyleElements elements = getStyle(t.getMetadata());
        if (elements == null) return "";
        return "[" +
                ((elements.thickness > 0)? "thickness=" + elements.thickness + "," : "")
                +((elements.color != null)?  "#" + elements.color:"")
                + "]";
    }

    public StyleElements getStyle(Map<String,String> md) {
        for (Map.Entry<Object, Object> prop : props.entrySet()) {
            String key = (String) prop.getKey();
            String[] arr = key.split("==");
            String value = (String) prop.getValue();
            if (arr[1].equals(md.get(arr[0]))) {
                try {
                    return objectMapper.readValue(value, StyleElements.class);
                }catch (Exception e){ return null;}
            }
        }
        return null;
    }
}
