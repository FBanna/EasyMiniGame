package fbanna.easyminigame.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import fbanna.easyminigame.EmgPlayerManagerAccess;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin({PlayerManager.class})
public abstract class MixinUpdatePlayer implements EmgPlayerManagerAccess {

    @Shadow
    private final List<ServerPlayerEntity> players = Lists.newArrayList();

    @Shadow
    private final Map<UUID, ServerPlayerEntity> playerMap = Maps.newHashMap();

    @Shadow
    public abstract void sendStatusEffects(ServerPlayerEntity serverPlayerEntity);

    @Shadow
    public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);

    @Shadow
    public abstract void sendCommandTree(ServerPlayerEntity player);

    @Shadow @Final
    private MinecraftServer server;


    @Shadow @Final private static Logger LOGGER;

    @Override
    public void emg$updatePlayer(ServerPlayerEntity oldPlayer, NbtCompound nbt) {

        LOGGER.info("here?");
        this.players.remove(oldPlayer);
        oldPlayer.getServerWorld().removePlayer(oldPlayer, Entity.RemovalReason.DISCARDED);

        //PlayerManager manager = this.server.getPlayerManager();

        //manager.remov


        //player.readNbt(nbt);


        if(!nbt.contains("Dimension")) {
            return;
        }

        DataResult<RegistryKey<World>> result = World.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.get("Dimension")));

        if (!result.isSuccess()) {
            return;
        }

        ServerWorld world = this.server.getWorld(result.getOrThrow());



        if(world == null) {
            LOGGER.info("NOT GOOD");
            return;
        } else {
            LOGGER.info(world.getRegistryKey().getValue().toString());
        }

        ServerPlayerEntity player = new ServerPlayerEntity(this.server, world, oldPlayer.getGameProfile(), oldPlayer.getClientOptions());
        player.networkHandler = oldPlayer.networkHandler;
        player.setId(player.getId());
        player.readGameModeNbt(nbt);
        player.readNbt(nbt);


        player.setServerWorld(world);
        LOGGER.info("here?");



        LOGGER.info("here?");








        player.refreshPositionAndAngles(
                player.getX(),player.getY(), player.getZ(),
                player.getYaw(), player.getPitch()
        );

        WorldProperties worldProperties = world.getLevelProperties();

        //player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.createCommonPlayerSpawnInfo(world), b));
        player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        //player.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(serverWorld.getSpawnPos(), serverWorld.getSpawnAngle()));
        player.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        player.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        this.sendStatusEffects(player);
        this.sendWorldInfo(player, world);
        this.sendCommandTree(player);
        world.onPlayerConnected(player);



        LOGGER.info(player.getServerWorld().getRegistryKey().getValue().toString());

        LOGGER.info("here?");

        this.players.add(player);

        this.playerMap.put(player.getUuid(), player);
        //player.onSpawn();
        player.setHealth(player.getHealth());
        LOGGER.info("here?");


    }
}
