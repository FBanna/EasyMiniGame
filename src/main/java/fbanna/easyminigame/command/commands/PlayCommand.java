package fbanna.easyminigame.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fbanna.easyminigame.config.GetConfig;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.GameMap;
import fbanna.easyminigame.play.GameManager;
import fbanna.easyminigame.play.PlayStates;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import static fbanna.easyminigame.EasyMiniGame.MANAGER;

public class PlayCommand {

    public static void playGame(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");

        Optional<Game> optionalGame = Game.getGame(name);

        if(optionalGame.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<ArrayList<GameMap>> optionalGameMaps = GetConfig.getMaps(optionalGame.get());

        if(optionalGameMaps.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find folder!"), false);
        }

        if (optionalGameMaps.get().isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("No maps found!" ), false);
        }
            //GameManager manager = new GameManager(ctx.getSource().getServer())

        if(MANAGER.playState != PlayStates.STOPPED) {
            ctx.getSource().sendFeedback(() -> Text.literal("game already started or waiting! Please stop last game"), false);
            return;
        }

        Random rand = new Random();
        ArrayList<GameMap> maps = optionalGameMaps.get();
        GameMap chosenMap = maps.get(rand.nextInt(maps.size()));

        if (optionalGame.get().getPlayers() % chosenMap.getTeams() != 0) {
            ctx.getSource().sendFeedback(() -> Text.literal("Un-even teams! Please set a valid team number for map " + chosenMap.getName()), false);
            return;
        }

        MANAGER.playMap(optionalGame.get(), chosenMap);




    }

    public static void playMap(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");

        Optional<Game> optionalGame = Game.getGame(name);

        if(optionalGame.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<GameMap> optionalGameMap = GameMap.getMap(optionalGame.get(), mapName);

        if(optionalGameMap.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            return;
        }

        if(MANAGER.playState != PlayStates.STOPPED) {
            ctx.getSource().sendFeedback(() -> Text.literal("game already started or waiting! Please stop last game"), false);
            return;
        }

        if (optionalGame.get().getPlayers() % optionalGameMap.get().getTeams() != 0) {
            ctx.getSource().sendFeedback(() -> Text.literal("Un-even teams! Please set a valid team number for map " + optionalGameMap.get().getName()), false);
            return;
        }

        MANAGER.playMap(optionalGame.get(),optionalGameMap.get());
    }

    public static void forceStart(CommandContext<ServerCommandSource> ctx) {

        if(MANAGER.playState != PlayStates.WAITING) {
            ctx.getSource().sendFeedback(() -> Text.literal("not waiting for players!"), false);
            return;
        }

        if(MANAGER.getPlayerCount() % MANAGER.getTeamPlayerCount() != 0) {
            ctx.getSource().sendFeedback(() -> Text.literal("Un-even teams! Needs a multiple of " + MANAGER.getTeamCount()), false);
            return;
        }

        MANAGER.startGame();

    }
}
