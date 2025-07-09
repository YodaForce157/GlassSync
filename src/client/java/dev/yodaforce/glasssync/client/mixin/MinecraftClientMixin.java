package dev.yodaforce.glasssync.client.mixin;

import dev.yodaforce.glasssync.client.GlassSyncClient;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Nullable public HitResult crosshairTarget;

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"))
    public void onBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (breaking) {
            if (this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult)this.crosshairTarget;
                BlockPos blockPos = blockHitResult.getBlockPos();
                Block block = MinecraftClient.getInstance().world.getBlockState(blockPos).getBlock();

                if (Registries.BLOCK.getId(block).toString().contains("stained_glass")) {
                    // If the block is stained-glass or stained-glass pane
                    GlassSyncClient.latestGemBreaking = blockPos;
                }
            }
        }
    }
}
