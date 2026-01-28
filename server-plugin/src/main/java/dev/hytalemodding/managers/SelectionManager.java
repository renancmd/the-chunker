package dev.hytalemodding.managers;

import dev.hytalemodding.world.pos.BlockPos;
import java.util.*;

public class SelectionManager {

    // Main list for the UI (keeps the order)
    private static final Map<UUID, List<BlockPos>> selections = new HashMap<>();

    // A "Checklist" for coloring (Set is faster for checking "contains")
    private static final Map<UUID, Set<BlockPos>> origins = new HashMap<>();

    public static void addSelection(UUID playerId, int blockX, int blockZ, boolean isOrigin) {
        BlockPos pos = new BlockPos(blockX, blockZ);

        // Add to the main list
        selections.computeIfAbsent(playerId, id -> new ArrayList<>()).add(pos);

        // If it is an origin, add to the "Checklist"
        if (isOrigin) {
            origins.computeIfAbsent(playerId, id -> new HashSet<>()).add(pos);
        }
    }

    public static List<BlockPos> getSelections(UUID playerId) {
        return selections.getOrDefault(playerId, List.of());
    }

    // Helper to check if a block should be colored
    public static boolean isOrigin(UUID playerId, BlockPos pos) {
        Set<BlockPos> playerOrigins = origins.get(playerId);
        return playerOrigins != null && playerOrigins.contains(pos);
    }

    public static void clearSelections(UUID playerId) {
        if (selections.containsKey(playerId)) selections.get(playerId).clear();
        if (origins.containsKey(playerId)) origins.get(playerId).clear();
    }

    public static void removeSelection(UUID playerId, int index) {
        List<BlockPos> playerSelections = selections.get(playerId);
        Set<BlockPos> playerOrigins = origins.get(playerId);

        if (playerSelections != null && index >= 0 && index < playerSelections.size()) {
            BlockPos removedPos = playerSelections.remove(index);

            // Also remove from the checklist if it exists there
            if (playerOrigins != null) {
                playerOrigins.remove(removedPos);
            }

            if (playerSelections.isEmpty()) {
                selections.remove(playerId);
                origins.remove(playerId);
            }
        }
    }
}