package fbanna.easyminigame.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static fbanna.easyminigame.EasyMiniGame.MANAGER;


@Mixin(PlayerManager.class)
public class MixinJoin {

    @Shadow @Final private static Logger LOGGER;

    @WrapOperation(
            method = "onPlayerConnect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;loadPlayerData(Lnet/minecraft/server/network/ServerPlayerEntity;)Ljava/util/Optional;")
    )
    private Optional<NbtCompound> mixin(PlayerManager instance, ServerPlayerEntity player, Operation<Optional<NbtCompound>> original){
        Optional<NbtCompound> nbt = MANAGER.unregisterPlayerWithoutUpdating(player);
        if(nbt.isPresent()) {
            player.readNbt(nbt.get());
            return nbt;
        } else {
            return original.call(instance, player);
        }

    }
}

