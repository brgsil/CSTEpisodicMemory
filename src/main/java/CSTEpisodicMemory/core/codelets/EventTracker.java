package CSTEpisodicMemory.core.codelets;

import CSTEpisodicMemory.experiments.ExperimentB;
import CSTEpisodicMemory.util.IdeaHelper;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static CSTEpisodicMemory.experiments.ExperimentB.*;
import static CSTEpisodicMemory.util.IdeaHelper.csvPrint;

public class EventTracker extends MemoryCodelet {

    private String inputBufferMemoryName = "PERCEPTUAL_BUFFER";
    private String outputMemoryName = "EVENTS_MEMORY";
    private Memory bufferInputMO;
    private Memory eventsOutputMO;
    private Memory contextSegmentationMO;
    private long latestBoundary;
    private Idea eventsIdea;

    private Map<String, List<Idea>> internal;
    private int bufferSize = 1;
    private int bufferStepSize = 1;
    private Idea buffer;
    private static int count = 1;
    private double detectionTreashold = 0.5;
    private Idea trackedEventCategory;
    private boolean debug = false;

    public EventTracker(Mind m, String inputBufferMemoryName, String outputMemoryName, Idea trackedEventCategory) {
        super(m, trackedEventCategory.getName());
        setInternalI(new HashMap<String, Idea>());
        this.inputBufferMemoryName = inputBufferMemoryName;
        this.outputMemoryName = outputMemoryName;
        if (trackedEventCategory.getValue() instanceof Category)
            this.trackedEventCategory = trackedEventCategory;
        this.name = "Tracker" + trackedEventCategory.getName();
    }

    public EventTracker(Mind m, String inputBufferMemoryName, String outputMemoryName, Idea trackedEventCategory, boolean debug) {
        super(m, trackedEventCategory.getName());
        setInternalI(new HashMap<String, Idea>());
        this.inputBufferMemoryName = inputBufferMemoryName;
        this.outputMemoryName = outputMemoryName;
        if (trackedEventCategory.getValue() instanceof Category)
            this.trackedEventCategory = trackedEventCategory;
        this.debug = debug;
        this.name = "Tracker" + trackedEventCategory.getName();
    }

    public EventTracker(Mind m, String inputBufferMemoryName, String outputMemoryName, double detectionTreashold, Idea trackedEventCategory) {
        super(m, trackedEventCategory.getName());
        setInternalI(new HashMap<String, Idea>());
        this.inputBufferMemoryName = inputBufferMemoryName;
        this.outputMemoryName = outputMemoryName;
        this.detectionTreashold = detectionTreashold;
        if (trackedEventCategory.getValue() instanceof Category)
            this.trackedEventCategory = trackedEventCategory;
        this.name = "Tracker" + trackedEventCategory.getName();
    }

    public EventTracker(Mind m, String inputBufferMemoryName, String outputMemoryName, double detectionTreashold, Idea trackedEventCategory, boolean debug) {
        super(m, trackedEventCategory.getName());
        setInternalI(new HashMap<String, Idea>());
        this.inputBufferMemoryName = inputBufferMemoryName;
        this.outputMemoryName = outputMemoryName;
        this.detectionTreashold = detectionTreashold;
        if (trackedEventCategory.getValue() instanceof Category)
            this.trackedEventCategory = trackedEventCategory;
        this.debug = debug;
        this.name = "Tracker" + trackedEventCategory.getName();
    }

    @Override
    public void accessMemoryObjects() {
        this.bufferInputMO = (MemoryObject) this.getInput(this.getInputMemoryName());
        this.buffer = (Idea) bufferInputMO.getI();
        this.eventsOutputMO = (MemoryObject) this.getOutput(this.getOutputMemoryName());
        this.contextSegmentationMO = (MemoryObject) this.getInput("BOUNDARIES");
        this.latestBoundary = getLatestBoundaryTime();
        this.internal = (Map<String, List<Idea>>) getInternalMemoryI();
        this.eventsIdea = (Idea) eventsOutputMO.getI();
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        ArrayList<String> ignoreObjects = new ArrayList<>();
        synchronized (buffer) {
            synchronized (eventsOutputMO) {
                for (Idea timeStep : buffer.getL()) {
                    for (Idea object : timeStep.getL()) {
                        if (object.getType() == 5) {
                            for (Idea subObj : object.getL()) {
                                processObjectTimeStep(timeStep, subObj, ignoreObjects);
                            }
                        } else {
                            processObjectTimeStep(timeStep, object, ignoreObjects);
                        }
                    }
                }
                finishedUnupdatedEvents();
                commitInternalMemoryChanges();
            }
        }
    }

    private void finishedUnupdatedEvents() {
        for (String objectName : internal.keySet()) {
            List<Idea> eventBuffer = constructCurrentBufferOf(objectName);
            //if (eventBuffer.size() == bufferSize) {
            long lastUpdate = getLastTimeStampOf(objectName);
            Idea lastBufferTime = buffer.getL().get(buffer.getL().size() - 1);
            if ((long) lastBufferTime.getValue() - lastUpdate > 500 || isForcedSegmentation(objectName)) {
                if (getInitialEventOf(objectName) != null)
                    insertEventInOutputMemory(lastBufferTime, eventBuffer.get(eventBuffer.size() - 1), getInitialEventOf(objectName), eventBuffer);
            }
            //}
        }
    }

    private void processObjectTimeStep(Idea timeStep, Idea object, ArrayList<String> ignoreObjects) {
        String objectName = object.getName();
        if (isTrackedObject(objectName)) {
            long lastTrackedTimeStamp = getLastTimeStampOf(objectName);
            if ((long) timeStep.getValue() > lastTrackedTimeStamp) {
                List<Idea> inputIdeaBuffer = constructCurrentBufferOf(objectName);

                if (inputIdeaBuffer.isEmpty()) {
                    pushStepToMemory(object, (long) timeStep.getValue());
                } else {
                    if (inputIdeaBuffer.size() < this.bufferSize) {
                        if (checkElapsedTime(timeStep, lastTrackedTimeStamp)) {
                            pushStepToMemory(object, (long) timeStep.getValue());
                        }
                    } else {
                        if (checkElapsedTime(timeStep, lastTrackedTimeStamp)) {
                            //Check if current state is coherent with previous states and event category
                            Idea initialEventIdea = getInitialEventOf(objectName);
                            Idea testEvent = constructTestEvent(inputIdeaBuffer, object);
                            //System.out.println(name +": "+ objectName + " - " + trackedEventCategory.membership(testEvent) + " - " + isForcedSegmentation(objectName) + " - " + (initialEventIdea==null));
                            if (trackedEventCategory.membership(testEvent) > detectionTreashold && !isForcedSegmentation(objectName)) {
                                //Copies start of the event
                                if (initialEventIdea == null) {
                                    setBufferTopAsInitialEvent(objectName);
                                    //System.out.println("top----------------------");
                                }

                                pushStepToMemory(object, (long) timeStep.getValue());
                            } else {
                                if (initialEventIdea != null) {
                                    insertEventInOutputMemory(timeStep, object, initialEventIdea, inputIdeaBuffer);
                                } else {
                                    pushStepToMemory(object, (long) timeStep.getValue());
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (!ignoreObjects.contains(objectName)) {
                Idea testEvent = constructTestEvent(new ArrayList<>(), object);
                if (trackedEventCategory.membership(testEvent) >= detectionTreashold) {
                    pushStepToMemory(object, (long) timeStep.getValue());
                } else {
                    ignoreObjects.add(objectName);
                }
            }
        }
    }

    private void insertEventInOutputMemory(Idea timeStep, Idea object, Idea initialEventIdea, List<Idea> inputIdeaBuffer) {
        Idea constraints = new Idea("Constraints");
        constraints.add(new Idea("0", initialEventIdea));
        constraints.add(new Idea("1", inputIdeaBuffer.get(inputIdeaBuffer.size() - 1)));
        Idea event = trackedEventCategory.getInstance(constraints);
        event.setName("Event" + count++);
        event.setValue(trackedEventCategory);
        restartEventStage(object, (long) timeStep.getValue());
        synchronized (eventsIdea) {
            eventsIdea.add(event);
            if (debug)
                System.out.println(csvPrint(eventsIdea));
        }
    }

    private void commitInternalMemoryChanges() {
        setInternalI(internal);
        eventsOutputMO.setI(eventsIdea);
    }

    private void restartEventStage(Idea object, long timeStamp) {
        Idea step = new Idea("", timeStamp, "TimeStep", 1);
        Idea stepObj = IdeaHelper.cloneIdea(object);
        if (stepObj.get("TimeStamp") == null) {
            stepObj.add(new Idea("TimeStamp", timeStamp, "Property", 1));
        }
        step.add(stepObj);
        String objName = object.getName();
        internal.put(objName, new LinkedList<Idea>() {{
            add(null);
            add(step);
        }});
    }

    private void setBufferTopAsInitialEvent(String objectName) {
        List<Idea> stageEventSteps = internal.get(objectName);
        stageEventSteps.set(0, IdeaHelper.cloneIdea(stageEventSteps.get(1)));
    }

    private void pushStepToMemory(Idea object, long timeStamp) {
        Idea step = new Idea("", timeStamp, "TimeStep", 1);
        Idea stepObj = IdeaHelper.cloneIdea(object);
        if (stepObj.get("TimeStamp") == null) {
            stepObj.add(new Idea("TimeStamp", timeStamp, "Property", 1));
        }
        step.add(stepObj);
        String objName = object.getName();
        if (internal.containsKey(objName)) {
            List<Idea> stageEventSteps = internal.get(objName);
            if (timeStamp - (long) stageEventSteps.get(1).getL().get(0).get("TimeStamp").getValue() < 500) {
                stageEventSteps.add(step);
                if (stageEventSteps.size() > bufferSize + 1) {
                    stageEventSteps.remove(1);
                }
                return;
            }
        }
        internal.put(objName, new LinkedList<Idea>() {{
            add(null);
            add(step);
        }});
    }

    private boolean isTrackedObject(String objectName) {
        return internal.containsKey(objectName);
    }

    private long getLastTimeStampOf(String objectName) {
        List<Idea> stageEventSteps = internal.get(objectName);
        if (!stageEventSteps.isEmpty()) {
            return (long) stageEventSteps.get(stageEventSteps.size() - 1).getValue();
        }
        return 0;
    }

    private List<Idea> constructCurrentBufferOf(String objectName) {
        List<Idea> stageEventSteps = internal.get(objectName);
        if (stageEventSteps.size() > 1) {
            return stageEventSteps.subList(1, stageEventSteps.size())
                    .stream()
                    .map(s -> s.getL().get(0))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private Idea getInitialEventOf(String objectName) {
        List<Idea> stageEventSteps = internal.get(objectName);
        if (!stageEventSteps.isEmpty() && stageEventSteps.get(0) != null) {
            return stageEventSteps.get(0).getL().get(0);
        }
        return null;
    }

    private long getLatestBoundaryTime() {
        synchronized (contextSegmentationMO) {
            Idea boundaries = (Idea) contextSegmentationMO.getI();
            return boundaries.getL()
                    .stream()
                    .mapToLong(i -> (long) i.get("TimeStamp").getValue())
                    .max().orElse(0);
        }

    }

    private boolean isForcedSegmentation(String objectName) {
        if (internal.get(objectName).get(0) != null) {
            long initialTimeStamp = (long) internal.get(objectName).get(0).getValue();
            return initialTimeStamp < latestBoundary;
        }
        return false;
    }

    private Idea constructTestEvent(List<Idea> inputIdeaBuffer, Idea object) {
        Idea testEvent = new Idea("Event", null, "Episode", 0);
        List<Idea> steps = new ArrayList<>();
        for (int i = 0; i < inputIdeaBuffer.size(); i++) {
            Idea step = new Idea("Step_" + i, i, "Timestep", 0);
            step.add(inputIdeaBuffer.get(i));
            steps.add(step);
        }
        Idea step = new Idea("Step_" + inputIdeaBuffer.size(), inputIdeaBuffer.size(), "Timestep", 0);
        step.add(object);
        steps.add(step);
        testEvent.setL(steps);
        return testEvent;
    }

    private boolean checkElapsedTime(Idea timeStep, long lastTimeStamp) {
        return (long) timeStep.getValue() - lastTimeStamp >= bufferStepSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize > 0)
            this.bufferSize = bufferSize;
    }

    public int getBufferStepSize() {
        return bufferStepSize;
    }

    public void setBufferStepSizeInMillis(int bufferStepSize) {
        if (bufferStepSize > 0)
            this.bufferStepSize = bufferStepSize;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getInputMemoryName() {
        return inputBufferMemoryName;
    }

    public void setInputMemoryName(String inputMemoryName) {
        this.inputBufferMemoryName = inputMemoryName;
    }

    public String getOutputMemoryName() {
        return outputMemoryName;
    }

    public void setOutputMemoryName(String outputMemoryName) {
        this.outputMemoryName = outputMemoryName;
    }
}
