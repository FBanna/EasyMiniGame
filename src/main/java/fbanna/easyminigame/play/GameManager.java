package fbanna.easyminigame.play;

import fbanna.easyminigame.EasyMiniGame;
import fbanna.easyminigame.config.DelConfig;
import fbanna.easyminigame.config.GenConfig;
import fbanna.easyminigame.config.GetConfig;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.WinConditions;
import fbanna.easyminigame.game.map.GameMap;
import fbanna.easyminigame.timer.Call;
import fbanna.easyminigame.timer.Timer;
import fbanna.easyminigame.timer.TimerEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import org.apache.http.config.Registry;
import org.joml.Matrix3dStack;

import java.util.*;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

import static fbanna.easyminigame.EasyMiniGame.MANAGER;
import static fbanna.easyminigame.EasyMiniGame.TIMER;
import static fbanna.easyminigame.dimension.MiniGameDimension.EMG_DIMENSION_KEY;

public class GameManager {

    private final MinecraftServer server;
    private Game game;
    private GameMap map;
    public PlayStates playState = PlayStates.STOPPED;
    private List<PlayerState> players = new ArrayList<>();
    private List<UUID> playingPlayers = new ArrayList<>();
    private List<List<UUID>> teams = new ArrayList<List<UUID>>();
    private List<List<Integer>> lives = new ArrayList<>();



    public GameManager(MinecraftServer server) {
        this.server = server;

            Optional<List<PlayerState>> players = GetConfig.getSaveStates();


            if(players.isPresent()) {

                this.players = new ArrayList<>(players.get());
            }

    }

    public void playMap(Game game, GameMap map) {
        this.game = game;
        this.map = map;

        this.teams = new ArrayList<>();
        this.lives = new ArrayList<>();
        //this.players = new ArrayList<>();

        //need to change

        /*
        for(int i = 0; i < this.map.getTeams(); i++) {
            this.lives.add(new ArrayList<>(Collections.nCopies(this.game.getPlayers(), this.game.getLives())));
        }*/


        playState = PlayStates.WAITING;
        sendInvite();
    }

    private void sendInvite() {
        for(ServerPlayerEntity player: this.server.getPlayerManager().getPlayerList()) {
            //player.sendMessage(Text.literal("click to join game!").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/joinemg"))), false);
            player.sendMessage(Text.literal("click to join game!").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/joinemg")).withFormatting(Formatting.YELLOW)), false);
        }
    }

    public void releasePlayers() {

        int i = 0;
        for(List<UUID> team: this.teams) {

            Vec3d pos = this.map.getSpawnPoint(i).pos().toCenterPos().offset(Direction.UP, 0.5);
            int yaw = this.map.getSpawnPoint(i).yaw();

            for(UUID uuid: team) {

                Optional<ServerPlayerEntity> playerEntity = UUIDtoPlayer(uuid);

                if(playerEntity.isPresent()) {

                    ServerPlayerEntity player = playerEntity.get();

                    //player.teleport(this.server.getWorld(EMG_DIMENSION_KEY), pos.getX(), pos.getY(), pos.getZ(), yaw, 0);
                    player.teleport(this.server.getWorld(EMG_DIMENSION_KEY), pos.getX(), pos.getY(), pos.getZ(), Set.of(), yaw, 0, true);

                    player.changeGameMode(this.game.getGameMode());

                }


            }
            i++;
        }


    }

    public void startGame() {

        playState = PlayStates.PLAYING;

        TIMER.register(new TimerEvent(20, new Call() {
            private int loops = 6;
            @Override
            public void call() {
                loops--;

                MANAGER.messagePlayers(Text.translatable(String.valueOf(loops)).formatted(Formatting.RED), true);
                //MANAGER.countdown(loops);
                if(loops != 0) {
                    TIMER.register(new TimerEvent(20, this));
                } else {
                    EasyMiniGame.LOGGER.info("lower gates");
                    MANAGER.releasePlayers();
                }
            }
        }));

        List<Integer> ticks = this.game.getChestReGen();

        if(ticks.size() != 0) {

            TIMER.register(new TimerEvent(ticks.get(0), new Call() {
                List<Integer> regen = new ArrayList<>(ticks); //[20,40,50]
                @Override
                public void call() {
                    regen.removeFirst();
                    if(MANAGER.playState == PlayStates.PLAYING) {
                        map.clearChests(server);
                        map.genChests(server);

                        MANAGER.messagePlayers(Text.literal("Re-generated chests!"), true);

                        if(regen.size() > 0) {
                            TIMER.register(new TimerEvent(regen.getFirst(), this));
                        }

                    }
                }
            }));

        }



        this.map.resetMap(this.server, this.game.getReload());

        Collections.shuffle(this.playingPlayers);

        this.teams = new ArrayList<>(chop(this.playingPlayers));


        for(UUID uuid: new ArrayList<>(this.playingPlayers)){
            Optional<ServerPlayerEntity> player = UUIDtoPlayer(uuid);
            if(player.isPresent()) {
                player.get().sendMessage(Text.literal("game is starting"), true);
                this.players.add(new PlayerState(player.get()));
            }
        }

        this.playingPlayers = new ArrayList<>();


        try {
            GenConfig.makeSaveStates(this.players);
        } catch (Exception e ){
            EasyMiniGame.LOGGER.info(String.valueOf("b| " + e));
        }


        int i = 0;
        for(List<UUID> team: this.teams) {
            Vec3d pos = this.map.getSpawnPoint(i).pos().toCenterPos().offset(Direction.UP, 0.5);
            int yaw = this.map.getSpawnPoint(i).yaw();

            this.lives.add(new ArrayList<>());
            for(UUID uuid: team) {

                Optional<ServerPlayerEntity> playerEntity = UUIDtoPlayer(uuid);

                if(playerEntity.isPresent()) {

                    this.lives.get(i).add(this.game.getLives());

                    ServerPlayerEntity player = playerEntity.get();

                    player.sendMessage(Text.literal("your on team " + i).formatted(Formatting.AQUA));

                    //player.teleport(this.server.getWorld(EMG_DIMENSION_KEY), pos.getX(), pos.getY(), pos.getZ(), yaw, 0);
                    player.teleport(this.server.getWorld(EMG_DIMENSION_KEY),  pos.getX(), pos.getY(), pos.getZ(), Set.of(), yaw, 0, true);
                    player.getInventory().clear();
                    //player.changeGameMode(this.game.getGameMode());
                    player.changeGameMode(GameMode.SPECTATOR);
                    player.setHealth(20);
                    player.getHungerManager().setSaturationLevel(5);
                    player.getHungerManager().setFoodLevel(20);
                    player.fallDistance = 0;
                    player.experienceProgress = 0;
                    player.extinguish();
                    player.setExperienceLevel(0);
                    player.clearStatusEffects();

                }


            }
            i++;
        }

    }

    public boolean ifPlayerIn(UUID player) {
        for(UUID uuid: this.playingPlayers) {
            if(uuid.equals(player)) {
                return true;
            }
        }
        return false;
    }

    public void addPlayer(ServerPlayerEntity player) {


        if(this.playingPlayers.size() < this.game.getPlayers()) {

            this.playingPlayers.add(player.getUuid());

            if(this.playingPlayers.size() == this.game.getPlayers()) {
                startGame();
            }
        } else {
            player.sendMessage(Text.literal("sorry game is full").formatted(Formatting.AQUA));
        }
    }

    public void stop() {

        TIMER.clear();

        if(this.map != null) {
            this.map.killItems(server);
        }

        List<PlayerState> temp = new ArrayList<>();

        for(int i = 0; i < this.players.size(); i++){
            Optional<ServerPlayerEntity> player = UUIDtoPlayer(this.players.get(i).getUuid());

            if(player.isPresent()) {
                this.players.get(i).updatePlayer(player.get(), this.server);
            } else {
                temp.add(this.players.get(i));
            }
        }

        this.players.clear();
        if(!temp.isEmpty()) {

            this.players = new ArrayList<>(temp);
            GenConfig.makeSaveStates(this.players);

        } else {
            DelConfig.deleteSaveStates();

        }

        this.game = null;
        this.map = null;
        this.teams.clear(); // TEMP
        this.playingPlayers.clear();
        this.playState = PlayStates.STOPPED;
    }

    private  List<List<UUID>> chop(List<UUID> list) {
        int length = this.game.getPlayers()/this.map.getTeams();

        List<List<UUID>> tempTeams = new ArrayList<List<UUID>>();

        final int N = list.size();
        for (int i = 0; i < N; i += length) {
            tempTeams.add(list.subList(i, Math.min(N, i+length)));
        }

        return tempTeams;

    }

    public Optional<ServerPlayerEntity> UUIDtoPlayer(UUID uuid) {
        for (ServerPlayerEntity onlinePlayers : this.server.getPlayerManager().getPlayerList()) {
            if (onlinePlayers.getUuid().equals(uuid)){
                return Optional.of(onlinePlayers);
            }

        }

        return Optional.empty();
    }

    public void isNeeded(ServerPlayerEntity player) {

        if(this.players.size() != 0 && playState != PlayStates.PLAYING) {
            for(int i = 0; i < this.players.size(); i++) {

                if(player.getUuid().equals(this.players.get(i).getUuid())){


                    try {

                        this.players.get(i).updatePlayer(player, this.server);
                        this.players.remove(i);

                    } catch (Exception e) {
                        EasyMiniGame.LOGGER.info(String.valueOf("1"+e));
                    }

                }

            }
            try{
                if(this.players.size() > 0) {
                    GenConfig.makeSaveStates(this.players);

                } else {
                    DelConfig.deleteSaveStates();

                }
            } catch (Exception e) {
                EasyMiniGame.LOGGER.info("2" + e);
            }
        }

    }

    public boolean playDeath(UUID uuid) {

        try{

            if(this.game == null) {
                return true;
            } else if(this.game.getWinCondition() == WinConditions.LAST_TEAM) {
                for( int i = 0; i < this.teams.size(); i++ ) {
                    for( int j = 0; j < this.teams.get(i).size(); j++ ) {

                        if(this.teams.get(i).get(j).equals(uuid)) {

                            this.lives.get(i).set(j, this.lives.get(i).get(j) - 1);

                            if(this.lives.get(i).get(j) <= 0) {

                                int winning = teamAlive();

                                if(winning != -1) {

                                    String winningPlayers = "";

                                    int k = 0;

                                    for(UUID player: this.teams.get(winning)) {
                                        Optional<ServerPlayerEntity> optionalPlayer = UUIDtoPlayer(player);

                                        if(optionalPlayer.isPresent()) {
                                            if(k == this.teams.size()-1) {
                                                winningPlayers = "%s %s".formatted(winningPlayers,optionalPlayer.get().getName().toString());
                                            } else {
                                                winningPlayers = "%s %s,".formatted(winningPlayers, optionalPlayer.get().getName().toString());
                                            }
                                        }

                                    }


                                    this.messagePlayers( Text.of("Team %s wins, %s".formatted(winning, winningPlayers) ).copy().formatted(Formatting.AQUA), false);
                                    this.stop();

                                    //code here for party game set up
                                    return false;
                                }

                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            EasyMiniGame.LOGGER.info(String.valueOf("a| " + e));
        }
        return false;

    }

    public int teamAlive() {
        int winning = -1;
        for (int i = 0; i < this.teams.size(); i++) {
            if(teamIsAlive(i)) {
                if(winning == -1) {
                    winning = i;
                } else {
                    return -1;
                }
            }
        }
        return winning;
    }

    public boolean teamIsAlive(int i) {
        for(int j: this.lives.get(i)) {
            EasyMiniGame.LOGGER.info("player has " +j);
            if(j > 0) {
                return true;
            }
        }
        return false;
    }

    public void messagePlayers(Text message, boolean overlay) {
        for( int i = 0; i < this.teams.size(); i++ ) {
            for( int j = 0; j < this.teams.get(i).size(); j++ ) {

                Optional<ServerPlayerEntity> player = UUIDtoPlayer(this.teams.get(i).get(j));

                if(player.isPresent()) {
                    player.get().sendMessage(message, overlay);
                }

            }
        }
    }

    public void respawnPlayer(UUID uuid) {
        for( int i = 0; i < this.teams.size(); i++ ) {
            for( int j = 0; j < this.teams.get(i).size(); j++ ) {

                if(this.teams.get(i).get(j).equals(uuid)) {

                    Optional<ServerPlayerEntity> optional = UUIDtoPlayer(uuid);

                    if(optional.isPresent()) {

                        Vec3d pos = this.map.getSpawnPoint(i).pos().toCenterPos().offset(Direction.UP, 0.5);
                        int yaw = this.map.getSpawnPoint(i).yaw();
                        //player.get().teleport(this.server.getWorld(EMG_DIMENSION_KEY), pos.getX(), pos.getY(), pos.getZ(), 0,0);
                        ServerPlayerEntity player = optional.get();
                        //player.teleport(this.server.getWorld(EMG_DIMENSION_KEY), pos.getX(), pos.getY(), pos.getZ(), yaw, 0);
                        player.teleport(this.server.getWorld(EMG_DIMENSION_KEY),  pos.getX(), pos.getY(), pos.getZ(), Set.of(), yaw, 0, true);

                        player.getHungerManager().setSaturationLevel(5);
                        player.getHungerManager().setFoodLevel(20);
                        player.fallDistance = 0;
                        player.experienceProgress = 0;
                        player.extinguish();
                        player.setExperienceLevel(0);
                        player.clearStatusEffects();
                    }
                }
            }
        }
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getTeamCount() {
        return this.map.getTeams();
    }

    public int getTeamPlayerCount() {
        return this.game.getPlayers()/this.map.getTeams();
    }

}
