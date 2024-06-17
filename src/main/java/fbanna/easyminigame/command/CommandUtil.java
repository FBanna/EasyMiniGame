package fbanna.easyminigame.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.GameMap;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.objectweb.asm.TypeReference;

import java.util.Optional;

public class CommandUtil {

    public static Game getGame(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "gameName");


        Optional<Game> game = Game.getGame(name);

        if(game.isPresent()) {
            return game.get();
        } else {
            throw new SimpleCommandExceptionType(Text.of("Could not find game!")).create();
        }
    }

    public static GameMap getMap(CommandContext<ServerCommandSource> ctx, Game game) throws CommandSyntaxException {
        String mapName = StringArgumentType.getString(ctx, "mapName");

        Optional<GameMap> map = GameMap.getMap(game, mapName);

        if(map.isEmpty()){
            throw new SimpleCommandExceptionType(Text.of("Could not find map!")).create();
            //throw new RuntimeException("Could not find map!", new SimpleCommandExceptionType(new LiteralMessage("Could not find map!")).create());
            //throw (;
            //throw new RuntimeException(new Throwable("Could not find map!"));
            //throw new RuntimeException("Could not find map!");
        }

        return map.get();
    }

    public static GameMap getMap(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");

        Optional<Game> game = Game.getGame(name);

        if(game.isEmpty()){
            throw new RuntimeException("Could not find game!");
        }

        Optional<GameMap> map = GameMap.getMap(game.get(), mapName);

        if(map.isEmpty()){
            //throw new ArrayIndexOutOfBoundsException();

            //throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Could not find map!");
            throw new SimpleCommandExceptionType(Text.of("Could not find map!")).create();
            //throw new RuntimeException("Could not find map!", new SimpleCommandExceptionType(new LiteralMessage("Could not find map!")).create());
            //throw new RuntimeException(new Throwable("Could not find map!"));
            //throw new RuntimeException("Could not find map!");
        }

        return map.get();
    }
}
