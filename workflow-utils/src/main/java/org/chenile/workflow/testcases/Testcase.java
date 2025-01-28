package org.chenile.workflow.testcases;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Testcase {
    public boolean first = false;
    public int id;
    public Deque<TestcaseStep> steps = new ArrayDeque<>();
    /**
     * These steps include auto states as well
     */
    public Deque<TestcaseStep> allSteps = new ArrayDeque<>();
    public List<String> comments = new ArrayList<>();
    public String toString() {
        return """
                "id" : "%s",
                "first" : "%s",
                "comment" : "%s",
                "steps" :  %s
                """.formatted(id,first,comments,steps.toString());
    }
}
