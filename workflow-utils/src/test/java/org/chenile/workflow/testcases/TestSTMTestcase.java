package org.chenile.workflow.testcases;

import org.chenile.stm.dummy.DummyStore;
import org.chenile.stm.impl.XmlFlowReader;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestSTMTestcase {
    private static final String HAPPY_PATH_FILE = "org/chenile/workflow/testcases/happy-path.xml";
    private static final String ACTIVITIES_FILE = "org/chenile/workflow/testcases/activity-test.xml";
    private static final String ACTIVITIES_AUTO_STATE = "org/chenile/workflow/testcases/activity-with-auto-state.xml";
    private static final String MULTIPLE_AUTO_STATES = "org/chenile/workflow/testcases/multiple-auto-states.xml";

    public STMTestCaseGenerator setup(String filename) throws Exception{
        DummyStore dummyStore = new DummyStore();
        XmlFlowReader xmlFlowReader = new XmlFlowReader(dummyStore);
        xmlFlowReader.setFilename(filename);
        return new STMTestCaseGenerator(dummyStore);
    }
    @Test public void testHappyPath() throws Exception {
        STMTestCaseGenerator stmTestCaseGenerator = setup(HAPPY_PATH_FILE);
        List<Testcase> testcases = stmTestCaseGenerator.buildFlow();
        assertEquals(2, testcases.size());
        for (Testcase testcase: testcases){
            if (testcase.steps.size() == 3){
                assertOrder( new TestcaseStep[] {
                new TestcaseStep("INITIATED","APPROVED","approve"),
                new TestcaseStep("APPROVED","RECEIVED", "receive"),
                new TestcaseStep("RECEIVED","REFUNDED","refund")
                        }, testcase);
            }
            if (testcase.steps.size() == 1){
                assertOrder( new TestcaseStep[] {
                    new TestcaseStep("INITIATED","CANCELLED","cancel")}, testcase);
            }
        }
    }

    @Test public void testActivities() throws Exception {
        STMTestCaseGenerator stmTestCaseGenerator = setup(ACTIVITIES_FILE);
        List<Testcase> testcases = stmTestCaseGenerator.buildFlow();
        assertEquals(1, testcases.size());
        for (Testcase testcase: testcases){
            assertOrder(new TestcaseStep[] {
                    new TestcaseStep("DEV","DEV","build"),
                    new TestcaseStep("DEV","DEV","test"),
                    new TestcaseStep("DEV","PRODUCTION","deploy")}, testcase);
       }
    }

    private void assertOrder(TestcaseStep[] eventOrder, Testcase testcase) {
        assertArrayEquals(eventOrder, testcase.steps.toArray(TestcaseStep[]::new));
    }

    @Test public void testActivitiesWithAutoState() throws Exception {
        STMTestCaseGenerator stmTestCaseGenerator = setup(ACTIVITIES_AUTO_STATE);
        List<Testcase> testcaseList = stmTestCaseGenerator.buildFlow();
        assertEquals(2, testcaseList.size());
        for (Testcase testcase: testcaseList){
            assertEquals(3,testcase.steps.size());
            assert testcase.steps.peekLast() != null;
            if (testcase.steps.peekLast().to.equals("PRODUCTION")) {
                assertOrder(new TestcaseStep[]{
                        new TestcaseStep("DEV", "DEV", "build"),
                        new TestcaseStep("DEV", "QA", "test"),
                        new TestcaseStep("QA", "PRODUCTION", "pass")
                }, testcase);
            }else {
                assertOrder(new TestcaseStep[]{
                        new TestcaseStep("DEV", "DEV", "build"),
                        new TestcaseStep("DEV", "QA", "test"),
                        new TestcaseStep("QA", "DISCARDED", "fail")
                }, testcase);
            }
        }
    }

    @Test public void testMultipleAutoStates() throws Exception {
        STMTestCaseGenerator stmTestCaseGenerator = setup(MULTIPLE_AUTO_STATES);
        // System.out.println(stmTestCaseGenerator.visualizeTestcase());
        List<Testcase> testcases = stmTestCaseGenerator.buildFlow();
        assertEquals(9,testcases.size());
        // out of this 4 must start from REGULAR_APPLICANT and end in either REJECTED (if one or both
        // checks fail ) and SUCCESSFUL if both pass.
        // 4 others will start from FIRST_APPLICANT and then become a REGULAR_APPLICANT and follow the
        // same path.
        // 1 will fail straight from FIRST_APPLICANT
        assertEquals(5, testcases.stream().
                filter((testcase)-> testcase.steps.getFirst().from.equals("FIRST_APPLICANT")).
                toList().size());
        assertEquals(4, testcases.stream().
                filter((testcase)-> testcase.steps.getFirst().from.equals("REGULAR_APPLICANT")).
                toList().size());
        // find all testcases with second step starting from REGULAR_APPLICANT. There must be 4 of them
        assertEquals(4, testcases.stream().
                filter((testcase)-> {
                    if (testcase.steps.size() < 2) return false;
                    else return testcase.steps.stream().toList().get(1).from.equals("REGULAR_APPLICANT");
                }).
                toList().size());
        // there must be two testcases with last step culminating in SUCCESSFUL.
        assertEquals(2, testcases.stream().
                filter((testcase)-> {
                     return testcase.steps.getLast().to.equals("SUCCESSFUL");
                }).
                toList().size());
    }
}
