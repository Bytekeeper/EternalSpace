package org.bk;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import org.bk.component.*;
import org.bk.data.EntityDef;

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
        List<Class<? extends Component>> compontentClasses = Arrays.asList(Body.class, Physics.class, Movement.class, Ship.class, Steering.class,
                Transform.class, Health.class, Mounts.class, Planet.class, Asteroid.class, Projectile.class, LifeTime.class);
        for (Class<?> c: compontentClasses) {
            json.addClassTag(c.getSimpleName(), c);
        }
        json.setElementType(Mounts.class, "weapons", Mounts.Weapon.class);
        json.setElementType(Mounts.class, "thrusters", Mounts.Thruster.class);
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
