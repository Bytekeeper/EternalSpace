package org.bk;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import org.bk.component.*;
import org.bk.data.EntityDef;
import org.bk.data.GameData;
import org.bk.data.SystemDef;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dante on 22.10.2016.
 */
public class JSONLoader<T> extends AsynchronousAssetLoader<T, JSONLoader.GameDataParameters<T>> {
    private final Class<T> classToLoad;
    private T loaded;

    public JSONLoader(Class<T> classToLoad, FileHandleResolver resolver) {
        super(resolver);
        this.classToLoad = classToLoad;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, GameDataParameters<T> parameter) {
        Json json = new Json();
        json.addClassTag("Entity", EntityDef.class);
        json.addClassTag("System", SystemDef.class);
        List<Class<? extends Component>> compontentClasses = Arrays.asList(Body.class, Physics.class, Movement.class, Ship.class, Steering.class,
                Transform.class, Health.class, Mounts.class, Planet.class, Asteroid.class, Projectile.class, LifeTime.class, Orbiting.class);
        for (Class<?> c: compontentClasses) {
            json.addClassTag(c.getSimpleName(), c);
        }
        json.setSerializer(Entity.class, new Json.Serializer<Entity>() {
            @Override
            public void write(Json json, Entity object, Class knownType) {
            }

            @Override
            public Entity read(Json json, JsonValue jsonData, Class type) {
                GameData.EntityRef entityRef = new GameData.EntityRef();
                entityRef.id = jsonData.asString();
                return entityRef;
            }
        });
        loaded = json.fromJson(classToLoad, file);
    }

    @Override
    public T loadSync(AssetManager manager, String fileName, FileHandle file, GameDataParameters<T> parameter) {
        T result = loaded;
        loaded = null;
        return result;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, GameDataParameters<T> parameter) {
        return null;
    }

    public static class GameDataParameters<T> extends AssetLoaderParameters<T> {
    }
}
