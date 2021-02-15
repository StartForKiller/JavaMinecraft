package io.github.startforkiller.jminecraft.engine;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.lwjgl.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

import static org.lwjgl.BufferUtils.*;

public class Utils {

    public static String loadResource(String fileName) throws Exception {
        String result;
        try(InputStream in = Utils.class.getResourceAsStream(fileName); Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        }

        return result;
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws Exception {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1) {
                    ;
                }
            }
        } else {
            try (
                    InputStream source = Utils.class.getResourceAsStream(resource)
            ) {
                if(source == null) throw new Exception();
                ReadableByteChannel rbc = Channels.newChannel(source);
                buffer = BufferUtils.createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }

}
