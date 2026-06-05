package org.chenile.workflow.puml;

import org.chenile.stm.STMFlowStore;
import org.chenile.stm.State;
import org.chenile.stm.model.AutomaticStateDescriptor;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a Mermaid state diagram for the State Transition Diagram.
 * This class mirrors the traversal behavior of {@link STMPlantUmlSDGenerator}
 * without changing the existing PlantUML generator.
 */
public class STMMermaidGenerator {
    private static final String MAIN_PATH = "mainPath";
    private static final String LABEL = "label";
    private static final String START = "[*]";

    private final STMFlowStore stmFlowStore;
    private final Map<String, Boolean> notOrphaned = new HashMap<>();
    public PumlStyler pumlStyler = new PumlStyler();

    public STMMermaidGenerator(STMFlowStore flowStore) {
        this.stmFlowStore = flowStore;
        findIncomingForAllStates();
    }

    public String toStateDiagram() {
        return new StateStringBuilder()
                .printHeader()
                .printStyles()
                .renderStates()
                .renderTransitions()
                .toString();
    }

    private class StateStringBuilder {
        private final StringBuilder stringBuilder = new StringBuilder();

        StateStringBuilder printHeader() {
            stringBuilder.append("stateDiagram-v2\n");
            return this;
        }

        StateStringBuilder printStyles() {
            stringBuilder.append("    classDef mainPath fill:Bisque,stroke:Peru,stroke-width:4px;\n");
            stringBuilder.append("    classDef orphaned fill:OrangeRed,color:white;\n");
            stringBuilder.append("    classDef autoState fill:PaleGreen,stroke:green,stroke-width:2.5px;\n");
            for (PumlStyler.StyleRule rule : pumlStyler.styleRules.rules) {
                if (rule.id == null || rule.style == null) {
                    continue;
                }
                List<String> css = new ArrayList<>();
                if (rule.style.color != null) {
                    css.add("fill:#" + rule.style.color);
                }
                if (rule.style.border != null) {
                    css.add("stroke:" + rule.style.border);
                }
                if (rule.style.thickness > 0) {
                    css.add("stroke-width:" + rule.style.thickness + "px");
                }
                if (rule.style.stateTextColor != null) {
                    css.add("color:" + rule.style.stateTextColor);
                }
                if (!css.isEmpty()) {
                    stringBuilder.append("    classDef ").append(rule.id)
                            .append(" ").append(String.join(",", css)).append(";\n");
                }
            }
            return this;
        }

        StateStringBuilder renderStates() {
            for (StateDescriptor sd : stmFlowStore.getAllStates()) {
                stringBuilder.append("    ");
                if (!sd.isManualState()) {
                    stringBuilder.append("state ").append(sd.getId()).append(" <<choice>>\n");
                    appendNote(sd);
                } else {
                    String label = getDisplayText(sd.getId(), sd.getMetadata());
                    if (label.equals(sd.getId())) {
                        stringBuilder.append("state ").append(sd.getId()).append("\n");
                    } else {
                        stringBuilder.append("state \"").append(escape(label))
                                .append("\" as ").append(sd.getId()).append("\n");
                    }
                }
                appendClasses(sd);
            }
            return this;
        }

        StateStringBuilder renderTransitions() {
            for (StateDescriptor sd : stmFlowStore.getAllStates()) {
                Map<String, Transition> transitions = sd.getTransitions();
                if (sd.isInitialState()) {
                    stringBuilder.append("    ").append(START).append(" --> ").append(sd.getId()).append("\n");
                }
                if (transitions.isEmpty()) {
                    stringBuilder.append("    ").append(sd.getId()).append(" --> ").append(START).append("\n");
                }
                for (Transition t : transitions.values()) {
                    stringBuilder.append("    ").append(sd.getId())
                            .append(" --> ").append(t.getNewStateId())
                            .append(": ").append(escape(getDisplayText(t.getEventId(), t.getMetadata())))
                            .append("\n");
                }
            }
            return this;
        }

        private void appendClasses(StateDescriptor sd) {
            List<String> classes = new ArrayList<>();
            if (!sd.isManualState()) {
                classes.add("autoState");
            }
            Boolean connected = notOrphaned.get(sd.getId());
            if (connected != null && !connected) {
                classes.add("orphaned");
            }
            if (isInMainPath(sd)) {
                classes.add("mainPath");
            }
            PumlStyler.StyleRule rule = findMatchingStyle(sd.getMetadata());
            if (rule != null && rule.id != null) {
                classes.add(rule.id);
            }
            for (String cssClass : classes) {
                stringBuilder.append("    class ").append(sd.getId()).append(" ").append(cssClass).append("\n");
            }
        }

        private void appendNote(StateDescriptor sd) {
            List<String> lines = new ArrayList<>();
            lines.add(sd.getId());
            Map<String, String> metadata = sd.getMetadata();
            if (metadata != null && !metadata.isEmpty()) {
                for (Map.Entry<String, String> md : metadata.entrySet()) {
                    lines.add(md.getKey() + ": " + md.getValue());
                }
            }
            if (sd instanceof AutomaticStateDescriptor asd) {
                Map<String, Object> props = asd.getComponentProperties();
                for (Map.Entry<String, Object> prop : props.entrySet()) {
                    lines.add(prop.getKey() + ": " + String.valueOf(prop.getValue()));
                }
            }
            stringBuilder.append("    note right of ").append(sd.getId()).append("\n");
            for (String line : lines) {
                stringBuilder.append("        ").append(escape(line)).append("\n");
            }
            stringBuilder.append("    end note\n");
        }

        @Override
        public String toString() {
            return stringBuilder.toString();
        }
    }

    private void findIncomingForAllStates() {
        for (StateDescriptor sd : stmFlowStore.getAllStates()) {
            notOrphaned.put(sd.getId(), sd.isInitialState());
        }
        for (StateDescriptor sd : stmFlowStore.getAllStates()) {
            for (Transition t : sd.getTransitions().values()) {
                if (!t.getNewStateId().equals(sd.getId())) {
                    notOrphaned.put(t.getNewStateId(), true);
                }
            }
        }
    }

    private boolean isInMainPath(StateDescriptor sd) {
        if (sd == null) {
            return false;
        }
        return Boolean.parseBoolean(metadataValue(sd.getMetadata(), MAIN_PATH));
    }

    private String getDisplayText(String text, Map<String, String> metadata) {
        String label = metadataValue(metadata, LABEL);
        return (label == null || label.isEmpty()) ? text : label;
    }

    private String metadataValue(Map<String, String> metadata, String key) {
        return metadata == null ? null : metadata.get(key);
    }

    private PumlStyler.StyleRule findMatchingStyle(Map<String, String> metadata) {
        if (metadata == null) {
            return null;
        }
        for (PumlStyler.StyleRule rule : pumlStyler.styleRules.rules) {
            if (rule.expression == null || !rule.expression.contains(PumlStyler.EQUALS)) {
                continue;
            }
            String[] arr = rule.expression.split(PumlStyler.EQUALS, 2);
            if (arr[1].equals(metadata.get(arr[0]))) {
                return rule;
            }
        }
        return null;
    }

    private String escape(String value) {
        return value.replace("\"", "\\\"");
    }
}
