package fbanna.easyminigame.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fbanna.easyminigame.EasyMiniGame;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.GameMap;
import fbanna.easyminigame.play.GameManager;
import fbanna.easyminigame.play.PlayerState;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fbanna.easyminigame.EasyMiniGame.CONFIG;
import static fbanna.easyminigame.EasyMiniGame.PARENTFOLDER;

public class GetConfig {

    public static Optional<ArrayList<Game>> getGames() {
        GenConfig.makeParentFolder();

        try{
            List<Path> list = Files.list(PARENTFOLDER).toList();

            ArrayList<Game> games = new ArrayList<>();

            for(Path folder: list) {
                if(Files.isDirectory(folder)){

                    Optional<Game> optionalGame = getGame(folder);

                    if(optionalGame.isPresent()) {
                        games.add(optionalGame.get());
                    }
                    /*
                    try {
                        games.add(getGame(folder));

                    } catch (Exception ignored) {}*/

                }
            }
            return Optional.of(games);
        } catch (Exception e) {
            return Optional.empty();
        }

    }

    public static Optional<ArrayList<GameMap>> getMaps(Game game) {

        try{
            List<Path> list = Files.list(PARENTFOLDER.resolve(game.getName())).toList();

            ArrayList<GameMap> maps = new ArrayList<>();

            for(Path folder: list) {
                if(Files.isDirectory(folder)){

                    Optional<GameMap> optionalMap = getGameMap(folder);

                    if(optionalMap.isPresent()){
                        maps.add(optionalMap.get());
                    }
                    //maps.add(getGameMap(folder));
                }
            }
            return Optional.of(maps);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<List<PlayerState>> getSaveStates() {
        Path path = PARENTFOLDER.resolve("playerState.json");

        try{

            String json = Files.readString(path);
            JsonElement element = JsonParser.parseString(json);
            DataResult<List<PlayerState>> result = PlayerState.CODEC.listOf().parse(JsonOps.INSTANCE, element);

            if(result.isSuccess()) {
                return Optional.of(result.getOrThrow());
            }


            /*
            Type listType = new TypeToken<ArrayList<PlayerState>>(){}.getType();

            Gson gson = new Gson();
            //SaveStates saveStates = gson.fromJson(json, SaveStates.class);
            List<PlayerState> states = gson.fromJson(json, listType);*/
            return Optional.empty();
        } catch (Exception e) {
            EasyMiniGame.LOGGER.info("eror" + e);
            return Optional.empty();
        }


    }



    private static Optional<Game> getGame(Path path) throws IOException {
        Path config = path.resolve(CONFIG);

        try{

            String json = Files.readString(config);
            JsonElement element = JsonParser.parseString(json);
            DataResult<Game> result = Game.CODEC.parse(JsonOps.INSTANCE, element);

            if(result.isSuccess()) {
                return Optional.of(result.getOrThrow());
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }

        /*



        String json = Files.readString(config);
        Gson gson = new Gson();
        Game game = gson.fromJson(json, Game.class);
        return game;*/


    }

    private static Optional<GameMap> getGameMap(Path path) throws IOException {
        Path config = path.resolve(CONFIG);

        try{

            String json = Files.readString(config);
            JsonElement element = JsonParser.parseString(json);
            DataResult<GameMap> result = GameMap.CODEC.parse(JsonOps.INSTANCE, element);

            if(result.isSuccess()) {
                return Optional.of(result.getOrThrow());
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }

        /*

        String json = Files.readString(config);
        Gson gson = new Gson();
        GameMap map = gson.fromJson(json, GameMap.class);
        return map;*/
    }


}
