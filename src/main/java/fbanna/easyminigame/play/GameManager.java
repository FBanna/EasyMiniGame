package fbanna.easyminigame.play;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import fbanna.easyminigame.config.GenConfig;
import fbanna.easyminigame.config.GetConfig;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.GameMap;
import fbanna.easyminigame.EmgPlayerManagerAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.*;

import static fbanna.easyminigame.EasyMiniGame.LOGGER;

public class GameManager {

    private final MinecraftServer server;

    private final List<GameInstance> games;

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

    public void deleteAllGames() {
        for (GameInstance instance: List.copyOf(this.games)) {
            instance.stop();
            this.games.remove(instance);
        }
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

            NbtCompound nbt = player.writeNbt(new NbtCompound()).copy();
            playerdata.put(player.getUuidAsString(), nbt);
            LOGGER.info(player.getUuidAsString());

        }

        GenConfig.makeSaveStates(playerdata);
    }

    public Optional<NbtCompound> unregisterPlayerWithoutUpdating(ServerPlayerEntity player) {

        if(playerdata.contains(player.getUuidAsString())) {
            Optional<NbtCompound> nbt = Optional.of(playerdata.getCompound(player.getUuidAsString()));
            playerdata.remove(player.getUuidAsString());
            GenConfig.makeSaveStates(playerdata);
            return nbt;
        }

        return Optional.empty();

    }

    public void unregisterPlayers(List<ServerPlayerEntity> players) {

        for (ServerPlayerEntity player: players) {

            if(isOnline(player)) {
                //updatePlayer(player);

                if(playerdata.contains(player.getUuidAsString())) {
                    //((EmgPlayerManagerAccess) this.server.getPlayerManager()).emg$updatePlayer(
                    //    player,
                    //        playerdata.getCompound(player.getUuidAsString()).copy()
                    //);
                    updatePlayer(player, playerdata.getCompound(player.getUuidAsString()).copy());
                }


                playerdata.remove(player.getUuidAsString());
            }

        }

        GenConfig.makeSaveStates(playerdata);
    }

    private void updatePlayer(ServerPlayerEntity player, NbtCompound nbt) {
        player.readNbt(nbt);
        player.readCustomDataFromNbt(nbt);
        //player.setPos();
        //player.readGameModeNbt(nbt);


        if(!nbt.contains("Dimension") || !nbt.contains("playerGameType")) {
            return;
        }

        GameMode gameMode = GameMode.byId(nbt.getInt("playerGameType"));

        DataResult<RegistryKey<World>> result = World.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.get("Dimension")));

        if (!result.isSuccess()) {
            return;
        }

        ServerWorld world = this.server.getWorld(result.getOrThrow());

        player.sendAbilitiesUpdate();

        player.changeGameMode(gameMode);

        player.teleportTo(new TeleportTarget(world, player.getPos(), player.getVelocity(), player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
        //player.updatePositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());


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
