package fbanna.easyminigame.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fbanna.easyminigame.EasyMiniGame;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Boundary {
    private BlockPos corner1 = new BlockPos(0,0,0);
    private BlockPos corner2 = new BlockPos(0,0,0);

    public static final Codec<Boundary> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("corner1").forGetter(Boundary::getCorner1),
            BlockPos.CODEC.fieldOf("corner2").forGetter(Boundary::getCorner2)
    ).apply(instance, Boundary::new));

    public Boundary() {
    }

    public Boundary(BlockPos corner1, BlockPos corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public BlockPos getCorner1(){
        return this.corner1;
    }

    public BlockPos getCorner2(){
        return this.corner2;
    }

    public void setBoundaries(BlockPos corner1, BlockPos corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public void setBoundaries(Boundary boundary) {
        this.corner1 = boundary.getCorner1();
        this.corner2 = boundary.getCorner2();
    }

    public Vec3i getDimensions() {
        int x = Math.abs(corner1.getX() - corner2.getX());
        int y = Math.abs(corner1.getY() - corner2.getY());
        int z = Math.abs(corner1.getZ() - corner2.getZ());

        return new Vec3i(x,y,z);
    }

    public Boolean isUsed() {
        if (this.corner1 == null || this.corner2 == null) {

            return false;
        }
        return true;
    }

}
