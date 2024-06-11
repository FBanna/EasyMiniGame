package fbanna.easyminigame.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record SpawnPoint(BlockPos pos, int yaw) {

    public static final Codec<SpawnPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(SpawnPoint::pos),
            Codec.INT.fieldOf("yaw").forGetter(SpawnPoint::yaw)
    ).apply(instance, SpawnPoint::new));
}
