package org.bk.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import org.bk.Game;

/**
 * Created by dante on 24.10.2016.
 */
public class MapScreen extends ScreenAdapter {
    private final Game game;

    public MapScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        Table root = new Table();
        game.stage.addActor(root);
    }
}
