package org.bk.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import org.bk.Game;
import org.bk.data.SolarSystem;

import static org.bk.data.component.Mapper.STEERING;

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
        root.setFillParent(true);
        root.row().size(800, 600);
        Window window = new Window(game.msg("galaxyMap"), game.assets.skin);
        window.setMovable(false);
        root.add(window);

        game.stage.setKeyboardFocus(window);
        window.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (event.getKeyCode() == Input.Keys.ESCAPE) {
                    game.setScreen(null);
                    return true;
                }
                return super.keyDown(event, keycode);
            }
        });
        EventListener myChangeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SolarSystem targetSystem = (SolarSystem) actor.getUserObject();
                if (game.currentSystem != targetSystem) {
                    STEERING.get(game.playerEntity).jumpTo = targetSystem;
                }
            }
        };
        Group mapView = new Group();
        ButtonGroup<CheckBox> buttonGroup = new ButtonGroup<CheckBox>();
        for (SolarSystem solarSystem : game.gameData.getSystem()) {
            CheckBox checkBox = new CheckBox(solarSystem.name, game.assets.skin);
            checkBox.setPosition(solarSystem.position.x + 1000, solarSystem.position.y + 1000);
            checkBox.addListener(myChangeListener);
            checkBox.setUserObject(solarSystem);
            buttonGroup.add(checkBox);
            mapView.addActor(checkBox);
        }

        mapView.setSize(2000, 2000);
        ScrollPane scrollPane = new ScrollPane(mapView);
        scrollPane.setFlingTime(0.1f);
        window.row().fill().expand();
        window.add(scrollPane);
        root.layout();
        scrollPane.scrollTo(600, 700, 800, 600);
        game.stage.addActor(root);
    }
}
