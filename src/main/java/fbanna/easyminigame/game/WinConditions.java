package fbanna.easyminigame.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.world.GameMode;

public enum WinConditions implements StringIdentifiable{
    LAST_TEAM("last_team");

    private final String name;
    public static final Codec<WinConditions> CODEC = StringIdentifiable.createCodec(WinConditions::values);


    WinConditions(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
