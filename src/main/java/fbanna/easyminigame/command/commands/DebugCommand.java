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
import java.util.Set;

import static fbanna.easyminigame.EasyMiniGame.DIMENSION;

public class DebugCommand {
    public static void enter(CommandContext<ServerCommandSource> ctx) {

        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (!DIMENSION.isMiniGameDimension(ctx.getSource().getWorld()) && player != null) {


            //RegistryKey<World> destKey = wrapRegistryKey(MiniGameDimension.EMG_DIMENSION_KEY.getValue());
            //ServerWorld destination = ctx.getSource().getServer().getWorld(destKey);
            ServerWorld destination = DIMENSION.createDimension("test").asWorld();

            //player.teleport(destination, 0, 300,0, 0,0);
            player.teleport(destination, 0, 300, 0, Set.of(), player.getYaw(), player.getPitch(), true);

        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("You're already in the dimension!"), false);
        }
    }

    public static void exit(CommandContext<ServerCommandSource> ctx) {

        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (DIMENSION.isMiniGameDimension(ctx.getSource().getWorld()) && player != null) {
            //player.getRespawnTarget(true, TeleportTarget.PostDimensionTransition.new)
            player.teleportTo(player.getRespawnTarget(true, Entity::resetPosition));

        } else {

            ctx.getSource().sendFeedback(() -> Text.literal("You're already not in the dimension!"), false);

        }
    }


    private static RegistryKey<World> wrapRegistryKey(Identifier dimID) {
        return RegistryKey.of(RegistryKeys.WORLD, dimID);
    }
}
