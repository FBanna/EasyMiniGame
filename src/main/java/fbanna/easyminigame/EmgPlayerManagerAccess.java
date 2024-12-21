package fbanna.easyminigame;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface EmgPlayerManagerAccess {
    void emg$updatePlayer(ServerPlayerEntity player, NbtCompound nbt);
}
