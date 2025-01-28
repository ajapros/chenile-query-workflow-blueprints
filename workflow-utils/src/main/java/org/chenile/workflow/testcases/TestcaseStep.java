package org.chenile.workflow.testcases;

import org.chenile.stm.model.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The basic test case. The test case consists of a set of events that will be emitted.
 * The event transitions the state entity. The from and to state IDs are captured.<br/>
 * The testcase object is derived from a transition and looks like it for the most part with additional information.
 */
public class TestcaseStep {
    public boolean first = false;
    public String event;
    public String fromFlow;
    public String from; // from state ID
    public String to;// from state ID
    public String toFlow;
    public boolean manual = false;
    public List<String> comments = new ArrayList<>();
    public Transition transition;

    public TestcaseStep(Transition t){
        this.event = t.getEventId();
        this.fromFlow = t.getFlowId();
        this.from = t.getStateId();
        this.to = t.getNewStateId();
        this.toFlow = t.getNewFlowId();
        this.transition = t;
    }

    /**
     * This method is used in test cases for assertions.
     * @param from - from State ID
     * @param to - to State ID
     * @param event - Event
     */
    public TestcaseStep(String from, String to, String event){
        this.from = from; this.to = to; this.event = event;
    }
    public String toString(){
        return """
        {
            "first": %s,
            "event": "%s",
            "from": "%s",
            "fromFlow": "%s",
            "toFlow": "%s",
            %s
            "isManual": %s,
            "to": "%s"
        }
        """.formatted(this.first,this.event,this.from,this.fromFlow,this.toFlow,
                STMTestCaseGenerator.printComments(comments),
                this.manual,this.to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestcaseStep testcase = (TestcaseStep) o;
        return Objects.equals(event, testcase.event) && Objects.equals(from, testcase.from) && Objects.equals(to, testcase.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, from, to);
    }
}
