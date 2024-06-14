package fbanna.easyminigame.command.commands;

import com.mojang.brigadier.context.CommandContext;
import fbanna.easyminigame.dimension.MiniGameDimension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.Optional;

public class DebugCommand {
    public static void enter(CommandContext<ServerCommandSource> ctx) {

        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (ctx.getSource().getWorld().getRegistryKey() != MiniGameDimension.EMG_DIMENSION_KEY && player != null) {

            RegistryKey<World> destKey = wrapRegistryKey(MiniGameDimension.EMG_DIMENSION_KEY.getValue());
            ServerWorld destination = ctx.getSource().getServer().getWorld(destKey);

            player.teleport(destination, 0, 300,0, 0,0);

        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("You're already in the dimension!"), false);
        }
    }

    public static void exit(CommandContext<ServerCommandSource> ctx) {

        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (ctx.getSource().getWorld().getRegistryKey() == MiniGameDimension.EMG_DIMENSION_KEY && player != null) {
            //player.getRespawnTarget(true, TeleportTarget.PostDimensionTransition.new)
            player.teleportTo(player.getRespawnTarget(true, Entity::resetPosition));

            /*

            if (player.getSpawnPointDimension() != null && player.getSpawnPointPosition() != null){

                if (player.getSpawnPointDimension() != MiniGameDimension.EMG_DIMENSION_KEY) {

                    //BlockPos pos = player.getSpawnPointPosition();

                    //ServerWorld destination = ctx.getSource().getServer().getWorld(player.getSpawnPointDimension());
                    //Optional<Vec3d> pos = PlayerEntity.findRespawnPosition(destination, respawnPosition, 0, true, true);

                    //if (pos){
                    //ctx.getSource().getPlayer().teleport(destination, pos.getX(), pos.getY(), pos.getZ(), 0,0);

                    //} else {

                    //    worldRespawn(ctx);

                    //}

                } else {

                    ctx.getSource().sendFeedback(() -> Text.literal("Your respawn is in this dimension!"), false);

                }

            } else {

                worldRespawn(ctx);

            }*/

        } else {

            ctx.getSource().sendFeedback(() -> Text.literal("You're already not in the dimension!"), false);

        }
    }


    private static RegistryKey<World> wrapRegistryKey(Identifier dimID) {
        return RegistryKey.of(RegistryKeys.WORLD, dimID);
    }
}
