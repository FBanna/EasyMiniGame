package fbanna.easyminigame.dimension;

import com.fasterxml.jackson.databind.ObjectWriter;
import fbanna.easyminigame.EasyMiniGame;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import org.apache.logging.log4j.core.jmx.Server;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.ChunkGeneratorSettingsProvider;

import java.sql.Time;
import java.util.*;

import static fbanna.easyminigame.EasyMiniGame.LOGGER;

public class MiniGameDimension {

    //public static final RegistryKey<World> EMG_DIMENSION_KEY = RegistryKey.of(RegistryKeys.WORLD,Identifier.of(EasyMiniGame.MOD_ID, "gamedim"));
            //new Identifier(EasyMiniGame.MOD_ID, "gamedim"));
    //public static final RegistryKey<DimensionType> EMG_DIM_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, EMG_DIMENSION_KEY.getValue());

    private final MinecraftServer server;
    private final Fantasy fantasy;
    private final RuntimeWorldConfig worldConfig;
    private List<RuntimeWorldHandle> handles = new ArrayList<>();

    public MiniGameDimension(MinecraftServer server) {
        LOGGER.info("Registering ModDimension");
        this.server = server;
        this.fantasy = Fantasy.get(this.server);

        this.worldConfig = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setGenerator(server.getOverworld().getChunkManager().getChunkGenerator())
                /*
                .setGenerator(
                        new FlatChunkGenerator(
                                new FlatChunkGeneratorConfig(
                                        Optional.empty(),
                                        RegistryEntry.of(null),
                                        List.of()
                                )
                        )
                )*/
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setDifficulty(Difficulty.HARD)
                .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, false)
                .setGameRule(GameRules.DO_WEATHER_CYCLE, false);
                //.set
        /*this.worldConfig = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setDifficulty(Difficulty.HARD)
                .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, false)
                .setGameRule(GameRules.DO_WEATHER_CYCLE, false)
                .setGenerator(server.getOverworld().getChunkManager().getChunkGenerator());*/
    }

    public RuntimeWorldHandle createDimension(String name) {
        RuntimeWorldHandle handle = fantasy.openTemporaryWorld(Identifier.of("easyminigame", name), this.worldConfig);
        this.handles.add(handle);

        return handle;
    }

    public RuntimeWorldHandle createDimension(String name, RegistryEntry<DimensionType> dimension) {
        this.worldConfig.setDimensionType(dimension);
        RuntimeWorldHandle handle = fantasy.openTemporaryWorld(Identifier.of("easyminigame", name), this.worldConfig);

        this.handles.add(handle);

        return handle;
    }

    public Optional<RuntimeWorldHandle> getHandle(String name) {
        if (handles.isEmpty()) {
            return Optional.empty();
        }

        for(RuntimeWorldHandle handle: handles){
            //LOGGER.info(handle.getRegistryKey().getValue().getPath() + " hi ", handle.getRegistryKey().getRegistry().getPath());
            if(Objects.equals(handle.getRegistryKey().getValue().getPath(), name)){
                //LOGGER.info("Success!");
                //handles.remove(handle);
                //handle.delete();
                return Optional.of(handle);
            }
        }

        return Optional.empty();
    }

    public void deleteDimension(String name){
        if (handles.isEmpty()) {
            return;
        }

        for(RuntimeWorldHandle handle: handles){
            LOGGER.info(handle.getRegistryKey().getValue().getPath() + " hi ", handle.getRegistryKey().getRegistry().getPath());
            if(Objects.equals(handle.getRegistryKey().getValue().getPath(), name)){
                LOGGER.info("Success!");
                handles.remove(handle);
                handle.delete();
                return;
            }
        }
    }

    public boolean isMiniGameDimension(ServerWorld world){

        if (handles.isEmpty()) {
            return false;
        }
        for (RuntimeWorldHandle handle: handles){
            if (handle.asWorld().getRegistryKey() == world.getRegistryKey()){
                return true;
            }
        }
        return false;
    }



}
