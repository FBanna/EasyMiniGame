package fbanna.easyminigame.dimension;

import fbanna.easyminigame.EasyMiniGame;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;

public class MiniGameDimension {
    public static final RegistryKey<World> EMG_DIMENSION_KEY = RegistryKey.of(RegistryKeys.WORLD,Identifier.of(EasyMiniGame.MOD_ID, "gamedim"));
            //new Identifier(EasyMiniGame.MOD_ID, "gamedim"));
    public static final RegistryKey<DimensionType> EMG_DIM_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, EMG_DIMENSION_KEY.getValue());

    public static void register(){
        EasyMiniGame.LOGGER.info("Registering ModDimension");
    }

}
