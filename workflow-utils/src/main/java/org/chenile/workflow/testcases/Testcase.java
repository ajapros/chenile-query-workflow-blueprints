package org.chenile.workflow.testcases;

import org.chenile.stm.model.Transition;

import java.util.Objects;

/**
 * The basic test case. The test case consists of a set of events that will be emitted.
 * The event transitions the state entity. The from and to state IDs are captured.
 * The testcase mirrors the transition for the most part but can be apart from it as well.
 */
public class Testcase {
    public String event;
    public String from; // from state ID
    public String to; // from state ID
    public String getEvent(){ return event;}
    public Testcase(){}
    public Testcase(Transition t){
        this.event = t.getEventId();
        this.from = t.getStateId();
        this.to = t.getNewStateId();
    }
    public Testcase(String from,String to, String event){
        this.from = from; this.to = to; this.event = event;
    }
    public String toString(){
        return """
        {
            "event": %s,
            "from": %s,
            "to": %s
        }
        """.formatted(this.event,this.from,this.to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Testcase testcase = (Testcase) o;
        return Objects.equals(event, testcase.event) && Objects.equals(from, testcase.from) && Objects.equals(to, testcase.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, from, to);
    }
}
