package fbanna.easyminigame;

import fbanna.easyminigame.command.EasyMiniGameCommand;
import fbanna.easyminigame.dimension.MiniGameDimension;
import fbanna.easyminigame.play.GameManager;
import fbanna.easyminigame.play.PlayStates;
import fbanna.easyminigame.timer.Call;
import fbanna.easyminigame.timer.Timer;
import fbanna.easyminigame.timer.TimerEvent;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionTypes;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.nio.file.Path;


public class EasyMiniGame implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "easyminigame";
    public static final Logger LOGGER = LoggerFactory.getLogger("easyminigame");

	public static final Path PARENTFOLDER = Path.of("./easyminigame");
	public static final Path CONFIG = Path.of("config.json");

	public static GameManager MANAGER;

	public static Timer TIMER = new Timer();

	public static MiniGameDimension DIMENSION;


	//private int countdown = 101;
	//private int joinDelay = 0;
	//private ServerPlayerEntity player;

    @Override
	public void onInitialize() {

		CommandRegistrationCallback.EVENT.register(EasyMiniGameCommand::register);

		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {

			DIMENSION = new MiniGameDimension(server);
			MANAGER = new GameManager(server);
			//MANAGER.stop();
		});



		ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {

			TIMER.update();


			/*
			if (countdown != 0) {
				if (MANAGER.playState == PlayStates.PLAYING) {

					countdown--;

					if (countdown % 20 == 0) {
						MANAGER.countdown(countdown / 20);
					}
				}
			} else if (MANAGER.playState == PlayStates.WAITING) {
				countdown = 101;
			}*/

			/*

			if(joinDelay > 0) {
				joinDelay--;
				if(joinDelay==0) {
					MANAGER.isNeeded(player);
				}
			}*/

		});

		/*
		ServerPlayConnectionEvents.JOIN.register((handler, sender,server) -> {

			//joinDelay = 40;
			//player = handler.getPlayer();


			//TIMER.register(new TimerEvent(40, () -> MANAGER.isNeeded(handler.getPlayer())));
			//MANAGER.isNeeded(handler.getPlayer());
		});
		/*

		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
			try {
				if(world == world.getServer().getWorld(EMG_DIMENSION_KEY) && killedEntity.isPlayer()) {
					MANAGER.playDeath((ServerPlayerEntity) killedEntity);
				}
			}catch (Exception e) {
				EasyMiniGame.LOGGER.info(String.valueOf(e));
			}

		});*/
	}
}
