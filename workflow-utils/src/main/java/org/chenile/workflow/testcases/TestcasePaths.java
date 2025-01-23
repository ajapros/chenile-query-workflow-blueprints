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

public class TestcasePaths {
    TestcasePaths(STMFlowStore flowStore){
        this.stmFlowStore = flowStore;
        this.activityChecker = new ActivityChecker(stmFlowStore);
    }
    public Map<State,List<Deque<Testcase>>> pathsMap = new HashMap<>();
    private final STMFlowStore stmFlowStore;
    private final ActivityChecker activityChecker;
    public  List<Deque<Testcase>> cachedComputePaths(State state, Set<State> visitedStates){
        // If it is cached then make sure that we return from cache.
        if (pathsMap.get(state) != null)
            return pathsMap.get(state);
        List<Deque<Testcase>> testcases = computePaths(state,visitedStates);
        pathsMap.put(state,testcases);
        return testcases;
    }

    private List<Deque<Testcase>> computePaths(State state, Set<State> visitedStates) {
        visitedStates.add(state);
        StateDescriptor sd = stmFlowStore.getStateInfo(state);
        List<Deque<Testcase>> testcaseList = new ArrayList<>();
        process(sd,state,visitedStates,testcaseList);
        return testcaseList;
    }

    private void noActivities(StateDescriptor sd,
                              State state, Set<State> visitedStates,
                              List<Deque<Testcase>> testcaseList){
        for (Transition transition : sd.getTransitions().values()) {
            State newState = new State(transition.getNewStateId(), transition.getNewFlowId());
            if (newState.equals(state)) continue; // ignore self transitions
            if (visitedStates.contains(newState)) continue; // ignore transitions that go back i.e.
            // that transit to already visited states.
            addToNext(transition,newState,visitedStates,testcaseList);
        }
    }

    /**
     * Obtain the next state for the given transition. Get all the testcases that are associated
     * with the next state and then add this transition to each of those.
     * Return the next state transitions with this one added. If no  transitions are found for the
     * next state then create a new test case with just this transition in it.
     * @param transition the transition for which the next state needs to be obtained
     * @param visitedStates - the states that have been visited already
     * @param testcaseList - an empty list to which the test cases have to be added.
     */
    private void addToNext(Transition transition,
                  State newState,
                  Set<State> visitedStates,
                  List<Deque<Testcase>> testcaseList){
        List<Deque<Testcase>> childTransitions = computePaths(newState, visitedStates);

        if (childTransitions.isEmpty()) {
            Deque<Testcase> stack = new ArrayDeque<>();
            stack.push(new Testcase(transition));
            testcaseList.add(stack);
        }else {
            for (Deque<Testcase> sT : childTransitions) {
                sT.push(new Testcase(transition));
                testcaseList.add(sT);
            }
        }
    }

    private void computeActivitiesPath(StateDescriptor sd,
                                    Set<State> visitedStates,
                                    List<Deque<Testcase>> testcaseList){
        for (Transition transition: sd.getTransitions().values()){
            if (activityChecker.isCompletionChecker(transition)){
                State newState = new State(transition.getNewStateId(), transition.getNewFlowId());
                if (newState.equals(new State(transition.getStateId(),transition.getFlowId()))){
                    System.err.println("Error: Loop detected. Completion checker " + transition.getEventId() + " is not leading to a new state ");
                    return;
                }
                addToNext(transition,newState,visitedStates,testcaseList);
            }
        }
        for (Transition transition: sd.getTransitions().values()){
            if (activityChecker.isMandatoryActivity(transition) ||
                    activityChecker.isOptionalActivity(transition)){
                for(Deque<Testcase> testcase :testcaseList){
                    testcase.push(new Testcase(transition));
                }
            }
        }
    }

    private void process(StateDescriptor sd, State state,Set<State> visitedStates,
                                     List<Deque<Testcase>> testcaseList){
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
                        computeActivitiesWithAutoComputationChecker(sd,visitedStates,testcaseList,asd);
                        return;
                    }
                }
            }
            if (activityChecker.isCompletionChecker(t)){
                computeActivitiesPath(sd,visitedStates,testcaseList);
                return;
            }
        }
        noActivities(sd,state,visitedStates,testcaseList);
    }

    /**
     * This method computes the path for states that have concurrent activities culminating in an
     * auto state. The auto state will lead to the new state if all the activities are completed.
     * @param sd the state descriptor for the start state.
     * @param visitedStates the states that have already been visited
     * @param testcaseList the test case list that we need to generate.
     * @param asd - the Automatic state descriptor for the activities. This is a completion checker that
     *            leads to the new state.
     */
    private void computeActivitiesWithAutoComputationChecker(StateDescriptor sd,
             Set<State> visitedStates, List<Deque<Testcase>> testcaseList,
             AutomaticStateDescriptor asd) {
        State targetState = null;
        // first find the transitions for the auto state.
        for (Transition t: asd.getTransitions().values()){
            if (t.getEventId().equals(AreActivitiesComplete.YES)){
                targetState = new State(t.getNewStateId(),t.getNewFlowId());
                break;
            }
        }
        if (targetState == null) return; // cannot continue processing this.
        List<Deque<Testcase>> childTestcases = computePaths(targetState,visitedStates);
        if (childTestcases.isEmpty()){
            testcaseList.add(new ArrayDeque<Testcase>()); // add an empty test case to it.
        }else {
            testcaseList.addAll(childTestcases);
        }
        // Add all the activities to the testcases that have been returned by the target state
        int index = 0;
        Collection<Transition> allTransitions = sd.getTransitions().values();

        for (Transition transition: allTransitions){
            index++;
            Testcase testcase1 = new Testcase(transition);
            // for the last event (which is the first one to be pushed) the entity would have transferred into the
            // next state. for all the others it will still be in the existing state.
            if (index == 1)
                testcase1.to = targetState.getStateId();
            else
                testcase1.to = sd.getId();
            if (activityChecker.isMandatoryActivity(transition) ||
                    activityChecker.isOptionalActivity(transition)){
                for(Deque<Testcase> testcase :testcaseList){
                    testcase.push(testcase1);
                }
            }
        }
    }
}
