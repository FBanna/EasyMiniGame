package fbanna.easyminigame.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fbanna.easyminigame.EasyMiniGame;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MapCommand {

    public static void create(CommandContext<ServerCommandSource> ctx) {

        String gameName = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");
        int teamCount = IntegerArgumentType.getInteger(ctx, "teamCount");

        Optional<Game> game = Game.getGame(gameName);

        if(!game.isPresent()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        if (game.get().getPlayers() % teamCount != 0) {
            ctx.getSource().sendFeedback(() -> Text.literal("Un-even teams! Please enter valid team number"), false);
            return;
        }

        Optional<GameMap> foundmaps = GameMap.getMap(game.get(), mapName);

        if(foundmaps.isPresent()) {
            ctx.getSource().sendFeedback(() -> Text.literal("Map already existed!"), false);
            return;
        }

        GameMap map = new GameMap(mapName, teamCount);

        boolean result = map.create(game.get());

        if(result) {
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully created map!"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not create map!"), false);
        }
    }

    public static void delete(CommandContext<ServerCommandSource> ctx) {

        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");

        Optional<Game> game = Game.getGame(name);

        if(game.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<GameMap> map = GameMap.getMap(game.get(), mapName);

        if(map.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            return;
        }

        Optional<Boolean> result = map.get().delete(game.get());

        if(result.isPresent()) {
            if (result.get()) {
                ctx.getSource().sendFeedback(() -> Text.literal("Successfully deleted map!"), false);
            } else {
                ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            }
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not delete map!"), false);
        }

    }



    public static void list(CommandContext<ServerCommandSource> ctx) {

        String name = StringArgumentType.getString(ctx, "gameName");

        Optional<Game> game = Game.getGame(name);

        if(game.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<ArrayList<GameMap>> optionalGameMaps = GetConfig.getMaps(game.get());

        if(optionalGameMaps.isPresent()) {

            if (!optionalGameMaps.get().isEmpty()){
                ctx.getSource().sendFeedback(() -> Text.literal("Found " + optionalGameMaps.get().size() + " maps:" ), false);
                for(GameMap maps: optionalGameMaps.get()) {
                    ctx.getSource().sendFeedback(() -> Text.literal(maps.getName()), false);
                }
            } else {
                ctx.getSource().sendFeedback(() -> Text.literal("No maps found!" ), false);
            }


        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find folder!"), false);
        }
    }



    public static void setBoundaries(CommandContext<ServerCommandSource> ctx){
        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");

        BlockPos corner1 = BlockPosArgumentType.getBlockPos(ctx, "corner1");
        BlockPos corner2 = BlockPosArgumentType.getBlockPos(ctx, "corner2");

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

        optionalGameMap.get().getBoundaries().setBoundaries(corner1,corner2);
        GenConfig.makeMapConfig(optionalGame.get(), optionalGameMap.get());

        ctx.getSource().sendFeedback(() -> Text.literal("successfully set boundaries"), false);
    }

    public static void getBoundaries(CommandContext<ServerCommandSource> ctx) {
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

        if(!optionalGameMap.get().getBoundaries().isUsed()) {
            ctx.getSource().sendFeedback(() -> Text.literal("No Dimensions set!"), false);
            return;
        }

        Boundary boundary = optionalGameMap.get().getBoundaries();

        ctx.getSource().sendFeedback(() -> Text.literal("corner 1 = " + boundary.getCorner1() + ", corner 2 = " + boundary.getCorner2()), false);
    }

    public static void setBoundaryPosition( CommandContext<ServerCommandSource> ctx ){
        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");

        BlockPos corner1 = BlockPosArgumentType.getBlockPos(ctx, "corner1");

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

        Boolean result = optionalGameMap.get().setBoundaryPosition(corner1, ctx.getSource().getServer());

        if(result == false ){
            ctx.getSource().sendFeedback(() -> Text.literal("failed to update boundaries!"), false);
        }
        GenConfig.makeMapConfig(optionalGame.get(), optionalGameMap.get());

        ctx.getSource().sendFeedback(() -> Text.literal("successfully set boundary position"), false);
    }






    public static void setTeamPosition(CommandContext<ServerCommandSource> ctx) {

        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");
        int team = IntegerArgumentType.getInteger(ctx, "team");
        BlockPos position = BlockPosArgumentType.getBlockPos(ctx, "position");



        Optional<Game> optionalGame = Game.getGame(name);

        if(!optionalGame.isPresent()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<GameMap> optionalGameMap = GameMap.getMap(optionalGame.get(), mapName);

        if(!optionalGameMap.isPresent()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            return;
        }

        if(team < 0 || team > optionalGameMap.get().getTeams()) {
            ctx.getSource().sendFeedback(() -> Text.literal("Invalid team!"), false);
            return;
        }

        optionalGameMap.get().setSpawnPoint(team, new SpawnPoint(position, Math.round(ctx.getSource().getPlayer().getYaw()/45)*45));


        GenConfig.makeMapConfig(optionalGame.get(), optionalGameMap.get());
        ctx.getSource().sendFeedback(() -> Text.literal("saved team position"), false);
    }

    public static void setTeamPositionWithYaw(CommandContext<ServerCommandSource> ctx) {

        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");
        int team = IntegerArgumentType.getInteger(ctx, "team");
        BlockPos position = BlockPosArgumentType.getBlockPos(ctx, "position");
        int yaw = IntegerArgumentType.getInteger(ctx, "yaw");



        Optional<Game> optionalGame = Game.getGame(name);

        if(!optionalGame.isPresent()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<GameMap> optionalGameMap = GameMap.getMap(optionalGame.get(), mapName);

        if(!optionalGameMap.isPresent()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            return;
        }

        if(team < 0 || team > optionalGameMap.get().getTeams()-1) {
            ctx.getSource().sendFeedback(() -> Text.literal("Invalid team!"), false);
            return;
        }

        optionalGameMap.get().setSpawnPoint(team, new SpawnPoint(position, yaw));


        GenConfig.makeMapConfig(optionalGame.get(), optionalGameMap.get());
        ctx.getSource().sendFeedback(() -> Text.literal("saved team position"), false);
    }

    public static void getTeamPosition(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");
        int team = IntegerArgumentType.getInteger(ctx, "team");



        Optional<Game> optionalGame = Game.getGame(name);

        if(!optionalGame.isPresent()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<GameMap> optionalGameMap = GameMap.getMap(optionalGame.get(), mapName);

        if(!optionalGameMap.isPresent()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            return;
        }

        if(team < 0 || team > optionalGameMap.get().getTeams()) {
            ctx.getSource().sendFeedback(() -> Text.literal("Invalid team!"), false);
            return;
        }

        if(optionalGameMap.get().getSpawnPoint(team) == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("Team position not set!"), false);
            return;
        }


        ctx.getSource().sendFeedback(() -> Text.literal("team " + team + "at: " + optionalGameMap.get().getSpawnPoint(team)), false);
    }

    public static void save(CommandContext<ServerCommandSource> ctx) {

        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");



        Optional<Game> optionalGame = Game.getGame(name);

        if(!optionalGame.isPresent()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<GameMap> optionalGameMap = GameMap.getMap(optionalGame.get(), mapName);

        if(!optionalGameMap.isPresent()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            return;
        }

        if (ctx.getSource().getWorld().getRegistryKey() != MiniGameDimension.EMG_DIMENSION_KEY) {

            ctx.getSource().sendFeedback(() -> Text.literal("You're not in the dimension!"), false);
            return;
        }


        if (!optionalGameMap.get().getBoundaries().isUsed()){
            ctx.getSource().sendFeedback(() -> Text.literal("No boundaries set!"), false);
            return;
        }

        optionalGameMap.get().save(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("saved map!"), false);

    }

    public static void load(CommandContext<ServerCommandSource> ctx) {
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

        if (ctx.getSource().getWorld().getRegistryKey() != MiniGameDimension.EMG_DIMENSION_KEY) {

            ctx.getSource().sendFeedback(() -> Text.literal("You're not in the dimension!"), false);
            return;
        }

        if (!optionalGameMap.get().getBoundaries().isUsed()){
            ctx.getSource().sendFeedback(() -> Text.literal("No boundaries set!"), false);
            return;
        }

        optionalGameMap.get().load(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("loaded map!"), false);

    }

    public static void addChestPos(CommandContext<ServerCommandSource> ctx) {

        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");
        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");
        Optional<RegistryKey<LootTable>> optionalTable;
        try{
            optionalTable = RegistryEntryArgumentType.getLootTable(ctx, "loot").getKey();

            Optional<Game> game = Game.getGame(name);

            if(game.isEmpty()){
                ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
                return;
            }

            Optional<GameMap> map = GameMap.getMap(game.get(), mapName);

            if(map.isEmpty()){
                ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
                return;
            }

            if(optionalTable.isEmpty()) {
                ctx.getSource().sendFeedback(() -> Text.literal("Could not find loot table!"), false);
                return;
            }

            boolean result = map.get().addChestPos(pos, optionalTable.get());

            if(result == false) {
                ctx.getSource().sendFeedback(() -> Text.literal("Already exists, over-writing!"), false);
                return;
            }

            GenConfig.makeMapConfig(game.get(), map.get());

            ctx.getSource().sendFeedback(() -> Text.literal("Successfully added chest position!"), false);

        } catch (Exception e) {
            ctx.getSource().sendFeedback(() -> Text.literal("Error! could not get loot table " + e), false);
        }



    }

    public static void addAllChests(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");

        try{
            Optional<RegistryKey<LootTable>> optionalTable = RegistryEntryArgumentType.getLootTable(ctx, "loot").getKey();

            Optional<Game> game = Game.getGame(name);

            if(game.isEmpty()){
                ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
                return;
            }

            Optional<GameMap> map = GameMap.getMap(game.get(), mapName);

            if(map.isEmpty()){
                ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
                return;
            }

            if(optionalTable.isEmpty()) {
                ctx.getSource().sendFeedback(() -> Text.literal("Could not find loot table!"), false);
                return;
            }

            int result = map.get().addAllChests(ctx.getSource().getServer(), optionalTable.get());

            GenConfig.makeMapConfig(game.get(), map.get());

            ctx.getSource().sendFeedback(() -> Text.literal(String.format("added %d chest positions", result)), false);
        } catch (Exception e ) {
            ctx.getSource().sendFeedback(() -> Text.literal("Error! could not get loot table " + e), false);
        }



    }

    public static void removeChestPos(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");
        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");

        Optional<Game> game = Game.getGame(name);

        if(game.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<GameMap> map = GameMap.getMap(game.get(), mapName);

        if(map.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            return;
        }

        boolean result = map.get().delChestPos(pos);

        if(result == false) {
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find position!"), false);
        } else {
            GenConfig.makeMapConfig(game.get(), map.get());
            ctx.getSource().sendFeedback(() -> Text.literal("Successfully deleted position!"), false);
        }
    }

    public static void listChestPos(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "gameName");
        String mapName = StringArgumentType.getString(ctx, "mapName");

        Optional<Game> game = Game.getGame(name);

        if(game.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find game!"), false);
            return;
        }

        Optional<GameMap> map = GameMap.getMap(game.get(), mapName);

        if(map.isEmpty()){
            ctx.getSource().sendFeedback(() -> Text.literal("Could not find map!"), false);
            return;
        }



        //Optional<ArrayList<GameMap>> optionalGameMaps = GetConfig.getMaps(game.get());
        List<LootChest> chests = map.get().listChestPos();


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
