package CSTEpisodicMemory.habits;

import br.unicamp.cst.representation.idea.Category;
import br.unicamp.cst.representation.idea.Idea;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static CSTEpisodicMemory.habits.LocationCategoryGenerator.START_RADIUS;

public class LocationCategoryHabitsTest {

    @Test
    public void locationCategoryHabitsTest(){
        Idea locGenHabit = new Idea("LocationGen", null, "AbstractObject", 2);
        locGenHabit.setValue(new LocationCategoryGenerator());

        Idea seedIdea = new Idea("Position",null);
        seedIdea.add(new Idea("X", 1f));
        seedIdea.add(new Idea("Y", 2f));

        Idea locCat1 = locGenHabit.exec(seedIdea);
        Assertions.assertTrue(locCat1.getValue() instanceof Category);
        Assertions.assertEquals(1d, locCat1.membership(seedIdea), 0.01);

        seedIdea.get("X").setValue((float) (1-START_RADIUS));
        Assertions.assertEquals(1d, locCat1.membership(seedIdea), 0.01);

        seedIdea.get("X").setValue((float) (1+2*START_RADIUS));
        double expected = Math.exp(-4*START_RADIUS*START_RADIUS) * Math.exp(START_RADIUS*START_RADIUS);
        Assertions.assertEquals(expected, locCat1.membership(seedIdea), 0.01);


        Idea locModHabit = new Idea("LocationAdapt", null);
        locModHabit.setValue(new LocationCategoryModification(locModHabit));

        Idea newSample = new Idea("Position", null);
        newSample.add(new Idea("X", 1.2f));
        newSample.add(new Idea("Y", 1.9f));

        locModHabit.add(newSample);
        locCat1 = locModHabit.exec(locCat1);

        Assertions.assertEquals(1*0.8+1.2*0.2, (float) locCat1.get("centerX").getValue(), 0.01);
        Assertions.assertEquals(2*0.8+1.9*0.2, (float) locCat1.get("centerY").getValue(), 0.01);
    }
}
