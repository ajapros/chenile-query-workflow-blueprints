package org.chenile.workflow.testcases;

import org.chenile.stm.dummy.DummyStore;
import org.chenile.stm.impl.XmlFlowReader;
import org.junit.Test;

import java.util.Deque;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestSTMTestcase {
    private static final String HAPPY_PATH_FILE = "org/chenile/workflow/testcases/happy-path.xml";
    private static final String ACTIVITIES_FILE = "org/chenile/workflow/testcases/activity-test.xml";
    private static final String ACTIVITIES_AUTO_STATE = "org/chenile/workflow/testcases/activity-with-auto-state.xml";
    public STMTestCaseGenerator setup(String filename) throws Exception{
        DummyStore dummyStore = new DummyStore();
        XmlFlowReader xmlFlowReader = new XmlFlowReader(dummyStore);
        xmlFlowReader.setFilename(filename);
        return new STMTestCaseGenerator(dummyStore);
    }
    @Test public void testHappyPath() throws Exception {
        STMTestCaseGenerator stmTestCaseGenerator = setup(HAPPY_PATH_FILE);
        List<Deque<Testcase>> testcases = stmTestCaseGenerator.buildFlow();
        assertEquals(2, testcases.size());
        for (Deque<Testcase> testcase: testcases){
            if (testcase.size() == 3){
                assertOrder( new Testcase[] {
                new Testcase("INITIATED","APPROVED","approve"),
                new Testcase("APPROVED","RECEIVED", "receive"),
                new Testcase("RECEIVED","REFUNDED","refund")
                        }, testcase);
            }
            if (testcase.size() == 1){
                assertOrder( new Testcase[] {
                    new Testcase("INITIATED","CANCELLED","cancel")}, testcase);
            }
        }
    }

    @Test public void testActivities() throws Exception {
        STMTestCaseGenerator stmTestCaseGenerator = setup(ACTIVITIES_FILE);
        List<Deque<Testcase>> testcases = stmTestCaseGenerator.buildFlow();
        assertEquals(1, testcases.size());
        for (Deque<Testcase> testcase: testcases){
            assertOrder(new Testcase[] {
                    new Testcase("DEV","DEV","build"),
                    new Testcase("DEV","DEV","test"),
                    new Testcase("DEV","PRODUCTION","deploy")}, testcase);
       }
    }

    private void assertOrder(Testcase[] eventOrder, Deque<Testcase> testcase) {
        assertArrayEquals(eventOrder, testcase.toArray(Testcase[]::new));
    }

    @Test public void testActivitiesWithAutoState() throws Exception {
        STMTestCaseGenerator stmTestCaseGenerator = setup(ACTIVITIES_AUTO_STATE);
        List<Deque<Testcase>> testcaseList = stmTestCaseGenerator.buildFlow();
        assertEquals(1, testcaseList.size());
        for (Deque<Testcase> testcases: testcaseList){
            assertEquals(2,testcases.size());
            assertOrder(new Testcase[] {
                    new Testcase("DEV","DEV","build"),
                    new Testcase("DEV","PRODUCTION","test")}, testcases);
        }
    }
}
