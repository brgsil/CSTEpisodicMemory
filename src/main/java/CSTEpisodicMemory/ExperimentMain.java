/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package CSTEpisodicMemory;

import br.unicamp.cst.util.viewer.MindViewer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExperimentMain {

    public static void main(String[] args) {
        Logger.getLogger("codelets").setLevel(Level.SEVERE);
        // Create Environment
        Environment env=new Environment(); //Creates only a creature and some apples
        AgentMind a = new AgentMind(env);  // Creates the Agent Mind and start it
        // The following lines create the MindViewer and configure it
        MindViewer mv = new MindViewer(a,"MindViewer", a.bList);
        mv.setVisible(true);
        env.creature.moveTo(2f,2f);
    }
}
