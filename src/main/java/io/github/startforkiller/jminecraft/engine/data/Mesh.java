package io.github.startforkiller.jminecraft.engine.data;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private final int vaoID;
    private final int posVboID;
    private final int texCoordsVboID;
    private final int shadingVboID; //TODO: extend this in another class
    private final int idxVboID;
    private final int vertexCount;

    public Mesh(float[] positions, float[] texCoords, int[] indices, float[] shading) {
        FloatBuffer posBuffer = null;
        FloatBuffer texCoordsBuffer = null;
        FloatBuffer shadingBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            vertexCount = indices.length;

            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            posVboID = glGenBuffers();
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboID);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            texCoordsVboID = glGenBuffers();
            texCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.length);
            texCoordsBuffer.put(texCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, texCoordsVboID);
            glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

            shadingVboID = glGenBuffers();
            shadingBuffer = MemoryUtil.memAllocFloat(shading.length);
            shadingBuffer.put(shading).flip();
            glBindBuffer(GL_ARRAY_BUFFER, shadingVboID);
            glBufferData(GL_ARRAY_BUFFER, shadingBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 1, GL_FLOAT, false, 0, 0);

            idxVboID = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if(posBuffer != null) MemoryUtil.memFree(posBuffer);
            if(texCoordsBuffer != null) MemoryUtil.memFree(texCoordsBuffer);
            if(shadingBuffer != null) MemoryUtil.memFree(shadingBuffer);
            if(indicesBuffer != null) MemoryUtil.memFree(indicesBuffer);
        }
    }

    public void render() {
        glBindVertexArray(getVaoID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_VERTEX_ARRAY, 0);
        glDeleteBuffers(posVboID);
        glDeleteBuffers(texCoordsVboID);
        glDeleteBuffers(shadingVboID);
        glDeleteBuffers(idxVboID);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoID);
    }

}
