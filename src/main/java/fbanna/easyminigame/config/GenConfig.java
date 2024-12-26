package fbanna.easyminigame.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fbanna.easyminigame.EasyMiniGame;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.GameMap;
import fbanna.easyminigame.play.GameManager;
import fbanna.easyminigame.play.PlayerState;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static fbanna.easyminigame.EasyMiniGame.LOGGER;
import static fbanna.easyminigame.EasyMiniGame.PARENTFOLDER;

public class GenConfig {


    public static boolean makeParentFolder() {
        try {
            if (!Files.exists(PARENTFOLDER)){
                Files.createDirectories(PARENTFOLDER);

            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }
    public static boolean makeGame(Game game) {

        if (!makeParentFolder()) {
            EasyMiniGame.LOGGER.info("failed to make parent folder!");
            return false;
        }



        try {
            Path path = PARENTFOLDER.resolve(Path.of(game.getName()));

            if(!Files.exists(path)) {
                Files.createDirectories(path);
            }

            return makeGameConfig(game);

        } catch (Exception e) {
            EasyMiniGame.LOGGER.info("failed to make game " + e);
            return false;
        }

    }

    public static boolean makeMap(Game game, GameMap gameMap) {
        if (!makeGame(game)) {return false;}

        try {

            Path path = PARENTFOLDER.resolve(Path.of(game.getName())).resolve(Path.of(gameMap.getName()));
            if(!Files.exists(path)) {
                Files.createDirectories(path);
            }
            return makeMapConfig(game,gameMap);

        } catch (Exception e) {
            EasyMiniGame.LOGGER.info("making map error " + e);
            return  false;
        }

    }


    //change name
    public static boolean makeSaveStates(NbtCompound nbt){


        try{
            Path path = PARENTFOLDER.resolve("playerdata.dat");

            if( !Files.exists(path) ) {
                Files.createFile(path);
            }

            LOGGER.info(nbt.toString());

            if(nbt.isEmpty()) {
                LOGGER.info("deleting save");
                DelConfig.deleteSaveStates();
                return true;
            }
            //Path path = Files.createTempFile(PARENTFOLDER, "playerdata", ".dat");

            NbtIo.writeCompressed(nbt, path);
            return true;
        } catch (Exception e){
            LOGGER.info("failed to write!");
        }
        return false;
    }


    /*

    public static boolean makeSaveStates(List<PlayerState> states) {

        try{
            //DataResult<JsonElement> result = PlayerState.CODEC.listOf().encodeStart(JsonOps.INSTANCE, states);

            DataResult<JsonElement> result = PlayerState.CODEC.listOf().encodeStart(JsonOps.INSTANCE, states);

            if(result.isSuccess()) {
                JsonElement jsonElement = result.getOrThrow();
                String json = jsonElement.toString();
                byte[] byteString = json.getBytes();

                Path path = PARENTFOLDER.resolve(Path.of("playerState.json"));

                try {
                    Files.write(path, byteString);
                    return true;
                } catch (Exception e) {
                    EasyMiniGame.LOGGER.info("1 " + e );
                    return false;
                }


            }
            //if(result.is)
            //result.getOrThrow();
            /*


            if(result.isSuccess()) {
                byte[] byteString = result.getOrThrow().getAsBigInteger().toByteArray();
                Path path = PARENTFOLDER.resolve(Path.of("playerState.json"));

                try {
                    Files.write(path, byteString);
                    return true;
                } catch (Exception e) {
                    EasyMiniGame.LOGGER.info("1 " + e );
                    return false;
                }
            } else {
                try{
                    result.getOrThrow();
                } catch (Exception e ) {
                    EasyMiniGame.LOGGER.info(" faile" + e );
                }

                return false;
            }
        } catch (Exception e ) {
            EasyMiniGame.LOGGER.info("error in gen " + e);
        }



            /*

        Gson gson = new Gson();
        String json = gson.toJson(states);
        byte[] byteString = json.getBytes();

        Path path = PARENTFOLDER.resolve(Path.of("playerState.json"));

        try {
            Files.write(path, byteString);
            return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }*/

    public static boolean makeGameConfig(Game game) {

        DataResult<JsonElement> result = Game.CODEC.encodeStart(JsonOps.INSTANCE, game);
        if (result.isSuccess()) {
            JsonElement jsonElement = result.getOrThrow();

            String json = jsonElement.toString();
            byte[] byteString = json.getBytes();

            Path path = PARENTFOLDER.resolve(Path.of(game.getName())).resolve(Path.of("config.json"));

            try {
                Files.write(path, byteString);
                return true;
            } catch (Exception e) {
                EasyMiniGame.LOGGER.info("1 " + e);
                return false;
            }


        }
        return false;
    }

    public static boolean makeMapConfig(Game game, GameMap map) {
        DataResult<JsonElement> result = GameMap.CODEC.encodeStart(JsonOps.INSTANCE, map);
        EasyMiniGame.LOGGER.info("tried");
        if (result.isSuccess()) {
            JsonElement jsonElement = result.getOrThrow();
            EasyMiniGame.LOGGER.info("good??");

            String json = jsonElement.toString();
            byte[] byteString = json.getBytes();

            Path path = PARENTFOLDER.resolve(Path.of(game.getName())).resolve(map.getName()).resolve(Path.of("config.json"));

            try {
                Files.write(path, byteString);
                return true;
            } catch (Exception e) {
                EasyMiniGame.LOGGER.info("1 " + e);
                return false;
            }


        }
        return false;
    }


}
