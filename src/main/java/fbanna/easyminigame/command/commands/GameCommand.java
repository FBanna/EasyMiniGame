package fbanna.easyminigame.command.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fbanna.easyminigame.config.GenConfig;
import fbanna.easyminigame.config.GetConfig;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.WinConditions;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.core.appender.rolling.action.IfAll;

import java.util.ArrayList;
import java.util.Optional;

public class GameCommand {

    public static void create(CommandContext<ServerCommandSource> ctx) {

        String name = StringArgumentType.getString(ctx, "gameName");
        int playerCount = IntegerArgumentType.getInteger(ctx, "playerCount");

        Optional<Game> findGames = Game.getGame(name);
        if(findGames.isPresent()) {
            ctx.getSource().sendFeedback(() -> Text.literal("Game already existed!"), false);
            return;
        }

        Game game = new Game(name, playerCount);


        boolean result = game.create();

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully created game!"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not create game!"), false);
        }


    }

    public static void delete(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");


        Optional<Game> game = Game.getGame(name);

        if(game.isPresent()) {

            Optional<Boolean> result = game.get().delete();

            if(result.isPresent()) {
                if (result.get()) {
                    ctx.getSource().sendFeedback(() -> Text.literal("Successfully deleted game!"), false);
                } else {
                    ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
                }
            } else {
                ctx.getSource().sendFeedback(() -> Text.literal("Could not delete game!"), false);
            }
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
        }

    }

    public static void list(CommandContext<ServerCommandSource> ctx) {

        Optional<ArrayList<Game>> optionalGames = GetConfig.getGames();

        if(optionalGames.isPresent()) {

            if (!optionalGames.get().isEmpty()){
                ctx.getSource().sendFeedback(() -> Text.literal("Found " + optionalGames.get().size() + " games:" ), false);
                for(Game game: optionalGames.get()) {
                    ctx.getSource().sendFeedback(() -> Text.literal(game.getName()), false);
                }
            } else {
                ctx.getSource().sendFeedback(() -> Text.literal("No games found!" ), false);
            }


        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find folder!"), false);
        }

    }

    public static void getGameMode(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");

        Optional<Game> optionalGame = Game.getGame(name);

        if(optionalGame.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        GameMode gameMode = optionalGame.get().getGameMode();

        ctx.getSource().sendFeedback(() -> Text.literal("gamemode is " + gameMode.toString()), false);
    }

    public static void setGameMode(CommandContext<ServerCommandSource> ctx) {
        try {
            String name = StringArgumentType.getString(ctx, "gameName");

            Optional<Game> optionalGame = Game.getGame(name);

            if(optionalGame.isEmpty()){
                ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
                return;
            }

            GameMode gameMode = GameModeArgumentType.getGameMode(ctx, "gamemode");

            optionalGame.get().setGameMode(gameMode);

            GenConfig.makeGameConfig(optionalGame.get());

            ctx.getSource().sendFeedback(() -> Text.literal("set game mode!"), false);


        } catch (Exception e) {
            ctx.getSource().sendFeedback(() -> Text.literal("invalid gamemode"), false);
        }

    }

    public static void setReload(CommandContext<ServerCommandSource> ctx) {

        String name = StringArgumentType.getString(ctx, "gameName");
        boolean reload = BoolArgumentType.getBool(ctx, "boolean");

        Optional<Game> optionalGame = Game.getGame(name);

        if(optionalGame.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        optionalGame.get().setReload(reload);

        boolean result = GenConfig.makeGameConfig(optionalGame.get());

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully set reload!"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not save reload!"), false);
        }


    }
    public static void getReload(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");

        Optional<Game> optionalGame = Game.getGame(name);

        if(optionalGame.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        ctx.getSource().sendFeedback(() -> Text.literal("reload is set to " + optionalGame.get().getReload()), false);


    }

    public static void setLives(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");
        int lives = IntegerArgumentType.getInteger(ctx, "lives");

        Optional<Game> optionalGame = Game.getGame(name);

        if(optionalGame.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        if(lives <= 0) {
            ctx.getSource().sendFeedback(() -> Text.literal("Enter valid lives!"), false);
            return;
        }

        optionalGame.get().setLives(lives);

        boolean result = GenConfig.makeGameConfig(optionalGame.get());

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully set lives!"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not save lives!"), false);
        }
    }

    public static void getLives(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");

        Optional<Game> optionalGame = Game.getGame(name);

        if(optionalGame.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        ctx.getSource().sendFeedback(() -> Text.literal("players have " + optionalGame.get().getLives() + " lives"), false);
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
