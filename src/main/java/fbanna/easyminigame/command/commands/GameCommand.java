package fbanna.easyminigame.command.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fbanna.easyminigame.config.GenConfig;
import fbanna.easyminigame.config.GetConfig;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.WinConditions;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.core.appender.rolling.action.IfAll;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fbanna.easyminigame.command.CommandUtil.getGame;

public class GameCommand {

    public static void create(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        String name = StringArgumentType.getString(ctx, "gameName");
        int playerCount = IntegerArgumentType.getInteger(ctx, "playerCount");

        Optional<Game> findGames = Game.getGame(name);
        if(findGames.isPresent()) {
            throw new SimpleCommandExceptionType(Text.literal("Game already existed!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Game already existed!"), false);
            //return;
        }

        Game game = new Game(name, playerCount);


        boolean result = game.create();

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully created game!"), false);
        } else {
            throw new SimpleCommandExceptionType(Text.literal("Could not create game!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not create game!"), false);
        }


    }

    public static void delete(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);

        Optional<Boolean> result = game.delete();

        if(result.isPresent()) {
            if (result.get()) {
                ctx.getSource().sendFeedback(() -> Text.literal("Successfully deleted game!"), false);
            } else {
                throw new SimpleCommandExceptionType(Text.literal("Could not find game!")).create();
                //ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            }
        } else {
            throw new SimpleCommandExceptionType(Text.literal("Could not delete game!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not delete game!"), false);
        }


    }

    public static void list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        Optional<ArrayList<Game>> optionalGames = GetConfig.getGames();

        if(optionalGames.isPresent()) {

            if (!optionalGames.get().isEmpty()){
                ctx.getSource().sendFeedback(() -> Text.literal("Found " + optionalGames.get().size() + " games:" ), false);
                for(Game game: optionalGames.get()) {
                    ctx.getSource().sendFeedback(() -> Text.literal(game.getName()), false);
                }
            } else {
                throw new SimpleCommandExceptionType(Text.literal("No games found!")).create();
            }


        } else {
            throw new SimpleCommandExceptionType(Text.literal("Could not find folder!")).create();

            //ctx.getSource().sendFeedback(() -> Text.literal("Could not find folder!"), false);
        }

    }

    public static void getGameMode(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);

        GameMode gameMode = game.getGameMode();

        ctx.getSource().sendFeedback(() -> Text.literal("gamemode is " + gameMode.toString()), false);
    }

    public static void setGameMode(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);

        GameMode gameMode = GameModeArgumentType.getGameMode(ctx, "gamemode");

        game.setGameMode(gameMode);

        boolean result = GenConfig.makeGameConfig(game);

        if(!result) {
            throw new SimpleCommandExceptionType(Text.literal("Could not save game mode!")).create();
        }

        ctx.getSource().sendFeedback(() -> Text.literal("set game mode!"), false);

    }

    public static void setReload(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        Game game = getGame(ctx);
        boolean reload = BoolArgumentType.getBool(ctx, "boolean");

        game.setReload(reload);

        boolean result = GenConfig.makeGameConfig(game);

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully set reload!"), false);
        } else {
            throw new SimpleCommandExceptionType(Text.literal("Could not save reload!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not save reload!"), false);
        }


    }
    public static void getReload(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);

        ctx.getSource().sendFeedback(() -> Text.literal("reload is set to " + game.getReload()), false);
    }

    public static void setLives(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);
        int lives = IntegerArgumentType.getInteger(ctx, "lives");


        if(lives <= 0) {

            throw new SimpleCommandExceptionType(Text.literal("Enter valid lives!")).create();
        }

        game.setLives(lives);

        boolean result = GenConfig.makeGameConfig(game);

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully set lives!"), false);
        } else {
            throw new SimpleCommandExceptionType(Text.literal("Could not save lives!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not save lives!"), false);
        }
    }

    public static void getLives(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);

        ctx.getSource().sendFeedback(() -> Text.literal("players have " + game.getLives() + " lives"), false);
    }

    public static void addChestReGen(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");

        game.addChestReGen(ticks);

        boolean result = GenConfig.makeGameConfig(game);

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully set re-gen!"), false);
        } else {
            throw new SimpleCommandExceptionType(Text.literal("Could not save re-gen!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not save re-gen!"), false);
        }
    }

    public static void removeChestReGen(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        Game game = getGame(ctx);
        int index = IntegerArgumentType.getInteger(ctx, "index");

        List<Integer> ticks = game.getChestReGen();

        if(index < 0 || index > ticks.size()-1) {
            throw new SimpleCommandExceptionType(Text.literal("Invalid index!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Invalid index!"), false);
            //return;
        }

        game.removeChestReGen(index);

        boolean result = GenConfig.makeGameConfig(game);

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully set re-gen!"), false);
        } else {
            throw new SimpleCommandExceptionType(Text.literal("Could not save re-gen!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not save re-gen!"), false);
        }
    }

    public static void clearChestReGens(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);

        game.clearChestReGen();

        boolean result = GenConfig.makeGameConfig(game);

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully set re-gen!"), false);
        } else {
            throw new SimpleCommandExceptionType(Text.literal("Could not save re-gen!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not save re-gen!"), false);
        }
    }

    public static void listChestReGen(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);

        List<Integer> ticks = game.getChestReGen();

        if(ticks.isEmpty()) {
            throw new SimpleCommandExceptionType(Text.literal("No re-gens found!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("No re-gens found!"), false);
            //return;
        }

        ctx.getSource().sendFeedback(() -> Text.literal("Found " + ticks.size() + " re-gens!"), false);

        for(int tick: ticks) {
            ctx.getSource().sendFeedback(() -> Text.literal("re-gen at " + tick), false);
        }
    }

    /*

    public static void setWinCondition(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");
        WinConditions winCondition = WinConditionArgumentType.getWinCondition(ctx, "condition");

        Optional<Game> optionalGame = Game.getGame(name);

        if(optionalGame.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        optionalGame.get().setWinCondition(winCondition);

        boolean result = GenConfig.makeGameConfig(optionalGame.get());

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully set win condition!"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not save win condition!"), false);
        }
    }

    public static void getWinCondition(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");

        Optional<Game> optionalGame = Game.getGame(name);

        if(optionalGame.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        ctx.getSource().sendFeedback(() -> Text.literal("win condition is set to " + optionalGame.get().getWinCondition().asString()), false);
    }*/
}
