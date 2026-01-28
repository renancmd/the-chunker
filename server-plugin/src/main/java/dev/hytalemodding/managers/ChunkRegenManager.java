package dev.hytalemodding.managers;

import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import dev.hytalemodding.world.pos.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ChunkRegenManager {

    private static final int CHUNKS_PER_TICK = 5; // Higher = Faster but more Lag

    public static void startRegeneration(World world, PlayerRef player, List<BlockPos> targetChunks) {
        Queue<BlockPos> queue = new LinkedList<>(targetChunks);

        player.sendMessage(Message.raw("Starting regeneration of " + queue.size() + " chunks..."));

        processQueue(world, player, queue);
    }

    private static void processQueue(World world, PlayerRef player, Queue<BlockPos> queue) {
        if (queue.isEmpty()) {
            player.sendMessage(Message.raw("Regeneration Complete!"));
            // Refresh client chunks once at the end to ensure they see the changes
            refreshClientChunks(world, player);
            return;
        }

        // Process a batch of chunks
        for (int i = 0; i < CHUNKS_PER_TICK && !queue.isEmpty(); i++) {
            BlockPos pos = queue.poll();
            regenerateSingleChunk(world, pos.chunkX(), pos.chunkZ());
        }

        // Schedule the next batch for the next server tick
        world.execute(() -> processQueue(world, player, queue));
    }

    private static void regenerateSingleChunk(World world, int chunkX, int chunkZ) {
        try {
            ChunkStore chunkStore = world.getChunkStore();
            IChunkSaver saver = chunkStore.getSaver();

            if (saver == null) return;

            long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);

            // nload from RAM
            try {
                chunkStore.remove(chunkStore.getChunkReference(chunkIndex), RemoveReason.UNLOAD);
            } catch (Exception ignored) {}

            // Delete from Disk (This is usually blocking, be careful)
            saver.removeHolder(chunkX, chunkZ).join();
            saver.flush();

        } catch (Exception e) {
            System.out.println("Error regenerating chunk " + chunkX + "," + chunkZ + ": " + e.getMessage());
        }
    }

    private static void refreshClientChunks(World world, PlayerRef player) {
        try {
            ChunkTracker tracker = world.getEntityStore().getStore()
                    .getComponent(player.getReference(), ChunkTracker.getComponentType());
            if (tracker != null) {
                tracker.unloadAll(player);
            }
        } catch (Exception ignored) {}
    }
}