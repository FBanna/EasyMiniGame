package fbanna.easyminigame.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fbanna.easyminigame.command.commands.DebugCommand;
import fbanna.easyminigame.command.commands.GameCommand;
import fbanna.easyminigame.command.commands.MapCommand;
import fbanna.easyminigame.command.commands.PlayCommand;
import fbanna.easyminigame.game.Game;
import fbanna.easyminigame.game.WinConditions;
import fbanna.easyminigame.play.PlayStates;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import static fbanna.easyminigame.EasyMiniGame.MANAGER;

public class EasyMiniGameCommand {

    private static final SuggestionProvider<ServerCommandSource> LOOT_SUGGESTION_PROVIDER = (context, builder) -> {
        ReloadableRegistries.Lookup lookup = context.getSource().getServer().getReloadableRegistries();
        return CommandSource.suggestIdentifiers(lookup.getIds(RegistryKeys.LOOT_TABLE), builder);
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {


        final LiteralCommandNode<ServerCommandSource> EasyMiniGameCommand = dispatcher.register(CommandManager.literal("easyminigame")
                .requires(source -> source.hasPermissionLevel(2))

                .then(CommandManager.literal("debug")
                        .then(CommandManager.literal("enter")
                                .executes(ctx -> {
                                    DebugCommand.enter(ctx);
                                    return 1;
                                }))
                        .then(CommandManager.literal("exit")
                                .executes(ctx -> {
                                    DebugCommand.exit(ctx);
                                    return 1;
                                })))

                .then(CommandManager.literal("stop")
                        .executes(ctx -> {
                            if (MANAGER.playState != PlayStates.STOPPED){
                                MANAGER.stop();
                                ctx.getSource().sendFeedback(() -> Text.literal("stopped game!"), false);
                            } else {
                                ctx.getSource().sendFeedback(() -> Text.literal("game not running!"), false);
                            }

                            return 1;
                        }))



                .then(CommandManager.literal("start")
                        .executes(ctx -> {
                            PlayCommand.forceStart(ctx);
                            return 1;
                        }))

                .then(CommandManager.literal("play")
                        .then(CommandManager.argument("gameName", StringArgumentType.string())
                                .executes(ctx -> {
                                    PlayCommand.playGame(ctx);
                                    return 1;
                                })
                                .then(CommandManager.argument("mapName", StringArgumentType.string())
                                        .executes(ctx -> {
                                            PlayCommand.playMap(ctx);
                                            return 1;
                                        }))))


                .then(CommandManager.literal("game")
                        .then(CommandManager.literal("delete")
                                .then(CommandManager.argument("gameName", StringArgumentType.string())
                                        .executes(ctx -> {
                                            GameCommand.delete(ctx);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("create")
                                .then(CommandManager.argument("gameName", StringArgumentType.string())
                                        .then(CommandManager.argument("playerCount", IntegerArgumentType.integer())
                                                .executes(ctx -> {
                                                    GameCommand.create(ctx);
                                                    return 1;
                                                }))))
                        .then(CommandManager.literal("list")
                                .executes(ctx -> {
                                    GameCommand.list(ctx);
                                    return 1;
                                }))

                        .then(CommandManager.argument("gameName", StringArgumentType.string())

                                /*

                                .then(CommandManager.literal("wincondition")
                                        .then(CommandManager.literal("set")
                                                .then(CommandManager.argument("condition", )
                                                        .executes(ctx -> {
                                                            GameCommand.setWinCondition(ctx);
                                                            return 1;
                                                        })))
                                        .then(CommandManager.literal("get")
                                                .executes(ctx -> {
                                                    GameCommand.getWinCondition(ctx);
                                                    return 1;
                                                })))*/

                                .then(CommandManager.literal("lives")
                                        .then(CommandManager.literal("get")
                                                .executes(ctx -> {
                                                    GameCommand.getLives(ctx);
                                                    return 1;
                                                }))
                                        .then(CommandManager.literal("set")
                                                .then(CommandManager.argument("lives", IntegerArgumentType.integer())
                                                        .executes(ctx -> {
                                                            GameCommand.setLives(ctx);
                                                            return 1;
                                                        }))))

                                .then(CommandManager.literal("gamemode")
                                        .then(CommandManager.literal("get")
                                                .executes(ctx -> {
                                                    GameCommand.getGameMode(ctx);
                                                    return 1;
                                                }))
                                        .then(CommandManager.literal("set")
                                                .then(CommandManager.argument("gamemode", GameModeArgumentType.gameMode())
                                                        .executes(ctx -> {
                                                            GameCommand.setGameMode(ctx);
                                                            return 1;
                                                        }))))

                                .then(CommandManager.literal("reload")
                                        .executes(ctx -> {
                                            GameCommand.getReload(ctx);
                                            return 1;
                                        })
                                        .then(CommandManager.argument("boolean", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    GameCommand.setReload(ctx);
                                                    return 1;
                                                })))

                                .then(CommandManager.literal("map")
                                        .then(CommandManager.literal("create")
                                                .then(CommandManager.argument("mapName", StringArgumentType.string())
                                                        .then(CommandManager.argument("teamCount", IntegerArgumentType.integer())
                                                                .executes(ctx -> {
                                                                    MapCommand.create(ctx);
                                                                    return 1;
                                                                }))))
                                        .then(CommandManager.literal("delete")
                                                .then(CommandManager.argument("mapName", StringArgumentType.string())
                                                        .executes(ctx -> {
                                                            MapCommand.delete(ctx);
                                                            return 1;
                                                        })))
                                        .then(CommandManager.literal("list")
                                                .executes(ctx -> {
                                                    MapCommand.list(ctx);
                                                    return 1;
                                                }))
                                        .then(CommandManager.argument("mapName", StringArgumentType.string())
                                                .then(CommandManager.literal("load")
                                                        .executes(ctx -> {
                                                            MapCommand.load(ctx);
                                                            return 1;
                                                        }))
                                                .then(CommandManager.literal("save")
                                                        .executes(ctx -> {
                                                            MapCommand.save(ctx);
                                                            return 1;
                                                        }))
                                                .then(CommandManager.literal("boundaries")
                                                        .then(CommandManager.literal("set")
                                                                .then(CommandManager.argument("corner1", BlockPosArgumentType.blockPos())
                                                                        .then(CommandManager.argument("corner2", BlockPosArgumentType.blockPos())
                                                                                .executes(ctx -> {
                                                                                    MapCommand.setBoundaries(ctx);
                                                                                    return 1;
                                                                                }))
                                                                        .executes(ctx -> {
                                                                            MapCommand.setBoundaryPosition(ctx);
                                                                            return 1;
                                                                        })))
                                                        .then(CommandManager.literal("get")
                                                                .executes(ctx -> {
                                                                    MapCommand.getBoundaries(ctx);
                                                                    return 1;
                                                                })))
                                                .then(CommandManager.literal("chest")
                                                        .then(CommandManager.literal("add")
                                                                .then(CommandManager.argument("loot", RegistryEntryArgumentType.lootTable(commandRegistryAccess))
                                                                        .suggests(LOOT_SUGGESTION_PROVIDER)
                                                                        .then(CommandManager.argument("position", BlockPosArgumentType.blockPos())
                                                                                .executes(ctx -> {
                                                                                    MapCommand.addChestPos(ctx);
                                                                                    return 1;
                                                                                }))))
                                                        .then(CommandManager.literal("auto")
                                                                .then(CommandManager.argument("loot", RegistryEntryArgumentType.lootTable(commandRegistryAccess))
                                                                        .suggests(LOOT_SUGGESTION_PROVIDER)
                                                                        .executes(ctx -> {
                                                                            MapCommand.addAllChests(ctx);
                                                                            return 1;
                                                                        })))
                                                        .then(CommandManager.literal("remove")
                                                                .then(CommandManager.argument("position", BlockPosArgumentType.blockPos())
                                                                        .executes(ctx -> {
                                                                            MapCommand.removeChestPos(ctx);
                                                                            return 1;
                                                                        })))
                                                        .then(CommandManager.literal("list")
                                                                .executes(ctx -> {
                                                                    MapCommand.listChestPos(ctx);
                                                                    return 1;
                                                                })))
                                                .then(CommandManager.literal("spawnPoint")
                                                        .then(CommandManager.literal("set")
                                                                .then(CommandManager.argument("team", IntegerArgumentType.integer())
                                                                        .then(CommandManager.argument("position", BlockPosArgumentType.blockPos())
                                                                                .then(CommandManager.argument("yaw", IntegerArgumentType.integer())
                                                                                        .executes(ctx -> {
                                                                                            MapCommand.setTeamPositionWithYaw(ctx);
                                                                                            return 1;
                                                                                        }))
                                                                                .executes(ctx -> {
                                                                                    MapCommand.setTeamPosition(ctx);
                                                                                    return 1;
                                                                                }))))
                                                        .then(CommandManager.literal("get")
                                                                .then(CommandManager.argument("team", IntegerArgumentType.integer())
                                                                        .executes(ctx -> {
                                                                            MapCommand.getTeamPosition(ctx);
                                                                            return 1;
                                                                        })))))))));

        dispatcher.register(CommandManager.literal("emg").requires(source -> source.hasPermissionLevel(2)).redirect(EasyMiniGameCommand));

        dispatcher.register(CommandManager.literal("joinemg").requires(ServerCommandSource::isExecutedByPlayer)
                .executes(ctx -> {
                    if(MANAGER.playState == PlayStates.WAITING) {
                        if(!MANAGER.ifPlayerIn(ctx.getSource().getPlayer().getUuid())) {
                            MANAGER.addPlayer(ctx.getSource().getPlayer());
                            ctx.getSource().sendFeedback(() -> Text.literal("player added").formatted(Formatting.AQUA), false);
                        } else {
                            ctx.getSource().sendFeedback(() -> Text.literal("Your already in the game!").formatted(Formatting.AQUA), false);
                        }
                    } else {
                        ctx.getSource().sendFeedback(() -> Text.literal("no game running!"), false);
                    }


                    return 1;
                }));
    }


}
