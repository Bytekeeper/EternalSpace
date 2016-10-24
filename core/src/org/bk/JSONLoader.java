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
import org.bk.data.component.*;
import org.bk.data.*;
import org.bk.data.Faction;

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
        json.addClassTag("Entity", EntityTemplate.class);
        json.addClassTag("System", SolarSystem.class);
        List<Class<? extends Component>> compontentClasses = Arrays.asList(Body.class, Physics.class, Movement.class, Ship.class, Steering.class,
                Transform.class, Health.class, Mounts.class, Celestial.class, Asteroid.class, Projectile.class, LifeTime.class, Orbiting.class,
                LandingPlace.class);
        for (Class<?> c: compontentClasses) {
            json.addClassTag(c.getSimpleName(), c);
        }
        json.setSerializer(Entity.class, new ReferenceSerializer<Entity>(GameData.EntityRef.class));
        json.setSerializer(Faction.class, new ReferenceSerializer<Faction>(GameData.FactionRef.class));
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

    public static class ReferenceSerializer<T> implements Json.Serializer<T> {
        private final Class<? extends T> _class;

        public ReferenceSerializer(Class<? extends T> aClass) {
            _class = aClass;
        }

        @Override
        public void write(Json json, T object, Class knownType) {

        }

        @Override
        public T read(Json json, JsonValue jsonData, Class type) {
            try {
                T instance = _class.newInstance();
                ((GameData.Reference) instance).setId(jsonData.asString());
                return instance;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
