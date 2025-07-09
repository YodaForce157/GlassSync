package dev.yodaforce.glasssync.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;

public class GlassSyncClient implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    //Store the latest gem breaking position, to avoid updating non-gem blocks or blocks not broken by the player
    public static BlockPos latestGemBreaking = null;

    @Override
    public void onInitializeClient() {
        LOGGER.info("GlassSync Client Initialized");
    }

    public static boolean posEquals(BlockPos pos1, BlockPos pos2) {
        if (pos1 == null || pos2 == null) return false;
        return pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ();
    }

    public static boolean isDisconnectedPane(BlockState state, Direction toIgnore) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (direction == toIgnore) continue; // Ignore the direction of the just broken block
            if (state.get(PaneBlock.FACING_PROPERTIES.get(direction))) {
                return false; // Found a non disconnected pane
            }
        }
        return true; // No disconnected panes found
    }

    public static BlockState createFullConnectedPane(BlockState state) {
        BlockState newState = state;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            newState = newState.with(PaneBlock.FACING_PROPERTIES.get(direction), true);
        }
        return newState; // Return the state with all horizontal connections set to true
    }
}
