package fbanna.easyminigame.mixin;


import com.mojang.authlib.GameProfile;
import fbanna.easyminigame.EasyMiniGame;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static fbanna.easyminigame.EasyMiniGame.MANAGER;
import static fbanna.easyminigame.dimension.MiniGameDimension.EMG_DIMENSION_KEY;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinDeath extends PlayerEntity{

    //@Shadow public abstract PlayerInventory getInventory();

    @Shadow @Final public ServerPlayerInteractionManager interactionManager;

    public MixinDeath(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract boolean changeGameMode(GameMode gameMode);

    @Shadow public abstract SyncedClientOptions getClientOptions();

    @Shadow public abstract boolean damage(DamageSource source, float amount);


    @Shadow @Final public MinecraftServer server;

    @Shadow public abstract void resetStat(Stat<?> stat);

    @Shadow public abstract boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch);

    @Shadow protected abstract void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition);

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void inject(DamageSource damageSource, CallbackInfo ci) {




        if(this.getWorld().getRegistryKey() == EMG_DIMENSION_KEY) {
            //EasyMiniGame.LOGGER.info("huhhhhh");
            this.setHealth(20);

            if(this.interactionManager.getGameMode() != GameMode.SPECTATOR) {
                boolean result = MANAGER.playDeath(this.getUuid());


                this.drop(damageSource);

                if(result){

                    this.changeGameMode(GameMode.SPECTATOR);
                    MANAGER.messagePlayers(Text.of(this.getName().getString() + " was final killed"), false);

                } else {
                    Text text = this.getDamageTracker().getDeathMessage();
                    //MANAGER.messagePlayers(Text.of(this.getName().getString() + " was massacred"));
                    //MANAGER.messagePlayers(text.getWithStyle(Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.AQUA))));
                    MANAGER.messagePlayers(text, false);
                    MANAGER.respawnPlayer(this.getUuid());
                }

            } else {
                MANAGER.respawnPlayer(this.getUuid());
            }
            ci.cancel();
        }


    }

}
