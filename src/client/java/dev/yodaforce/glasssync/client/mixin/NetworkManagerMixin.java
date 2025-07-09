package dev.yodaforce.glasssync.client.mixin;

import dev.yodaforce.glasssync.client.GlassSyncClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class NetworkManagerMixin {
    @Unique
    private static BlockPos lastUpdatedToFullConnectedPane = null;
    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (GlassSyncClient.latestGemBreaking == null) return; // If the player hasn't started breaking a gem block, we don't need to do anything

        if (packet instanceof BlockUpdateS2CPacket blockUpdatePacket) {
            if (GlassSyncClient.posEquals(GlassSyncClient.latestGemBreaking, blockUpdatePacket.getPos())) {
                BlockState newState = blockUpdatePacket.getState();
                if (newState.isAir()) {
                    //If the block is now air, aka has been broken.
                    for (Direction direction : Direction.Type.HORIZONTAL) {
                        BlockPos neighborPos = blockUpdatePacket.getPos().offset(direction);
                        BlockState neighborState = MinecraftClient.getInstance().world.getBlockState(neighborPos);
                        if (neighborState.getBlock() instanceof PaneBlock) {
                            var nProperty = PaneBlock.FACING_PROPERTIES.get(direction.getOpposite());

                            MinecraftClient.getInstance().execute(() -> {
                                // Run on Main Render Thread to stop sodium complaining
                                var nState = neighborState.with(nProperty, false);
                                if (GlassSyncClient.isDisconnectedPane(nState, direction.getOpposite())) {
                                    //Make it a full connected pane like how hypixel makes it on 1.8.9
                                    lastUpdatedToFullConnectedPane = neighborPos;
                                    MinecraftClient.getInstance().world.setBlockState(neighborPos, GlassSyncClient.createFullConnectedPane(neighborState));
                                } else {
                                    //Hacky fix to not double update the same pane? No idea why it tries to double update, but this appears to work as a fix.
                                    if (!neighborPos.equals(lastUpdatedToFullConnectedPane)) {
                                        MinecraftClient.getInstance().world.setBlockState(neighborPos, nState);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }
}
