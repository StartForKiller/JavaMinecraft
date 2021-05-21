package io.github.startforkiller.jminecraft.engine.data;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class HitRay {

    public static int HIT_RANGE = 5;

    private final Vector3f vector;
    private Vector3f position;
    private Vector3f block;
    private final World world;
    public float currDistance;

    public HitRay(Vector3f rotation, Vector3f startingPos, World world) {
        this.vector = new Vector3f(
                (float)(Math.cos(rotation.x) * Math.cos(rotation.y)),
                (float)Math.sin(rotation.y),
                (float)(Math.sin(rotation.x) * Math.cos(rotation.y))
        );

        this.position = new Vector3f(startingPos);
        this.block = new Vector3f(Math.round(this.position.x), Math.round(this.position.y), Math.round(this.position.z));

        this.world = world;

        currDistance = 0;
    }

    private boolean check(Function<Vector3f[], Integer> callback, float distance, Vector3f currBlock, Vector3f nextBlock) {
        if(world.getBlockNumber(nextBlock.x, nextBlock.y, nextBlock.z) != 0) {
            callback.apply(new Vector3f[]{currBlock, nextBlock});
            return true;
        } else {
            this.position.add(this.vector.x * distance, this.vector.y * distance, this.vector.z * distance);
            block = nextBlock;
            currDistance += distance;
        }

        return false;
    }

    public boolean step(Function<Vector3f[], Integer> callback) {
        int bx = (int) this.block.x;
        int by = (int) this.block.y;
        int bz = (int) this.block.z;

        Vector3f localPos = new Vector3f(this.position.x - bx, this.position.y - by, this.position.z - bz);

        Vector3f absVector = new Vector3f(this.vector);

        Vector3i sign = new Vector3i(1, 1, 1);

        if(this.vector.x < 0) {
            sign.x = -1;
            absVector.x = -absVector.x;
            localPos.x = -localPos.x;
        }
        if(this.vector.y < 0) {
            sign.y = -1;
            absVector.y = -absVector.y;
            localPos.y = -localPos.y;
        }
        if(this.vector.z < 0) {
            sign.z = -1;
            absVector.z = -absVector.z;
            localPos.z = -localPos.z;
        }

        float lx = localPos.x;
        float ly = localPos.y;
        float lz = localPos.z;
        float vx = absVector.x;
        float vy = absVector.y;
        float vz = absVector.z;

        if(vx != 0.0f) {
            float x = 0.5f;
            float y = (((0.5f - lx) / vx) * vy) + ly;
            float z = (((0.5f - lx) / vx) * vz) + lz;

            if(y >= -0.5f && y <= 0.5f && z >= -0.5f && z <= 0.5f) {
                float distance = (float)Math.sqrt(Math.pow(x - lx, 2) + Math.pow(y - ly, 2) + Math.pow(z - lz, 2));
                return this.check(callback, distance, new Vector3f(bx, by, bz), new Vector3f(bx + sign.x, by, bz));
            }
        }

        if(vy != 0.0f) {
            float x = (((0.5f - ly) / vy) * vx) + lx;
            float y = 0.5f;
            float z = (((0.5f - ly) / vy) * vz) + lz;

            if(x >= -0.5f && x <= 0.5f && z >= -0.5f && z <= 0.5f) {
                float distance = (float)Math.sqrt(Math.pow(x - lx, 2) + Math.pow(y - ly, 2) + Math.pow(z- lz, 2));
                return this.check(callback, distance, new Vector3f(bx, by, bz), new Vector3f(bx, by + sign.y, bz));
            }
        }

        if(vz != 0.0f) {
            float x = (((0.5f - lz) / vz) * vx) + lx;
            float y = (((0.5f - lz) / vz) * vy) + ly;
            float z = 0.5f;

            if(x >= -0.5f && x <= 0.5f && y >= -0.5f && y <= 0.5f) {
                float distance = (float)Math.sqrt(Math.pow(x - lx, 2) + Math.pow(y - ly, 2) + Math.pow(z- lz, 2));
                return this.check(callback, distance, new Vector3f(bx, by, bz), new Vector3f(bx, by, bz + sign.z));
            }
        }

        return false;
    }

}
