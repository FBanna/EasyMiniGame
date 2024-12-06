package fbanna.easyminigame.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static fbanna.easyminigame.dimension.MiniGameDimension.EMG_DIMENSION_KEY;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinSpectatorTeleport {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onSpectatorTeleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FFZ)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void inject(SpectatorTeleportC2SPacket packet, CallbackInfo ci, @Local Entity entity){
        if(entity.getWorld().getRegistryKey() != EMG_DIMENSION_KEY && player.getWorld().getRegistryKey() == EMG_DIMENSION_KEY) {
            player.sendMessage(Text.translatable("You can't spectate players out of the game").formatted(Formatting.RED), true);
            ci.cancel();
        }
    }

}
