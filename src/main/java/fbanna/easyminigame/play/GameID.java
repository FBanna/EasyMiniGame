package fbanna.easyminigame.play;

import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.GameMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static fbanna.easyminigame.EasyMiniGame.DIMENSION;

public class GameID {
    private final UUID creator;
    private final Game game;
    private final GameMap map;
    private final Integer ID;
    private final ServerWorld world;

    public GameID(UUID creator, Game game, GameMap map, List<GameID> otherGames) {
        this.creator = creator;
        this.game = game;
        this.map = map;


        Integer newID = 0;

        Integer tempID;
        for(GameID otherGame: otherGames){
            tempID = getNewID(otherGame);

            if(tempID > newID) {
                newID = tempID;
            }
        }

        this.ID = newID;
        this.world = DIMENSION.createDimension(this.toString()).asWorld();

    }

    // FBanna - OG, cavern - 0

    public String toStringDebug(MinecraftServer server) {

        String string =
                server.getPlayerManager().getPlayer(creator).getName().getString()
                + " - "
                + game.getName()
                + ", "
                + map.getName();

        if (ID != 0) {
            return string + " - " + ID;
        }
        return string;
    }

    // FBannaOGCavern0

    public String toString() {

        return creator.toString()
        + game.getName()
        + map.getName()
        + ID;
    }

    public Integer getNewID(GameID otherGame) {
        if(
                otherGame.creator == this.creator
                && otherGame.game == this.game
                && otherGame.map  == this.map
        ) {
            return otherGame.ID + 1;
        }
        return 0;
    }

    public ServerWorld getWorld(){
        return this.world;
    }

    public Game getGame(){
        return this.game;
    }

    public GameMap getMap(){
        return this.map;
    }

    public UUID getCreator() {
        return creator;
    }
}
