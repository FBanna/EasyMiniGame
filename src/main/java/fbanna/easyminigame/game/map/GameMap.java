package fbanna.easyminigame.game.map;

import com.google.gson.Gson;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fbanna.easyminigame.EasyMiniGame;
import fbanna.easyminigame.config.DelConfig;
import fbanna.easyminigame.config.GenConfig;
import fbanna.easyminigame.config.GetConfig;
import fbanna.easyminigame.game.Game;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.command.EntitySelector;
import net.minecraft.data.DataProvider;
//import net.minecraft.data.server.loottable.LootTableProvider;
//import net.minecraft.data.server.tag.ItemTagProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static net.minecraft.block.entity.StructureBlockBlockEntity.createRandom;

public class GameMap {

  private final String name;
  private final int teams;
  private final Boundary boundary = new Boundary();
  // private BlockPos[] spawnPoints;
  private SpawnPoint[] spawnPoints;

  private List<LootChest> chestPos = new ArrayList<>();

  public static final Codec<GameMap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Codec.STRING.fieldOf("name").forGetter(GameMap::getName),
      Codec.INT.fieldOf("teams").forGetter(GameMap::getTeams),
      Boundary.CODEC.fieldOf("boundaries").forGetter(GameMap::getBoundaries),
      // BlockPos.CODEC.listOf().fieldOf("spawnPoints").forGetter(GameMap::getSpawnPoints),
      SpawnPoint.CODEC.listOf().fieldOf("spawnPoints").forGetter(GameMap::getSpawnPoints),
      LootChest.CODEC.listOf().fieldOf("lootChest").forGetter(GameMap::listChestPos)).apply(instance, GameMap::new));

  public GameMap(String name, int teams) {
    this.name = name;
    this.teams = teams;
    this.spawnPoints = new SpawnPoint[teams];
    Arrays.fill(this.spawnPoints, new SpawnPoint(new BlockPos(0, 0, 0), 0));
  }

  public GameMap(String name, int teams, Boundary boundary, List<SpawnPoint> spawnPoints, List<LootChest> chestPos) {
    this.name = name;
    this.teams = teams;
    this.boundary.setBoundaries(boundary);
    this.spawnPoints = spawnPoints.toArray(new SpawnPoint[teams]);
    this.chestPos = new ArrayList<>(chestPos);
  }

  public SpawnPoint getSpawnPoint(int team) {
    return this.spawnPoints[team];
  }

  public List<SpawnPoint> getSpawnPoints() {
    return Arrays.stream(this.spawnPoints).toList();
  }

  public void setSpawnPoint(int team, SpawnPoint position) {
    try {
      this.spawnPoints[team] = position;
    } catch (Exception e) {
      EasyMiniGame.LOGGER.info("errr" + e);
    }

  }

  public Boundary getBoundaries() {
    return this.boundary;
  }

  public Boolean setBoundaryPosition(BlockPos corner1, ServerWorld world) {

    Optional<Vec3i> dimensions = getTemplateDimensions(world);
    if (dimensions.isPresent()) {
      this.boundary.setBoundaries(corner1, corner1.add(dimensions.get()));
      return true;
    } else {
      return false;
    }

  }

  public String getName() {
    return this.name;
  }

  public Vec3i getDimensions() {
    return this.boundary.getDimensions();
  }

  public int getTeams() {
    return this.teams;
  }

  public static Optional<GameMap> getMap(Game game, String name) {
    Optional<ArrayList<GameMap>> maps = GetConfig.getMaps(game);

    if (maps.isPresent()) {
      for (GameMap map : maps.get()) {
        if (Objects.equals(map.getName(), name)) {
          return Optional.of(map);
        }
      }
    }

    return Optional.empty();
  }

  public boolean create(Game game) {

    return GenConfig.makeMap(game, this);

  }

  public Optional<Boolean> delete(Game game) {
    return DelConfig.deleteMap(game, this);
  }

  public Optional<Vec3i> getTemplateDimensions(ServerWorld world) {
    StructureTemplateManager manager = world.getStructureTemplateManager();

    // Optional<StructureTemplate> template = manager.getTemplate(new
    // Identifier(this.getName()));
    Optional<StructureTemplate> template = manager.getTemplate(Identifier.of(this.getName()));

    if (template.isPresent()) {
      return Optional.ofNullable(template.get().getSize());
    }
    return Optional.empty();
  }

  public void load(ServerWorld world) {

    // CLEAR ITEMS
    /*
     * try{
     * for (Entity entity : getWorld(server).iterateEntities()) {
     * try{
     * if (entity != null && entity.getType().equals(EntityType.ITEM)) {
     * EasyMiniGame.LOGGER.info("found item!");
     * entity.kill();
     * //entities.remove();
     * 
     * }
     * } catch (Exception e) {
     * EasyMiniGame.LOGGER.info("eroor killing " + e);
     * }
     * 
     * }
     * } catch (Exception e) {
     * EasyMiniGame.LOGGER.info("eorr looping " + e);
     * }
     */

    StructureTemplateManager manager = world.getStructureTemplateManager();

    // Optional<StructureTemplate> template = manager.getTemplate(new
    // Identifier(this.getName()));
    Optional<StructureTemplate> template = manager.getTemplate(Identifier.of(this.getName()));

    BlockPos corner1 = getBoundaries().getCorner1();
    BlockPos corner2 = getBoundaries().getCorner2();

    BlockPos newcorner = BlockPos.min(corner1, corner2);

    if (template.isPresent()) {
      template.get().place(
          world,
          newcorner,
          newcorner,
          new StructurePlacementData(),
          createRandom(2),
          2);
    } else {
      EasyMiniGame.LOGGER.info("not right");
    }

  }

  public void clearChests(ServerWorld world) {
    for (LootChest chest : this.chestPos) {
      if (world.getBlockEntity(chest.pos()) instanceof ChestBlockEntity) {

        ChestBlockEntity block = (ChestBlockEntity) world.getBlockEntity(chest.pos());

        if (chest.lootTable() != null && block != null) {
          block.clear();
          block.setLootTable(null);
        } else {
          EasyMiniGame.LOGGER.info("failed to clear");
        }

      } else {
        EasyMiniGame.LOGGER.info("no chest at coordinate " + chest.pos().toShortString());
      }
    }
  }

  public void genChests(ServerWorld world) {
    for (LootChest chest : this.chestPos) {

      if (world.getBlockEntity(chest.pos()) instanceof ChestBlockEntity) {

        ChestBlockEntity block = (ChestBlockEntity) world.getBlockEntity(chest.pos());

        if (chest.lootTable() != null && block != null) {
          block.setLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, chest.lootTable()));
        } else {
          EasyMiniGame.LOGGER.info("invalid loot table or chest position");
        }

      } else {
        EasyMiniGame.LOGGER.info("no chest at coordinate " + chest.pos().toShortString());
      }

    }
  }

  public void killItems(ServerWorld world) {
    List<Entity> entities = new ArrayList<>();
    Predicate<Entity> predicate = entity -> true;
    world.collectEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), predicate, entities);

    for (Entity entity : entities) {
      entity.kill(world);
    }
  }

  public void resetMap(MinecraftServer server, ServerWorld world, boolean reset) {

    killItems(world);

    clearChests(world);

    if (reset) {
      this.load(world);
    }

    genChests(world);

  }

  public void save(ServerWorld world) {

    clearChests(world); // clear chests so no loot is saved

    StructureTemplateManager manager = world.getStructureTemplateManager();

    // StructureTemplate template = manager.getTemplateOrBlank(new
    // Identifier(this.getName()));
    StructureTemplate template = manager.getTemplateOrBlank(Identifier.of(this.getName()));

    BlockPos corner1 = getBoundaries().getCorner1();
    BlockPos corner2 = getBoundaries().getCorner2();

    BlockPos newcorner = BlockPos.min(corner1, corner2);

    EasyMiniGame.LOGGER.info(String.valueOf(newcorner));

    template.saveFromWorld(world,
        newcorner,
        this.boundary.getDimensions(),
        false,
        Blocks.STRUCTURE_VOID);

    template.setAuthor("easyminigame");

    try {
      // manager.saveTemplate(new Identifier(this.getName()));
      manager.saveTemplate(Identifier.of(this.getName()));
    } catch (Exception e) {
      EasyMiniGame.LOGGER.info(String.valueOf(e));
    }
  }

  public boolean addChestPos(BlockPos pos, RegistryKey<LootTable> lootTable) {

    try {
      if (!this.chestPos.isEmpty()) {
        for (LootChest chest : this.chestPos) {
          if (chest.pos().equals(pos)) {
            this.chestPos.remove(chest);
            return false;
          }
        }
      }
    } catch (Exception e) {
      EasyMiniGame.LOGGER.info(String.valueOf(e));
    }

    this.chestPos.add(new LootChest(pos, lootTable.getValue()));
    return true;
  }

  public int addAllChests(ServerWorld world, RegistryKey<LootTable> lootTable) {
    try {
      BlockPos corner1 = getBoundaries().getCorner1();
      BlockPos corner2 = getBoundaries().getCorner2();

      BlockPos newcorner = BlockPos.min(corner1, corner2);
      Vec3i dimensions = getDimensions();
      int i = 0;

      /*
       * for(int x = 0; x < newcorner.getX(); x++) {
       * for(int y = 0; y < newcorner.getY(); y++) {
       * for(int z = 0; z < newcorner.getZ(); z++) {
       * BlockEntity block = getWorld(sever).getBlockEntity(newcorner.add(new
       * Vec3i(x,y,z)));
       * if(block.getType().equals(BlockEntityType.CHEST)){
       * i++;
       * this.chestPos.add(new LootChest(newcorner.add(new Vec3i(x,y,z)),
       * lootTable.getValue()));
       * }
       * }
       * }
       * }
       */

      for (int x = 0; x < dimensions.getX(); x++) {
        for (int y = 0; y < dimensions.getY(); y++) {
          for (int z = 0; z < dimensions.getZ(); z++) {
            BlockEntity block = world.getBlockEntity(newcorner.add(new Vec3i(x, y, z)));
            if (block != null && block.getType().equals(BlockEntityType.CHEST)) {
              i++;
              this.chestPos.add(new LootChest(newcorner.add(new Vec3i(x, y, z)), lootTable.getValue()));
            }
          }
        }
      }
      return i;
    } catch (Exception e) {
      EasyMiniGame.LOGGER.info("erorr " + e);
    }

    return 0;
  }

  public boolean delChestPos(BlockPos delPos) {
    for (LootChest chest : this.chestPos) {
      if (chest.pos().equals(delPos)) {
        this.chestPos.remove(chest);
        return true;
      }
    }
    return false;
  }

  public List<LootChest> listChestPos() {
    return new ArrayList<>(this.chestPos);
  }

  public void clearChestPos() {
    this.chestPos = new ArrayList<>();
  }

}
