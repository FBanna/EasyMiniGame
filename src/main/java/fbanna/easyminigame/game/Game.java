package fbanna.easyminigame.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fbanna.easyminigame.EasyMiniGame;
import fbanna.easyminigame.config.DelConfig;
import fbanna.easyminigame.config.GenConfig;
import fbanna.easyminigame.config.GetConfig;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class Game {

    private final String name;
    private final int players;
    private GameMode gameMode = GameMode.SURVIVAL;
    private boolean reload = false;
    private WinConditions winCondition = WinConditions.LAST_TEAM;
    private int lives;

    public static final Codec<Game> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(Game::getName),
            Codec.INT.fieldOf("players").forGetter(Game::getPlayers),
            GameMode.CODEC.fieldOf("gamemode").forGetter(Game::getGameMode),
            Codec.BOOL.fieldOf("reload").forGetter(Game::getReload),
            WinConditions.CODEC.fieldOf("winCondition").forGetter(Game::getWinCondition),
            Codec.INT.fieldOf("lives").forGetter(Game::getLives)
    ).apply(instance, Game::new));

    public Game(String name, int players) {
        this.name = name;
        this.players = players;
        this.lives = 1;
    }

    public Game(String name, int players, GameMode gameMode, boolean reload, WinConditions winCondition, int lives) {
        this.name = name;
        this.players = players;
        this.gameMode = gameMode;
        this.reload = reload;
        this.winCondition = winCondition;
        this.lives = lives;
    }

    public static Optional<Game> getGame(String name) {
        Optional<ArrayList<Game>> games = GetConfig.getGames();

        if(games.isPresent()){
            for(Game game: games.get()) {
                if(Objects.equals(game.getName(), name)) {
                    return Optional.of(game);
                }
            }
        }
        return Optional.empty();
    }

    public boolean create(){
        return GenConfig.makeGame(this);
    }

    public Optional<Boolean> delete() {
        return DelConfig.deleteGame(this);
    }

    public String getName() {
        return this.name;
    }

    public int getPlayers() {
        return this.players;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public boolean getReload() {
        return this.reload;
    }

    public void setReload(boolean reload){
        this.reload = reload;
    }

    public WinConditions getWinCondition() {
        return this.winCondition;
    }

    public void setWinCondition(WinConditions winCondition) {
        this.winCondition = winCondition;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }
}
