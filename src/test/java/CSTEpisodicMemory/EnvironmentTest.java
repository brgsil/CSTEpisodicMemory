/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package CSTEpisodicMemory;

import CSTEpisodicMemory.experiments.Environment;
import CSTEpisodicMemory.experiments.EnvironmentA;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnvironmentTest {

    @Test
    public void environmentInitializedTest() {
        Environment env = new EnvironmentA();
        Assertions.assertNotNull(env.creature);
        Assertions.assertNotNull(env.world);
        Assertions.assertTrue(env.initialized);

        env.stopSimulation();
        Assertions.assertFalse(env.initialized);
    }
}
