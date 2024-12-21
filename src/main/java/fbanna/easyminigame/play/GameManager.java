package fbanna.easyminigame.play;

import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.GameMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GameManager {

    private final MinecraftServer server;

    private List<GameInstance> games = new ArrayList<>();

    public GameManager(MinecraftServer server){
        this.server = server;
        this.games = new ArrayList<>();
    }

    public void createGame(Game game, GameMap map, UUID creator, boolean isDebug) {

        List<GameID> ids = new ArrayList<>();

        for(GameInstance instance: this.games){
            ids.add(instance.getID());
        }

        this.games.add(
                new GameInstance(
                        this.server,
                        new GameID(creator,game,map,ids),
                        isDebug
                )
        );
    }

    public void deleteGame(String ID) {
        for(GameInstance instance: this.games) {
            if(instance.getID().toString().equals(ID)) {
                instance.stop();
                this.games.remove(instance);
                return;
            }
        }
    }

    public Optional<GameInstance> getInstance(String ID) {
        for(GameInstance instance: this.games) {
            if(instance.getID().toString().equals(ID)) {
                return Optional.of(instance);
            }
        }
        return Optional.empty();
    }

    public Optional<ServerPlayerEntity> UUIDtoPlayer(UUID uuid) {
        for (ServerPlayerEntity onlinePlayers : this.server.getPlayerManager().getPlayerList()) {
            if (onlinePlayers.getUuid().equals(uuid)){
                return Optional.of(onlinePlayers);
            }

        }

        return Optional.empty();
    }
}
