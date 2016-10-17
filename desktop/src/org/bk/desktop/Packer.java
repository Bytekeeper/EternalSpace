package org.bk.desktop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * Created by dante on 17.10.2016.
 */
public class Packer {
    public static void main(String[] args) {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = 1024;
        settings.maxHeight = 1024;
        settings.combineSubdirectories = true;
        settings.filterMin = Texture.TextureFilter.MipMapLinearLinear;
        settings.filterMag = Texture.TextureFilter.Linear;
        TexturePacker.process(settings, "core/assets_src", "core/assets", "textures");
    }
}
