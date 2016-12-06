package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.Game;
import org.bk.data.component.AnimationComponent;
import org.bk.data.component.Body;
import org.bk.data.component.Effect;
import org.bk.data.component.TextureComponent;

import static org.bk.data.component.Mapper.BODY;

/**
 * Created by dante on 05.12.2016.
 */
public class AssetSystem extends IteratingSystem {
    private final Game game;

    public AssetSystem(Game game) {
        super(Family.all(Body.class).exclude(TextureComponent.class, Effect.class, AnimationComponent.class).get());
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Body body = BODY.get(entity);
        String graphics = body.graphics;
        if (!graphics.contains("#")) {
            TextureComponent textureComponent = getEngine().createComponent(TextureComponent.class);
            textureComponent.textureRegion = game.assets.textures.get(graphics);
            entity.add(textureComponent);
        } else {
            AnimationComponent animationComponent = getEngine().createComponent(AnimationComponent.class);
            animationComponent.frameDuration = 0.08f;
            String name = graphics.substring(0, graphics.indexOf("#"));
            int numFrames = Integer.parseInt(graphics.substring(graphics.indexOf("#") + 1));
            for (int i = 1; i < numFrames; i++) {
                animationComponent.frames.add(game.assets.textures.get(name + "#" + i));
            }
            entity.add(animationComponent);
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
