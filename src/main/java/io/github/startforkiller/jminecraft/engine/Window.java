package io.github.startforkiller.jminecraft.engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final String screenTitle;
    private int width, height;
    private long windowHandle;
    private boolean resized, vSync;

    public Window(String screenTitle, int width, int height, boolean vSync) {
        this.screenTitle = screenTitle;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit()) {
            throw new IllegalStateException("GLFW Initialization failed: glfwInit");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        windowHandle = glfwCreateWindow(width, height, screenTitle, NULL, NULL);
        if(windowHandle == NULL) {
            throw new RuntimeException("GLFW Initialization failed: glfwCreateWindow");
        }

        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        glfwSetKeyCallback(windowHandle, (window, key, scancode, action ,mods) -> {
           if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
               glfwSetWindowShouldClose(window, true);
           }
        });

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(windowHandle, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);

        glfwMakeContextCurrent(windowHandle);

        if(isvSync()) glfwSwapInterval(1);

        glfwShowWindow(windowHandle);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public void setClearColor(float r, float g, float b, float alpha) {
        glClearColor(r, g, b, alpha);
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public String getScreenTitle() {
        return screenTitle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public boolean isvSync() {
        return vSync;
    }

    public void setvSync(boolean vSync) {
        this.vSync = vSync;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

}
