package fbanna.easyminigame.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fbanna.easyminigame.EasyMiniGame;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static fbanna.easyminigame.dimension.MiniGameDimension.EMG_DIMENSION_KEY;

@Mixin(PlayerManager.class)
public class MixinJoin {

    @Shadow MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setServerWorld(Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.BEFORE))
    private void mixin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci, @Local(ordinal = 1) ServerWorld serverWorld, @Local(ordinal = 0) LocalRef<ServerWorld> serverWorld2){

        EasyMiniGame.LOGGER.info(serverWorld.getRegistryKey().toString() + ", " + serverWorld2.get().getRegistryKey().toString());
        if( serverWorld == this.server.getWorld(EMG_DIMENSION_KEY) ) {
            serverWorld2.set(this.server.getWorld(ServerWorld.NETHER));
        }

    }
}
