package org.bk.system;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 06.12.2016.
 */
public class SoundComponent implements Component, Pool.Poolable {
    public long id;

    @Override
    public void reset() {
        id = 0;
    }
}
