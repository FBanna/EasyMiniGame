package fbanna.easyminigame.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fbanna.easyminigame.EasyMiniGame;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static fbanna.easyminigame.dimension.MiniGameDimension.EMG_DIMENSION_KEY;

@Mixin(FireBlock.class)
public class MixinTick {


    @ModifyExpressionValue(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean Inject(boolean original, @Local(argsOnly = true) ServerWorld world) {
        //return original || world.getRegistryKey() == EMG_DIMENSION_KEY;
        if(world.getRegistryKey() == EMG_DIMENSION_KEY) {
            return false;
        } else {
            return original;
        }


        //return original && this.world.getRegistryKey() != EMG_DIMENSION_KEY;
    }
}

/*
@Mixin(ServerChunkManager.class)
public class MixinTick {

    @Shadow @Final private ServerWorld world;

    @Inject(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/tick/TickManager;shouldTick()Z"), cancellable = true)
    private void Inject(CallbackInfo ci) {
        if(this.world.getRegistryKey() == EMG_DIMENSION_KEY) {
            EasyMiniGame.LOGGER.info("cancelling");
            ci.cancel();
        }
        //return original && this.world.getRegistryKey() != EMG_DIMENSION_KEY;
    }
}*/

/*
@Mixin(ServerWorld.class)
public abstract class MixinTick {

    @Shadow public abstract ServerWorld toServerWorld();

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isDebugWorld()Z"))
    private boolean Inject(boolean original) {
        //return original || this.getRegistryKey() == EMG_DIMENSION_KEY;
        return original && this.toServerWorld().getRegistryKey() != EMG_DIMENSION_KEY;
    }
}*/

/*
@Mixin(ServerWorld.class)
public abstract class MixinTick {


    @Shadow public abstract ServerWorld toServerWorld();

    @Inject(method = "tickChunk", at = @At(value = "HEAD"), cancellable = true)
    private void Inject(CallbackInfo ci) {
        if(this.toServerWorld().getRegistryKey() == EMG_DIMENSION_KEY) {
            EasyMiniGame.LOGGER.info("here! cancelling¬!");
            ci.cancel();
        }
    }
}*/




