package org.bk.graphics;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
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
    private Shape playerShipOutline;
    private Shape targetShipOutline;

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
        br.pad(10);
        br.bottom().right();
        Table tr = new Table();
        tr.setFillParent(true);
        tr.pad(10);
        tr.padTop(80);
        tr.top().right();
        Table tl = new Table();
        tl.setFillParent(true);
        tl.pad(10);
        tl.padTop(400);
        tl.top().left();
        topLeft(assets, tl);
        topRight(assets, tr);
        bottomRight(assets, br);
        addActor(tl);
        addActor(tr);
        addActor(br);
    }

    private void topLeft(Assets assets, Table tl) {
        targetShipOutline = new Shape(game.shape);
        tl.add(targetShipOutline);
    }

    private void topRight(Assets assets, Table tr) {
        playerShipOutline = new Shape(game.shape);
        tr.add(playerShipOutline);
    }

    private void bottomRight(Assets assets, Table br) {

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
    }

    @Override
    public void act(float delta) {
        Health health = HEALTH.get(game.player);
        Battery battery = BATTERY.get(game.player);
        Shield shield = SHIELD.get(game.player);
        Account account = ACCOUNT.get(game.player);
        playerShipOutline.setShape(game.assets.outlineOf(BODY.get(game.player).graphics));
        Steering steering = STEERING.get(game.player);
        if (steering != null) {
            Entity selectedEntity = steering.selectedEntity;
            if (selectedEntity == null || !BODY.has(selectedEntity) || !TRANSFORM.has(selectedEntity)) {
                targetShipOutline.setShape(null);
            } else {
                targetShipOutline.setShape(game.assets.outlineOf(BODY.get(selectedEntity).graphics));
                targetShipOutline.setRotation(TRANSFORM.get(selectedEntity).orientRad * MathUtils.radDeg);
            }
        }

        healthBar.setFactor(health.hull / health.maxHull);
        powerBar.setFactor(battery.capacity / battery.maxCapacity);
        shieldBar.setFactor(shield.shields / shield.maxShields);
        creditsAmount.setText(String.format("%10d", account.credits));
    }
}
