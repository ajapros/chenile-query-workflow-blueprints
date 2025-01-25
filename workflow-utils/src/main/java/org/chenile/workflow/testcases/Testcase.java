package org.chenile.workflow.testcases;

import java.util.ArrayDeque;
import java.util.Deque;

public class Testcase {
    public boolean first = false;
    public Deque<TestcaseStep> steps = new ArrayDeque<>();
    public String toString() { return "{ first = " + first + ", steps = " + steps.toString();}
}
