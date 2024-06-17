package fbanna.easyminigame.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fbanna.easyminigame.EasyMiniGame;
import fbanna.easyminigame.command.CommandUtil;
import fbanna.easyminigame.config.GenConfig;
import fbanna.easyminigame.config.GetConfig;
import fbanna.easyminigame.dimension.MiniGameDimension;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.map.Boundary;
import fbanna.easyminigame.game.map.GameMap;
import fbanna.easyminigame.game.map.LootChest;
import fbanna.easyminigame.game.map.SpawnPoint;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.http.config.Registry;

import static fbanna.easyminigame.command.CommandUtil.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MapCommand {

    public static void create(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        Game game = getGame(ctx);
        String mapName = StringArgumentType.getString(ctx, "mapName");
        int teamCount = IntegerArgumentType.getInteger(ctx, "teamCount");


        if (game.getPlayers() % teamCount != 0) {
            throw new SimpleCommandExceptionType(Text.of("Un-even teams! Please enter valid team number!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Un-even teams! Please enter valid team number"), false);
            //return;
        }

        Optional<GameMap> foundmaps = GameMap.getMap(game, mapName);

        if(foundmaps.isPresent()) {
            throw new SimpleCommandExceptionType(Text.of("Map already exists!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Map already existed!"), false);
            //return;
        }

        GameMap map = new GameMap(mapName, teamCount);

        boolean result = map.create(game);

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully created map!"), false);
        } else {
            throw new SimpleCommandExceptionType(Text.of("Could not create map!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not create map!"), false);
        }
    }

    public static void delete(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        Game game = getGame(ctx);
        GameMap map = getMap(ctx, game);

        Optional<Boolean> result = map.delete(game);

        if(result.isPresent()) {
            if (result.get()) {
                ctx.getSource().sendFeedback(() -> Text.literal("Successfully deleted map!"), false);
            } else {
                throw new SimpleCommandExceptionType(Text.of("Could not find map!")).create();
                //ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            }
        } else {
            throw new SimpleCommandExceptionType(Text.of("Could not delete map!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not delete map!"), false);
        }

    }



    public static void list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        Game game = getGame(ctx);

        Optional<ArrayList<GameMap>> optionalGameMaps = GetConfig.getMaps(game);

        if(optionalGameMaps.isPresent()) {

            if (!optionalGameMaps.get().isEmpty()){
                ctx.getSource().sendFeedback(() -> Text.literal("Found " + optionalGameMaps.get().size() + " maps:" ), false);
                for(GameMap maps: optionalGameMaps.get()) {
                    ctx.getSource().sendFeedback(() -> Text.literal(maps.getName()), false);
                }
            } else {
                throw new SimpleCommandExceptionType(Text.of("No maps found!")).create();
                //ctx.getSource().sendFeedback(() -> Text.literal("No maps found!" ), false);
            }


        } else {
            throw new SimpleCommandExceptionType(Text.of("Could not find folder!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not find folder!"), false);
        }
    }



    public static void setBoundaries(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);
        GameMap map = getMap(ctx, game);

        BlockPos corner1 = BlockPosArgumentType.getBlockPos(ctx, "corner1");
        BlockPos corner2 = BlockPosArgumentType.getBlockPos(ctx, "corner2");

        map.getBoundaries().setBoundaries(corner1,corner2);
        boolean result = GenConfig.makeMapConfig(game, map);

        if(!result) {
            throw new SimpleCommandExceptionType(Text.of("Failed to set boundaries!")).create();
        }

        ctx.getSource().sendFeedback(() -> Text.literal("successfully set boundaries!"), false);
    }

    public static void getBoundaries(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        //Game game = util.getGame(ctx);
        GameMap map = getMap(ctx);


        Boundary boundary = map.getBoundaries();

        ctx.getSource().sendFeedback(() -> Text.literal("corner 1 = " + boundary.getCorner1() + ", corner 2 = " + boundary.getCorner2()), false);
    }

    public static void setBoundaryPosition( CommandContext<ServerCommandSource> ctx ) throws CommandSyntaxException {

        Game game = getGame(ctx);
        GameMap map = getMap(ctx, game);
        BlockPos corner1 = BlockPosArgumentType.getBlockPos(ctx, "corner1");


        Boolean result = map.setBoundaryPosition(corner1, ctx.getSource().getServer());

        if(!result){
            throw new SimpleCommandExceptionType(Text.of("Failed to set boundaries!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("failed to update boundaries!"), false);
        }
        GenConfig.makeMapConfig(game, map);

        ctx.getSource().sendFeedback(() -> Text.literal("successfully set boundary position"), false);
    }






    public static void setTeamPosition(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        Game game = getGame(ctx);
        GameMap map = getMap(ctx, game);

        int team = IntegerArgumentType.getInteger(ctx, "team");
        BlockPos position = BlockPosArgumentType.getBlockPos(ctx, "position");


        if(team < 0 || team > map.getTeams()-1) {
            throw new SimpleCommandExceptionType(Text.of("Invalid team!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Invalid team!"), false);
            //return;
        }

        map.setSpawnPoint(team, new SpawnPoint(position, Math.round(ctx.getSource().getPlayer().getYaw()/45)*45));


        GenConfig.makeMapConfig(game, map);
        ctx.getSource().sendFeedback(() -> Text.literal("saved team position"), false);
    }

    public static void setTeamPositionWithYaw(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        Game game = getGame(ctx);
        GameMap map = getMap(ctx, game);

        int team = IntegerArgumentType.getInteger(ctx, "team");
        BlockPos position = BlockPosArgumentType.getBlockPos(ctx, "position");
        int yaw = IntegerArgumentType.getInteger(ctx, "yaw");



        if(team < 0 || team > map.getTeams()-1) {
            throw new SimpleCommandExceptionType(Text.of("Invalid team!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Invalid team!"), false);
            //return;
        }

        map.setSpawnPoint(team, new SpawnPoint(position, yaw));


        GenConfig.makeMapConfig(game, map);
        ctx.getSource().sendFeedback(() -> Text.literal("saved team position"), false);
    }

    public static void getTeamPosition(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        GameMap map = getMap(ctx);
        //String name = StringArgumentType.getString(ctx, "gameName");
        //String mapName = StringArgumentType.getString(ctx, "mapName");
        int team = IntegerArgumentType.getInteger(ctx, "team");

        if(team < 0 || team > map.getTeams()-1) {
            throw new SimpleCommandExceptionType(Text.of("Invalid team!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Invalid team!"), false);
            //return;
        }

        if(map.getSpawnPoint(team) == null) {
            throw new SimpleCommandExceptionType(Text.of("Team position not set!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Team position not set!"), false);
            //return;
        }


        ctx.getSource().sendFeedback(() -> Text.literal("team " + team + "at: " + map.getSpawnPoint(team)), false);
    }

    public static void save(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        GameMap map = getMap(ctx);


        if (ctx.getSource().getWorld().getRegistryKey() != MiniGameDimension.EMG_DIMENSION_KEY) {
            throw new SimpleCommandExceptionType(Text.of("You're not in the dimension!")).create();

            //ctx.getSource().sendFeedback(() -> Text.literal("You're not in the dimension!"), false);
            //return;
        }

        /*


        if (!map.getBoundaries().isUsed()){
            throw new
            ctx.getSource().sendFeedback(() -> Text.literal("No boundaries set!"), false);
            return;
        }*/

        map.save(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("saved map!"), false);

    }

    public static void load(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        GameMap map = getMap(ctx);

        if (ctx.getSource().getWorld().getRegistryKey() != MiniGameDimension.EMG_DIMENSION_KEY) {
            throw new SimpleCommandExceptionType(Text.of("You're not in the dimension!")).create();

            //ctx.getSource().sendFeedback(() -> Text.literal("You're not in the dimension!"), false);
            //return;
        }
        /*

        if (!optionalGameMap.get().getBoundaries().isUsed()){
            ctx.getSource().sendFeedback(() -> Text.literal("No boundaries set!"), false);
            return;
        }*/

        map.load(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("loaded map!"), false);

    }

    public static void addChestPos(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Game game = getGame(ctx);
        GameMap map = getMap(ctx, game);
        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");
        Optional<RegistryKey<LootTable>> optionalTable = RegistryEntryArgumentType.getLootTable(ctx, "loot").getKey();


        if(optionalTable.isEmpty()) {
            throw new SimpleCommandExceptionType(Text.of("Could not find loot table!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not find loot table!"), false);
            //return;
        }

        boolean result = map.addChestPos(pos, optionalTable.get());

        if(result == false) {
            ctx.getSource().sendFeedback(() -> Text.literal("Already exists, over-writing!"), false);
            return;
        }

        GenConfig.makeMapConfig(game, map);

        ctx.getSource().sendFeedback(() -> Text.literal("Successfully added chest position!"), false);



    }

    public static void addAllChests(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {

        Game game = getGame(ctx);
        GameMap map = getMap(ctx, game);
        Optional<RegistryKey<LootTable>> optionalTable = RegistryEntryArgumentType.getLootTable(ctx, "loot").getKey();


        if(optionalTable.isEmpty()) {
            throw new SimpleCommandExceptionType(Text.of("Could not find loot table!")).create();
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not find loot table!"), false);
            //return;
        }

        int result = map.addAllChests(ctx.getSource().getServer(), optionalTable.get());

        GenConfig.makeMapConfig(game, map);

        ctx.getSource().sendFeedback(() -> Text.literal(String.format("added %d chest positions", result)), false);

    }

    public static void removeChestPos(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        //String name = StringArgumentType.getString(ctx, "gameName");
        //String mapName = StringArgumentType.getString(ctx, "mapName");
        Game game = getGame(ctx);
        GameMap map = getMap(ctx, game);
        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");

        boolean result = map.delChestPos(pos);

        if(result == false) {
            //ctx.getSource().sendFeedback(() -> Text.literal("Could not find position!"), false);
            throw new SimpleCommandExceptionType(Text.of("Could not find position!")).create();
        }

        GenConfig.makeMapConfig(game, map);
        ctx.getSource().sendFeedback(() -> Text.literal("Successfully deleted position!"), false);

    }

    public static void listChestPos(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        GameMap map = getMap(ctx);

        //Optional<ArrayList<GameMap>> optionalGameMaps = GetConfig.getMaps(game.get());
        List<LootChest> chests = map.listChestPos();


        if (!chests.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Found " + chests.size() + " chests:" ), false);
            for(LootChest chest: chests) {
                ctx.getSource().sendFeedback(() -> Text.literal("chest at " + chest.pos() + " with loot " + chest.lootTable().getPath()), false);
            }
        } else {

            ctx.getSource().sendFeedback(() -> Text.literal("No chests found!" ), false);
        }



    }

}
