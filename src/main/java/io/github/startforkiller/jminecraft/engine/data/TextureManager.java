package io.github.startforkiller.jminecraft.engine.data;

import io.github.startforkiller.jminecraft.engine.Utils;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import static org.lwjgl.stb.STBImage.*;

public class TextureManager {

    private final int textureWidth, textureHeight, maxTextures;

    private String[] textures;
    private int currentTexture = 0;

    private int textureArray;

    public TextureManager(int textureWidth, int textureHeight, int maxTextures) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.maxTextures = maxTextures;

        textures = new String[maxTextures];

        textureArray = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureArray);

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, textureWidth, textureHeight, maxTextures, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);

        stbi_set_flip_vertically_on_load(true);
    }

    public int getTextureIndex(String textureName) {
        for(int i = 0; i < maxTextures; i++) {
            if(textures[i] != null && textures[i].compareTo(textureName) == 0) {
                return i;
            }
        }

        return -1;
    }

    public void addTexture(String texture) throws Exception {
        if(getTextureIndex(texture) != -1) return;
        textures[currentTexture++] = texture;

        int width;
        int height;
        ByteBuffer buf;
        // Load Texture file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer imageBuffer;
            try {
                imageBuffer = Utils.ioResourceToByteBuffer("/textures/" + texture + ".png", 8 * 1024);
            } catch (Exception e) {
                throw new Exception("Image file [/textures/" + texture + ".png] not loaded: file not found");
            }

            buf = stbi_load_from_memory(imageBuffer, w, h, channels, 4);
            if (buf == null) {
                throw new Exception("Image file [/textures/" + texture + ".png] not loaded: " + stbi_failure_reason());
            }

            /* Get width and height of image */
            width = w.get();
            height = h.get();
        }

        if(width != textureWidth || height != textureHeight) throw new Exception("Texture size mismatch: /textures/" + texture + ".png");

        glBindTexture(GL_TEXTURE_2D_ARRAY, textureArray);
        glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, (currentTexture - 1), textureWidth, textureHeight, 1, GL_RGBA, GL_UNSIGNED_BYTE, buf);
    }

    public void generateMipMaps() {
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureArray);
        glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
    }

    public int getTextureArray() {
        return textureArray;
    }
}
