package org.bk.data;

import com.badlogic.ashley.core.Entity;
import org.bk.Game;
import org.bk.data.component.Landing;

import static org.bk.data.component.Mapper.LANDING;

/**
 * Created by dante on 02.11.2016.
 */
public class Condition {
    public Entity at;

    public boolean conditionsMet(Game game) {
        boolean hasCondition = false;
        if (at != null) {
            hasCondition = true;
            Landing landing = LANDING.get(game.player);
            if (landing != null && landing.target != at) {
                return false;
            }
        }
        return hasCondition;
    }
}
