package fbanna.easyminigame.config;

import fbanna.easyminigame.EasyMiniGame;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.GameMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static fbanna.easyminigame.EasyMiniGame.PARENTFOLDER;

public class DelConfig {

    public static Boolean deleteParentFolder() {
        try {
            if(Files.exists(PARENTFOLDER)) {
                Files.delete(PARENTFOLDER);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Boolean deleteSaveStates() {
        try {
            Path path = PARENTFOLDER.resolve("playerState.json");
            if(Files.exists(path)) {
                Files.delete(path);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Optional<Boolean> deleteGame(Game game) {

        try {
            Path path = PARENTFOLDER.resolve(Path.of(game.getName()));
            EasyMiniGame.LOGGER.info(String.valueOf(path));
            if(Files.exists(path)) {
                //Files.delete(path);
                deleteRecursively(path);


                return Optional.of(true);
            } else {
                return Optional.of(false);
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Boolean> deleteMap(Game game, GameMap gameMap) {

        try {
            Path path = PARENTFOLDER.resolve(Path.of(game.getName())).resolve(Path.of(gameMap.getName()));
            if(Files.exists(path)) {
                deleteRecursively(path);
                return Optional.of(true);
            } else {
                return Optional.of(false);
            }
        } catch (Exception e) {
            return  Optional.empty();
        }

    }

    public static void deleteRecursively(Path path) throws IOException {

        if(Files.isDirectory(path)) {

            List<Path> list = Files.list(path).toList();

            if(!list.isEmpty()) {
                for(Path folder: list) {

                    deleteRecursively(folder);
                }
            }
        }

        Files.delete(path);

    }
}
