/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package CSTEpisodicMemory;

import CSTEpisodicMemory.core.representation.GraphIdea;
import CSTEpisodicMemory.util.visualization.*;
import WS3DCoppelia.util.Constants;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.representation.idea.Idea;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExperimentMain {

    static AgentMind a;
    public static void main(String[] args) {
        //test();
        normal();
    }

    private static void test(){
        GraphIdea gg = new GraphIdea(new Idea("Graph", null));
        Idea e1 = new Idea("Event1", null);
        Idea e2 = new Idea("Event2", null);
        Idea e3 = new Idea("Event3", null);
        Idea l1 = new Idea("Location1", null);
        Idea l2 = new Idea("Location2", null);
        Idea l3 = new Idea("Location3", null);
        Idea l4 = new Idea("Location4", null);
        gg.insertEventNode(e1);
        gg.insertEventNode(e2);
        gg.insertEventNode(e3);
        gg.insertLocationNode(l1);
        gg.insertLocationNode(l2);
        gg.insertLocationNode(l3);
        gg.insertLocationNode(l4);
        gg.insertLink(e1,l1,"loc");
        gg.insertLink(e2,l2,"loc");
        gg.insertLink(e3,l2,"loc");
        gg.insertLink(e1,e2,"loc");
        gg.insertLink(e1,e3,"loc");
        gg.insertLink(e2,e3,"loc");
        GraphIdeaVisualizer tt = new GraphIdeaVisualizer(800, 800, gg);
    }
     private static void normal(){
         Logger.getLogger("codelets").setLevel(Level.SEVERE);
         // Create Environment
         Environment env=new Environment(); //Creates only a creature and some apples
         a = new AgentMind(env);  // Creates the Agent Mind and start it
         // The following lines create the MindViewer and configure it
         //MindViewer mv = new MindViewer(a,"MindViewer", a.bList);
         //mv.setVisible(true);

         IdeaVisualizer visu = new IdeaVisualizer(a);
         visu.addMemoryWatch("Story", 6);
         visu.addMemoryWatch("Impulses", 5);
         visu.addMemoryWatch("EPLTM", 4);
         visu.addMemoryWatch("Location", 3);
         visu.addMemoryWatch("PROPERTIES", 3);
         visu.addMemoryWatch("FOOD", 3);
         visu.addMemoryWatch("Events", 5);
         visu.setVisible(true);

         GraphicMind lv = new GraphicMind(a, env, 10,8,10*80,8*80);


         EventVisualizer ev = new EventVisualizer(1000, 200, a);

         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 env.stopSimulation();
             }
         });
         runTestCommands(env, a);

     }

    public static void runTestCommands(Environment env, AgentMind a){
        env.world.createThing(Constants.FoodTypes.NPFOOD, 7.5f, 1.5f);
        env.world.createThing(Constants.FoodTypes.NPFOOD, 7.5f, 9.5f);
        env.world.createThing(Constants.JewelTypes.RED_JEWEL, 0.2f, 9.5f);
        env.world.createThing(Constants.JewelTypes.BLUE_JEWEL, 0.4f, 9.5f);
        env.world.createThing(Constants.JewelTypes.GREEN_JEWEL, 0.6f, 9.5f);
        env.world.createThing(Constants.JewelTypes.WHITE_JEWEL, 0.8f, 9.5f);
        env.world.createThing(Constants.JewelTypes.MAGENTA_JEWEL, 1.0f, 9.5f);
        env.world.createThing(Constants.JewelTypes.YELLOW_JEWEL, 1.2f, 9.5f);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ExperimentMain.class.getName()).log(Level.SEVERE, null, ex);
        }
//        env.creature.moveTo(2f,2f);
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(ExperimentMain.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        env.creature.moveTo(3f,1f);
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(ExperimentMain.class.getName()).log(Level.SEVERE, null, ex);
//        }
        //-------Explore Impulse (TEST ONLY) -----
        Idea impulse = new Idea("Impulse", "Explore", "Episode", 0);
        Idea state = new Idea("State", null, "Timestep", 0);
        Idea dest = new Idea("Self", null, "AbstractObject", 1);
        Idea posIdea = new Idea("Position", null, "Property", 1);
        posIdea.add(new Idea("X",0.5f, 3));
        posIdea.add(new Idea("Y",2f, 3));
        dest.add(posIdea);
        state.add(dest);
        state.add(new Idea("Desire", 0.1, "Property", 1));
        impulse.add(state);
        //---------
        List<Memory> mems = a.getRawMemory().getAllMemoryObjects();
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("IMPULSES")) {
                MemoryContainer moc = (MemoryContainer) mo;
                moc.setI(impulse, 0.1, "Explore");
                //System.out.println(fullPrint((Idea) mo.getI()));
            }
        }

        //env.creature.moveTo(0.5f,2f);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ExperimentMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        CategoriesPerEventView cc = new CategoriesPerEventView(a);

        Idea impulse_ = new Idea("Impulse", "Explore", "Episode", 0);
        Idea state_ = new Idea("State", null, "Timestep", 0);
        Idea dest_ = new Idea("Self", null, "AbstractObject", 1);
        Idea posIdea_ = new Idea("Position", null, "Property", 1);
        posIdea_.add(new Idea("X",0.5f, 3));
        posIdea_.add(new Idea("Y",9f, 3));
        dest_.add(posIdea_);
        state_.add(dest_);
        state_.add(new Idea("Desire", 0.1, "Property", 1));
        impulse_.add(state_);
        for (Memory mo : mems) {
            if (mo.getName() != null && mo.getName().equalsIgnoreCase("IMPULSES")) {
                MemoryContainer moc = (MemoryContainer) mo;
                moc.setI(impulse_, 0.1, "Explore");
                //System.out.println(fullPrint((Idea) mo.getI()));
            }
        }
        //env.creature.moveTo(0.5f,9f);
    }
}
