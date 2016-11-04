package org.bk.graphics;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import org.bk.Assets;
import org.bk.Game;
import org.bk.data.component.*;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 03.11.2016.
 */
public class Hud extends WidgetGroup {

    private final Game game;
    private final StatusBar healthBar;
    private final StatusBar shieldBar;
    private final StatusBar powerBar;
    private final Label creditsAmount;

    public Hud(Game game, Assets assets) {
        this.game = game;
        NinePatchDrawable background = new NinePatchDrawable(assets.skin.getPatch("default-round"));
        NinePatchDrawable foreground = new NinePatchDrawable(assets.skin.getPatch("default-round-down"));
        healthBar = new StatusBar(background, foreground);
        powerBar = new StatusBar(background, foreground);
        shieldBar = new StatusBar(background, foreground);
        creditsAmount = new Label("", assets.skin);

        setFillParent(true);
        Table br = new Table();
        br.setFillParent(true);
        br.bottom().right();

        Image creditsIcon = new Image(assets.textures.get("ui/credits"));
        creditsIcon.setScale(0.8f, 0.8f);
        br.add(creditsIcon);
        br.add(creditsAmount);

        br.row();
        Image healthIcon = new Image(assets.textures.get("ui/health"));
        healthIcon.setScale(0.8f, 0.8f);
        br.add(healthIcon);
        br.add(healthBar).size(100, 10);
        br.row();
        Image shieldIcon = new Image(assets.textures.get("ui/shield"));
        shieldIcon.setScale(0.8f, 0.8f);
        br.add(shieldIcon);
        br.add(shieldBar).size(100, 10);
        br.row();
        Image powerIcon = new Image(assets.textures.get("ui/power"));
        powerIcon.setScale(0.8f, 0.8f);
        br.add(powerIcon);
        br.add(powerBar).size(100, 10);
        addActor(br);
    }

    @Override
    public void act(float delta) {
        Health health = HEALTH.get(game.player);
        Battery battery = BATTERY.get(game.player);
        Shield shield = SHIELD.get(game.player);
        Account account = ACCOUNT.get(game.player);

        healthBar.setFactor(health.hull / health.maxHull);
        powerBar.setFactor(battery.capacity / battery.maxCapacity);
        shieldBar.setFactor(shield.shields / shield.maxShields);
        creditsAmount.setText(String.format("%10d", account.credits));
    }
}
