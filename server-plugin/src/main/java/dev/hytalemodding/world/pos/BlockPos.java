package dev.hytalemodding.world.pos;

import com.hypixel.hytale.math.util.ChunkUtil;

public record BlockPos(int x, int z) {

    public int chunkX() {
        return ChunkUtil.chunkCoordinate(x);
    }

    public int chunkZ() {
        return ChunkUtil.chunkCoordinate(z);
    }
}
