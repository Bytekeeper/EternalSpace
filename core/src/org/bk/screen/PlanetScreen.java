package org.bk.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import org.bk.Game;
import org.bk.component.Landing;

import static org.bk.component.Mapper.LANDING;

/**
 * Created by dante on 19.10.2016.
 */
public class PlanetScreen extends ScreenAdapter {
    private final Game game;

    public PlanetScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        Skin skin = game.assets.skin;
        Table root = new Table(skin);
        root.setFillParent(true);
        Window window = new Window("Planet", skin);
        window.setMovable(false);
        TextButton departButton = new TextButton(game.msg("depart"), skin);
        departButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Landing landing = LANDING.get(game.player);
                landing.landingDirection = Landing.LandingDirection.DEPART;
                landing.timeRemaining = Landing.LAND_OR_LIFTOFF_DURATION;
                landing.landed = false;
            }
        });
        window.add(departButton);
        root.add(window);
        game.stage.addActor(root);

    }
}
