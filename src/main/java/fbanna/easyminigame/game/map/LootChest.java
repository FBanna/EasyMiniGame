package fbanna.easyminigame.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record LootChest(BlockPos pos, Identifier lootTable) {
    public static final Codec<LootChest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(LootChest::pos),
            Identifier.CODEC.fieldOf("lootTable").forGetter(LootChest::lootTable)
    ).apply(instance, LootChest::new));
}