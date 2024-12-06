package fbanna.easyminigame.play;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
//import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.*;

public class PlayerState {

    private final RegistryKey<World> world;
    private final UUID uuid;
    private final GameMode gameMode;
    private final NbtCompound inventory;
    private final Vec3d pos;
    private final float yaw;
    private final float pitch;
    private final int xpLevel;
    private final float xpProgress;
    private final float health;
    private final NbtCompound hunger;
    private final List<StatusEffectInstance> potions;
    private final int fire;

    //private final NbtCompound state;




    //private final List<String> inventoryItems = new ArrayList<>();
    //private final List<Integer> inventoryCount = new ArrayList<>();






    public static final Codec<PlayerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            World.CODEC.fieldOf("world").forGetter(PlayerState::getWorld),
            Uuids.CODEC.fieldOf("uuid").forGetter(PlayerState::getUuid),
            GameMode.CODEC.fieldOf("gamemode").forGetter(PlayerState::getGameMode),
            NbtCompound.CODEC.fieldOf("inventory").forGetter(PlayerState::getInventory),
            Vec3d.CODEC.fieldOf("position").forGetter(PlayerState::getPos),
            Codec.FLOAT.fieldOf("yaw").forGetter(PlayerState::getYaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(PlayerState::getPitch),
            Codec.INT.fieldOf("xpLevel").forGetter(PlayerState::getXpLevel),
            Codec.FLOAT.fieldOf("xpProgress").forGetter(PlayerState::getXpProgress),
            Codec.FLOAT.fieldOf("health").forGetter(PlayerState::getHealth),
            NbtCompound.CODEC.fieldOf("hunger").forGetter(PlayerState::getHunger),
            StatusEffectInstance.CODEC.listOf().fieldOf("potions").forGetter(PlayerState::getPotions),
            Codec.INT.fieldOf("fire").forGetter(PlayerState::getFire)
            //NbtCompound.CODEC.fieldOf("state").forGetter(PlayerState::getState)


            //ItemStack.CODEC.listOf().fieldOf("inventory").forGetter(PlayerState::getInventory1),




    ).apply(instance, PlayerState::new));

    public PlayerState(ServerPlayerEntity player) {

        this.world = player.getWorld().getRegistryKey();
        this.uuid = player.getUuid();
        this.gameMode = player.interactionManager.getGameMode();

        NbtCompound nbt = new NbtCompound();
        DefaultedList<ItemStack> totalList = DefaultedList.of();
        for(int i = 0; i < player.getInventory().size(); i++) {
            totalList.add(player.getInventory().getStack(i));
        }
        this.inventory = Inventories.writeNbt(nbt, totalList, true, player.getRegistryManager());

        this.pos = player.getPos();
        this.yaw = player.getYaw();
        this.pitch = player.getPitch();

        this.xpLevel = player.experienceLevel;
        this.xpProgress = player.experienceProgress;

        this.health = player.getHealth();
        NbtCompound hunger = new NbtCompound();
        player.getHungerManager().writeNbt(hunger);
        this.hunger = hunger;

        player.clearStatusEffects();
        this.potions = player.getStatusEffects().stream().toList();

        this.fire = player.getFireTicks();

    }



    public PlayerState(RegistryKey<World> world, UUID uuid, GameMode gameMode, NbtCompound inventory, Vec3d pos, float yaw, float pitch, int xpLevel, float xpProgress, float health, NbtCompound hunger, List<StatusEffectInstance> potions,int fire) {
        this.world = world;
        this.uuid = uuid;
        this.gameMode = gameMode;
        this.inventory = inventory;
        this.xpLevel = xpLevel;
        this.xpProgress = xpProgress;
        this.pos = pos;
        this.yaw = yaw;
        this.pitch = pitch;
        this.health = health;
        this.hunger = hunger;
        this.potions = potions;
        this.fire = fire;
    }

    /*

    public PlayerState(RegistryKey<World> world, UUID uuid, NbtCompound state){
        this.world = world;
        this.uuid = uuid;
        //this.pos = pos;
        //this.yaw = yaw;
        //this.fire = fire;
        this.state = state;
    }*/



    public void updatePlayer(ServerPlayerEntity player, MinecraftServer server) {
        /*

        player.readNbt(this.state);
        player.writeCustomDataToNbt();

        NbtList pos = this.state.getList("Pos", NbtElement.DOUBLE_TYPE);
        NbtList rot = this.state.getList("Rotation", NbtElement.FLOAT_TYPE);

        player.teleport(world, pos.getDouble(0), pos.getDouble(1), pos.getDouble(2), rot.getFloat(0), rot.getFloat(1));*/



        /*

        player.detach();
        Object entity = player.getType().create(world);
        if (entity != null) {
            ((Entity)entity).copyFrom(player);
            //((Entity)entity).refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            ((Entity) entity).resetPosition();
            ((Entity)entity).setHeadYaw(player.headYaw);
            player.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
            world.onDimensionChanged((Entity)entity);
        }*/


        RegistryKey<World> worldkey = RegistryKey.of(this.world.getRegistryRef(), this.world.getValue());

        ServerWorld world = server.getWorld(worldkey);

        //FabricDimensions.teleport(player, world, new TeleportTarget(pos, new Vec3d(0,0,0), yaw, pitch));
        //player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
        player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), Set.of(), yaw, pitch, true);
        player.fallDistance = 0;

        player.changeGameMode(this.gameMode);

        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(player.getInventory().size(), ItemStack.EMPTY);

        Inventories.readNbt(this.inventory, stacks, player.getRegistryManager());

        player.getInventory().clear();
        for(int i = 0; i < player.getInventory().size(); i++) {

            player.getInventory().setStack(i, stacks.get(i));

        }

        player.experienceProgress = this.xpProgress;

        player.setExperienceLevel(this.xpLevel);

        player.setHealth(this.health);

        player.getHungerManager().readNbt(this.hunger);

        for(StatusEffectInstance potion: this.potions) {
            player.addStatusEffect(potion);
        }

        player.setFireTicks(this.fire);

    }

    public UUID getUuid() {
        return this.uuid;

    }
    public RegistryKey<World> getWorld() {
        return this.world;
    }
    public GameMode getGameMode() {
        return this.gameMode;
    }

    public NbtCompound getInventory() {
        return this.inventory;
    }

    public Vec3d getPos() {
        return this.pos;
    }
    public float getYaw() {
        return this.yaw;
    }
    public float getPitch() {
        return this.pitch;
    }
    public int getXpLevel() {
        return this.xpLevel;
    }
    public float getXpProgress() {
        return this.xpProgress;
    }
    public float getHealth() {
        return health;
    }
    public NbtCompound getHunger() {
        return this.hunger;
    }

    public List<StatusEffectInstance> getPotions() {
        return potions;
    }

    public int getFire() {
        return this.fire;
    }



    /*public List<String> getInventoryItems() {
        return inventoryItems;
    }

    public List<Integer> getInventoryCount() {
        return inventoryCount;
    }

    public NbtCompound getInventory() {

        List<ItemStack> validatedList = new ArrayList<>();
        for(ItemStack stack: this.inventory) {

            if(stack.isEmpty()){

                validatedList.add(ItemStack.EMPTY);
            } else{
                validatedList.add(stack);
            }
        }
        return this.inventory;
    }*/



}
