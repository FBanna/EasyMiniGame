package fbanna.easyminigame.play;

import fbanna.easyminigame.config.GenConfig;
import fbanna.easyminigame.config.GetConfig;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.GameMap;
import fbanna.easyminigame.EmgPlayerManagerAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

import static fbanna.easyminigame.EasyMiniGame.LOGGER;

public class GameManager {

    private final MinecraftServer server;

    private List<GameInstance> games;

    private NbtCompound playerdata;

    public GameManager(MinecraftServer server){
        this.server = server;
        this.games = new ArrayList<>();
        this.playerdata = GetConfig.getSaveStates();
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

    public void deleteGame(GameInstance instance) {
        instance.stop();
        this.games.remove(instance);
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

    public void registerPlayers(List<ServerPlayerEntity> players) {


        for (ServerPlayerEntity player: players){

            NbtCompound nbt = player.writeNbt(new NbtCompound());
            playerdata.put(player.getUuidAsString(), nbt);
            LOGGER.info(player.getUuidAsString());

        }

        GenConfig.makeSaveStates(playerdata);
    }

    public void unregisterPlayers(List<ServerPlayerEntity> players) {
        LOGGER.info("I GOT CALLWE, " + players);
        for (ServerPlayerEntity player: players) {

            if(isOnline(player)) {
                LOGGER.info("IM ONLINE!! YAYA");
                //updatePlayer(player);

                if(playerdata.contains(player.getUuidAsString())) {
                    ((EmgPlayerManagerAccess) this.server.getPlayerManager()).emg$updatePlayer(
                            player,
                            playerdata.getCompound(player.getUuidAsString()).copy()
                    );
                }


                playerdata.remove(player.getUuidAsString());
            }

        }

        GenConfig.makeSaveStates(playerdata);
    }


    private boolean isOnline(ServerPlayerEntity player) {

        for (ServerPlayerEntity onlinePlayers : this.server.getPlayerManager().getPlayerList()) {
            if (onlinePlayers == player){
                return true;
            }

        }
        return false;
    }
}
