package org.bk.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import org.bk.Game;
import org.bk.data.Mission;
import org.bk.data.component.Landing;
import org.bk.data.script.*;

import static org.bk.data.component.Mapper.LANDING;

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
        final Window window = new Window("Celestial", skin);
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
        for (final Mission m : game.gameData.mission.values()) {
            if (m.done || !m.offerWhen.conditionsMet(game) || m.active) {
                continue;
            }
            window.row();
            final TextButton missionButton = new TextButton(m.title, skin);
            missionButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m.offered.accept(new MissionConversion(new ConversionResultHandler() {
                        @Override
                        public void result(ObjectSet<String> state) {
                            if (state.contains("accepted")) {
                                m.active = true;
                                game.gameData.activeMission.add(m);
                                window.getCell(missionButton).clearActor();
                            }
                        }
                    }));
                }
            });
            window.add(missionButton);
        }
        root.add(window);
        game.stage.addActor(root);
        for (Mission m: game.gameData.mission.values()) {
            if (m.done || !m.active) {
                continue;
            }
            if (m.failWhen.conditionsMet(game)) {
                m.active = false;
                m.done = true;
                m.failed.accept(new MissionConversion());
                game.gameData.activeMission.removeValue(m, true);
            } else if (m.succeedWhen.conditionsMet(game)) {
                m.active = false;
                m.done = true;
                m.succeeded.accept(new MissionConversion());
                m.wasSuccessful = true;
                game.gameData.activeMission.removeValue(m, true);
            }
        }
    }

    private class MissionConversion implements Visitor {
        private Array<Chain> chainStack = new Array<Chain>();
        private ObjectSet<String> conditions = new ObjectSet<String>();
        private ConversionResultHandler resultHandler;

        private MissionConversion() {
        }

        private MissionConversion(ConversionResultHandler resultHandler) {
            this.resultHandler = resultHandler;
        }


        @Override
        public void visit(final Text text) {
            chainStack.add(new Chain() {
                int index;
                @Override
                public boolean next() {
                    if (index >= text.line.size) {
                        return false;
                    }
                    String textToShow = text.line.get(index);
                    index++;
                    Dialog dialog = new Dialog("Text", game.assets.skin) {
                        @Override
                        protected void result(Object object) {
                            MissionConversion.this.next();
                        }
                    };
                    dialog.text(textToShow);
                    dialog.button("Ok");
                    dialog.show(game.stage);
                    return true;
                }
            });
            next();
        }

        @Override
        public void visit(If scriptIf) {
            if (conditions.contains(scriptIf.condition)) {
                visit(scriptIf.script);
            } else {
                next();
            }
        }

        @Override
        public void visit(Choice choice) {
            Dialog dialog = new Dialog("Choice", game.assets.skin) {
                @Override
                protected void result(Object object) {
                    Choice.Option option = (Choice.Option) object;
                    conditions.add(option.condition);
                    next();
                }
            };
            for (Choice.Option option : choice.options) {
                dialog.row();
                dialog.button(option.text.line.get(0), option);
            }
            dialog.show(game.stage);
        }

        @Override
        public void visit(final Script script) {
            chainStack.add(new Chain() {
                private int index;
                @Override
                public boolean next() {
                    if (index >= script.items.size) {
                        return false;
                    }
                    ScriptItem scriptItem = script.items.get(index);
                    index++;
                    scriptItem.accept(MissionConversion.this);
                    return true;
                }
            });
            next();
        }

        private void next() {
            while (true){
                if (chainStack.size == 0) {
                    if (resultHandler != null) {
                        resultHandler.result(conditions);
                    }
                    return;
                }
                boolean next = chainStack.peek().next();
                if (!next) {
                    chainStack.pop();
                } else {
                    return;
                }
            }
        }
    }

    private interface Chain {
        boolean next();
    }

    public interface ConversionResultHandler {
        void result(ObjectSet<String> state);
    }
}
