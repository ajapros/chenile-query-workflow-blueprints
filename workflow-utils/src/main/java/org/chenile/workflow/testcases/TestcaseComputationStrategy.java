package org.chenile.workflow.testcases;

import org.chenile.stm.STMFlowStore;
import org.chenile.stm.State;
import org.chenile.stm.model.AutomaticStateDescriptor;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.service.activities.ActivityChecker;
import org.chenile.workflow.service.activities.AreActivitiesComplete;

import java.util.*;

/**
 * Construct a flow without self transitions that goes forward till the last state
 * has no transitions or alternatively has transitions that only go backwards to
 * already visited states.
 * <p>This class returns multiple test cases each with their own flow. We can use that to construct
 * multiple test scenarios.
 */

public class TestcaseComputationStrategy {
    TestcaseComputationStrategy(STMFlowStore flowStore){
        this.stmFlowStore = flowStore;
        this.activityChecker = new ActivityChecker(stmFlowStore);
    }
    private final STMFlowStore stmFlowStore;
    private final ActivityChecker activityChecker;

    public List<Testcase> toTestcases(State state){
        Set<State> visitedStates = new HashSet<>();
        List<Testcase> testcases = cachedComputePaths(state,visitedStates);
        testcases = dropAutoStates(testcases);
        if (!testcases.isEmpty())
            testcases.get(0).first = true;
        for(Testcase testcase:testcases){
            if (!testcase.steps.isEmpty())
                testcase.steps.getFirst().first = true;
        }
        return testcases;
    }

    private  List<Testcase> cachedComputePaths(State state,Set<State> visitedStates){
        // Create a new visited states with a copy of the existing ones. Don't pass the
        // existing ones. We don't want the existing tree to be affected by the visited states
        // of this branch of the tree.
        visitedStates = new HashSet<>(visitedStates);
        visitedStates.add(state);
        return computePaths(state,visitedStates);
    }

    /**
     * If the test case happens to have auto states they need to be simply dropped from the
     * test case.
     * @param testcases - List of test cases to update
     * @return the altered test cases with the auto states dropped
     */
    private List<Testcase> dropAutoStates(List<Testcase> testcases){
        List<Testcase> retTestcases = new ArrayList<>();
        for(Testcase testcase: testcases){
            Testcase testcase1 = new Testcase();
            TestcaseStep prevStep = null;
            for(TestcaseStep testcaseStep: testcase.steps){
                State state = new State(testcaseStep.from,testcaseStep.fromFlow);
                StateDescriptor sd = stmFlowStore.getStateInfo(state);
                // if this is an auto state make sure that the prev step points to
                // the destination of the transition and not to the origin.
                // this will merely mutate the prev step without recording it.
                if (!sd.isManualState()){
                    if (prevStep != null){
                        prevStep.to = testcaseStep.to;
                        prevStep.toFlow = testcaseStep.toFlow;
                    }
                }else {
                    // for manual steps record the previous step if it exists
                    // and update the prev step to the current step
                    if(prevStep != null)
                        testcase1.steps.add(prevStep);
                    prevStep = testcaseStep;
                }
            }
            if(prevStep != null)
                testcase1.steps.add(prevStep);
            retTestcases.add(testcase1);
        }
        return retTestcases;
    }

    private List<Testcase> computePaths(State state,Set<State> visitedStates) {
        StateDescriptor sd = stmFlowStore.getStateInfo(state);
        List<Testcase> testcases = new ArrayList<>();
        process(sd,state,testcases,visitedStates);
        return testcases;
    }

    private void noActivities(StateDescriptor sd,
                              State state,
                              List<Testcase> testcaseList, Set<State> visitedStates){
        for (Transition transition : sd.getTransitions().values()) {
            State newState = new State(transition.getNewStateId(), transition.getNewFlowId());
            if (newState.equals(state)) continue; // ignore self transitions
            if (visitedStates.contains(newState)) return;
            addToNext(transition,newState,testcaseList, visitedStates);
        }
    }
    /**
     * Obtain the next state for the given transition. Get all the testcases that are associated
     * with the next state and then add this transition to each of those.
     * Return the next state transitions with this one added. If no  transitions are found for the
     * next state then create a new test case with just this transition in it.
     * @param transition the transition for which the next state needs to be obtained
     * @param testcaseList - an empty list to which the test cases have to be added.
     */
    private void addToNext(Transition transition,
                  State newState,
                  List<Testcase> testcaseList,
                  Set<State> visitedStates){
        List<Testcase> childTransitions = cachedComputePaths(newState, visitedStates);

        if (childTransitions.isEmpty()) {
            Testcase testcase = new Testcase();
            testcase.steps.push(new TestcaseStep(transition));
            testcaseList.add(testcase);
        }else {
            for (Testcase testcase : childTransitions) {
                testcase.steps.push(new TestcaseStep(transition));
                testcaseList.add(testcase);
            }
        }
    }

    private void computeActivitiesPath(StateDescriptor sd,
                                    List<Testcase> testcaseList, Set<State> visitedStates){
        for (Transition transition: sd.getTransitions().values()){
            if (activityChecker.isCompletionChecker(transition)){
                State newState = new State(transition.getNewStateId(), transition.getNewFlowId());
                if (newState.equals(new State(transition.getStateId(),transition.getFlowId()))){
                    System.err.println("Error: Loop detected. Completion checker " + transition.getEventId() + " is not leading to a new state ");
                    return;
                }
                addToNext(transition,newState,testcaseList,visitedStates);
            }
        }
        for (Transition transition: sd.getTransitions().values()){
            if (activityChecker.isMandatoryActivity(transition) ||
                    activityChecker.isOptionalActivity(transition)){
                for(Testcase testcase :testcaseList){
                    testcase.steps.push(new TestcaseStep(transition));
                }
            }
        }
    }

    private void process(StateDescriptor sd, State state,
                                     List<Testcase> testcaseList, Set<State> visitedStates){
        for(Transition t: sd.getTransitions().values()){
            if (activityChecker.isActivity(t)){
                // if this activity leads to an auto state then use that auto state as the basis
                // for getting testcases. If it is the same state then continue looking for
                // a completion checker event.
                State endState = new State(t.getNewStateId(),t.getNewFlowId());
                if (endState.equals(state))continue;
                StateDescriptor esd = stmFlowStore.getStateInfo(endState);
                if (!esd.isManualState()){
                    AutomaticStateDescriptor asd = (AutomaticStateDescriptor) esd;
                    String whichState = (String)asd.getComponentProperties().get("whichStateId");
                    if (asd.getComponent() != null && whichState != null && whichState.equals(sd.getId())){
                        computeActivitiesWithAutoComputationChecker(sd,testcaseList,asd, visitedStates);
                        return;
                    }
                }
            }
            if (activityChecker.isCompletionChecker(t)){
                computeActivitiesPath(sd,testcaseList, visitedStates);
                return;
            }
        }
        noActivities(sd,state,testcaseList, visitedStates);
    }

    /**
     * This method computes the path for states that have concurrent activities culminating in an
     * auto state. The auto state will lead to the new state if all the activities are completed.
     * @param sd the state descriptor for the start state.
     * @param testcaseList the test case list that we need to generate.
     * @param asd - the Automatic state descriptor for the activities. This is a completion checker that
     *            leads to the new state.
     */
    private void computeActivitiesWithAutoComputationChecker(StateDescriptor sd,
              List<Testcase> testcaseList,
             AutomaticStateDescriptor asd, Set<State> visitedStates) {
        State targetState = null;
        // first find the transitions for the auto state.
        for (Transition t: asd.getTransitions().values()){
            if (t.getEventId().equals(AreActivitiesComplete.YES)){
                targetState = new State(t.getNewStateId(),t.getNewFlowId());
                break;
            }
        }
        if (targetState == null) return; // cannot continue processing this.
        List<Testcase> childTestcases = cachedComputePaths(targetState,visitedStates);
        if (childTestcases.isEmpty()){
            testcaseList.add(new Testcase()); // add an empty test case to it.
        }else {
            testcaseList.addAll(childTestcases);
        }
        // Add all the activities to the testcases that have been returned by the target state
        int index = 0;
        Collection<Transition> allTransitions = sd.getTransitions().values();

        for (Transition transition: allTransitions){
            index++;
            TestcaseStep testcase1 = new TestcaseStep(transition);
            // for the last event (which is the first one to be pushed) the entity would have transferred into the
            // next state. for all the others it will still be in the existing state.
            if (index == 1)
                testcase1.to = targetState.getStateId();
            else
                testcase1.to = sd.getId();
            if (activityChecker.isMandatoryActivity(transition) ||
                    activityChecker.isOptionalActivity(transition)){
                for(Testcase testcase :testcaseList){
                    testcase.steps.push(testcase1);
                }
            }
        }
    }
}
