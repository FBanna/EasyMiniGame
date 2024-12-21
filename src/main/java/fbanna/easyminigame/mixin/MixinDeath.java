package fbanna.easyminigame.mixin;


import com.mojang.authlib.GameProfile;
import fbanna.easyminigame.EasyMiniGame;
import fbanna.easyminigame.play.GameInstance;
import fbanna.easyminigame.play.PlayStates;
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

import java.util.Optional;
import java.util.Set;

import static fbanna.easyminigame.EasyMiniGame.MANAGER;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinDeath extends PlayerEntity{

    //@Shadow public abstract PlayerInventory getInventory();


    public MixinDeath(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow @Final public ServerPlayerInteractionManager interactionManager;
    @Shadow public abstract boolean changeGameMode(GameMode gameMode);
    @Shadow public abstract ServerWorld getServerWorld();

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void inject(DamageSource damageSource, CallbackInfo ci) {

        

        Optional<GameInstance> optionalGameInstance = MANAGER.getInstance(this.getServerWorld().getRegistryKey().getValue().getPath());


        if(optionalGameInstance.isEmpty()) {
            ci.cancel();
            return;
        }

        GameInstance instance = optionalGameInstance.get();





        if(/*this.getWorld().getRegistryKey() == EMG_DIMENSION_KEY && */instance.playState == PlayStates.PLAYING) {

            //CHECK IF THIS IS GOOD ^

            this.setHealth(20);



            if(this.interactionManager.getGameMode() != GameMode.SPECTATOR) {
                this.drop((ServerWorld) this.getWorld(), damageSource);



                boolean isFinal = instance.onDeath(this.getUuid());
                boolean isWin = instance.checkWin();


                if(isFinal && !isWin){ // is final kill

                    this.changeGameMode(GameMode.SPECTATOR);
                    instance.messagePlayers(Text.of(this.getName().getString() + " was final killed"), false);

                } else if (isFinal && isWin){ // is final last kill
                    instance.messagePlayers(Text.of(this.getName().getString() + " was final killed"), false);

                } else if (!isFinal && !isWin) { // is normal kill
                    Text text = this.getDamageTracker().getDeathMessage();
                    //MANAGER.messagePlayers(Text.of(this.getName().getString() + " was massacred"));
                    //MANAGER.messagePlayers(text.getWithStyle(Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.AQUA))));
                    instance.messagePlayers(text, false);
                    instance.respawnPlayer(this.getUuid());

                }

            } else {
                instance.respawnPlayer(this.getUuid());
            }
            ci.cancel();
        }


    }

}
