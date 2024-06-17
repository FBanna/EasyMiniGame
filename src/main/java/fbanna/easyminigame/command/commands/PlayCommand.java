package fbanna.easyminigame.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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
import static fbanna.easyminigame.command.CommandUtil.getGame;
import static fbanna.easyminigame.command.CommandUtil.getMap;

public class PlayCommand {

    public static void playGame(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);

        Optional<ArrayList<GameMap>> optionalGameMaps = GetConfig.getMaps(game);

        if(optionalGameMaps.isEmpty()) {
            throw new SimpleCommandExceptionType(Text.literal("Could not find folder!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not find folder!"), false);
        }

        if (optionalGameMaps.get().isEmpty()){
            throw new SimpleCommandExceptionType(Text.literal("No maps found!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("No maps found!" ), false);
        }
            //GameManager manager = new GameManager(ctx.getSource().getServer())

        if(MANAGER.playState != PlayStates.STOPPED) {
            throw new SimpleCommandExceptionType(Text.literal("Game already started or waiting! Please stop existing game")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("game already started or waiting! Please stop last game"), false);
            //return;
        }

        Random rand = new Random();
        ArrayList<GameMap> maps = optionalGameMaps.get();
        GameMap chosenMap = maps.get(rand.nextInt(maps.size()));

        if (game.getPlayers() % chosenMap.getTeams() != 0) {
            ctx.getSource().sendFeedback(() -> Text.literal("Un-even teams! Please set a valid team number for map " + chosenMap.getName()), false);
            return;
        }

        MANAGER.playMap(game, chosenMap);




    }

    public static void playMap(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);
        GameMap map = getMap(ctx);

        if(MANAGER.playState != PlayStates.STOPPED) {
            throw new SimpleCommandExceptionType(Text.literal("Game already started or waiting! Please stop existing game")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("game already started or waiting! Please stop last game"), false);
            //return;
        }

        if (game.getPlayers() % map.getTeams() != 0) {
            throw new SimpleCommandExceptionType(Text.literal("Un-even teams! Please set a valid team number for map " + map.getName())).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Un-even teams! Please set a valid team number for map " + optionalGameMap.get().getName()), false);
            //return;
        }

        MANAGER.playMap(game,map);
    }

    public static void forceStart(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        if(MANAGER.playState != PlayStates.WAITING) {
            throw new SimpleCommandExceptionType(Text.literal("not waiting for players!")).create();

            //ctx.getSource().sendFeedback(() -> Text.literal("not waiting for players!"), false);
            //return;
        }

        if(MANAGER.getPlayerCount() % MANAGER.getTeamPlayerCount() != 0) {
            throw new SimpleCommandExceptionType(Text.literal("Un-even teams! Needs a multiple of " + MANAGER.getTeamCount())).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Un-even teams! Needs a multiple of " + MANAGER.getTeamCount()), false);
            //return;
        }

        MANAGER.startGame();

    }
}
