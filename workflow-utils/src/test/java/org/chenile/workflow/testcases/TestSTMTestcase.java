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
    private static final String LEAD = "org/chenile/workflow/testcases/leads.xml";

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
        assertEquals(1, testcaseList.size());
        for (Testcase testcase: testcaseList){
            assertEquals(2,testcase.steps.size());
            assertOrder(new TestcaseStep[] {
                    new TestcaseStep("DEV","DEV","build"),
                    new TestcaseStep("DEV","PRODUCTION","test")}, testcase);
        }
    }

    @Test public void testMultiple() throws Exception {
        STMTestCaseGenerator stmTestCaseGenerator = setup(LEAD);
        List<Testcase> testcases = stmTestCaseGenerator.buildFlow();
        // System.out.println("#testcases = " + testcases.size());
       //  List<Testcase> list = testcases.stream().filter((testcase) -> !testcase.first).toList();
       //  System.out.println("#testcases after first = true filter = " + list.size());
        // for (Testcase testcase: testcases){
//
        //}
    }
}
