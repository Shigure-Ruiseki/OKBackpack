package ruiseki.okbackpack.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.backpack.backpack.TileBackpack;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okcore.block.BlockBase;
import ruiseki.okcore.datastructure.BlockPos;

public class BlockSleepingBag extends BlockBase {

    private static final int[][] footBlockToHeadBlockMap = new int[][] { { 0, 1 }, { -1, 0 }, { 0, -1 }, { 1, 0 } };

    private record SleepingBagPlacement(int direction, BlockPos footPos, BlockPos headPos) {

        BlockPos ownerPos() {
            return footPos.add(-footBlockToHeadBlockMap[direction][0], 0, -footBlockToHeadBlockMap[direction][1]);
        }
    }

    public record SleepingBagResult(int direction, BlockPos footPos) {

        public static SleepingBagResult failure() {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    private IIcon[] endIcons;

    @SideOnly(Side.CLIENT)
    private IIcon[] sideIcons;

    @SideOnly(Side.CLIENT)
    private IIcon[] topIcons;

    public BlockSleepingBag() {
        super(Material.cloth);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
        this.setHardness(0F);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    /**
     * Returns whether this bed block is the head of the bed.
     */
    private static boolean isBlockHeadOfBed(int meta) {
        return (meta & 8) != 0;
    }

    public static int getDirection(int meta) {
        return meta & 3;
    }

    public static void storeOriginalSpawn(EntityPlayer player) {
        ChunkCoordinates spawn = player.getBedLocation(player.worldObj.provider.dimensionId);
        final BackpackProperty props = BackpackProperty.get(player);

        if (spawn != null && props != null) {
            props.setStoredSpawn(spawn);
            OKBackpack.okLog(
                Level.INFO,
                "Stored spawn data for " + player
                    .getDisplayName() + ": " + spawn + " dimID: " + player.worldObj.provider.dimensionId);
        } else {
            OKBackpack.okLog(
                Level.WARN,
                "Cannot store spawn data for " + player.getDisplayName() + ", because it is non-existent");
        }
    }

    public static void restoreOriginalSpawn(EntityPlayer player) {
        final BackpackProperty props = BackpackProperty.get(player);

        if (props != null) {
            final ChunkCoordinates oldSpawn = props.getStoredSpawn();
            if (oldSpawn != null) {
                player.setSpawnChunk(oldSpawn, false, player.worldObj.provider.dimensionId);
                OKBackpack.okLog(
                    Level.INFO,
                    "Restored spawn data for " + player
                        .getDisplayName() + ": " + oldSpawn + " dimID: " + player.worldObj.provider.dimensionId);
            }
        } else {
            OKBackpack.okLog(Level.WARN, "No spawn data to restore for " + player.getDisplayName());
        }
    }

    public void onPortableBlockActivated(World world, EntityPlayer player, BlockPos pos) {
        if (world.isRemote) return;
        if (!isSleepingInPortableBag(player)) return;
        if (!onBlockActivated(world, pos.getX(), pos.getY(), pos.getZ(), player, 1, 0f, 0f, 0f)) {
            packPortableSleepingBag(player);
        }
    }

    public static boolean isSleepingInPortableBag(EntityPlayer player) {
        final BackpackProperty props = BackpackProperty.get(player);
        return props.isSleepingInPortableBag();
    }

    public static void packPortableSleepingBag(EntityPlayer player) {
        if (isSleepingInPortableBag(player)) {
            final BackpackProperty props = BackpackProperty.get(player);
            props.setSleepingInPortableBag(false);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else {
            int meta = world.getBlockMetadata(x, y, z);

            if (!isBlockHeadOfBed(meta)) {
                int dir = getDirection(meta);
                x += footBlockToHeadBlockMap[dir][0];
                z += footBlockToHeadBlockMap[dir][1];

                if (world.getBlock(x, y, z) != this) {
                    return false;
                }

                meta = world.getBlockMetadata(x, y, z);
            }

            if (world.provider.canRespawnHere() && world.getBiomeGenForCoords(x, z) != BiomeGenBase.hell) {
                if (isBedOccupied(meta)) {
                    EntityPlayer entityplayer1 = null;

                    for (Object o : world.playerEntities) {
                        EntityPlayer entityplayer2 = (EntityPlayer) o;

                        if (entityplayer2.isPlayerSleeping()) {
                            ChunkCoordinates chunkcoordinates = entityplayer2.playerLocation;

                            if (chunkcoordinates.posX == x && chunkcoordinates.posY == y
                                && chunkcoordinates.posZ == z) {
                                entityplayer1 = entityplayer2;
                            }
                        }
                    }

                    if (entityplayer1 != null) {
                        player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.occupied"));
                        return false;
                    }

                    setBedOccupied(world, x, y, z, false);
                }

                EntityPlayer.EnumStatus enumstatus = player.sleepInBedAt(x, y, z);

                if (enumstatus == EntityPlayer.EnumStatus.OK) {
                    setBedOccupied(world, x, y, z, true);

                    storeOriginalSpawn(player);

                    player.setSpawnChunk(new ChunkCoordinates(x, y, z), true, player.dimension);
                    return true;
                } else {
                    if (enumstatus == EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW) {
                        player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.noSleep"));
                    } else if (enumstatus == EntityPlayer.EnumStatus.NOT_SAFE) {
                        player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.notSafe"));
                    }

                    return false;
                }
            } else {
                double d2 = (double) x + 0.5D;
                double d0 = (double) y + 0.5D;
                double d1 = (double) z + 0.5D;
                world.setBlockToAir(x, y, z);
                int k1 = getDirection(meta);
                x += footBlockToHeadBlockMap[k1][0];
                z += footBlockToHeadBlockMap[k1][1];

                if (world.getBlock(x, y, z) == this) {
                    world.setBlockToAir(x, y, z);
                    d2 = (d2 + (double) x + 0.5D) / 2.0D;
                    d0 = (d0 + (double) y + 0.5D) / 2.0D;
                    d1 = (d1 + (double) z + 0.5D) / 2.0D;
                }

                world.newExplosion(null, (float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F, 5.0F, true, true);

                return false;
            }
        }
    }

    private static void setBedOccupied(World world, int x, int y, int z, boolean flag) {
        int l = world.getBlockMetadata(x, y, z);

        if (flag) {
            l |= 4;
        } else {
            l &= -5;
        }

        world.setBlockMetadataWithNotify(x, y, z, l, 4);
    }

    private static boolean isBedOccupied(int meta) {
        return (meta & 4) != 0;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        BlockPos pos = new BlockPos(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        SleepingBagPlacement placement = resolvePlacementFromBlock(pos, meta);
        BlockPos otherPos = isBlockHeadOfBed(meta) ? placement.footPos() : placement.headPos();
        if (otherPos.getBlock(world) != this) {
            world.setBlockToAir(x, y, z);
            if (!world.isRemote && !isBlockHeadOfBed(meta)) {
                this.dropBlockAsItem(world, x, y, z, meta, 0);
            }
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return null;
    }

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
        if (player.capabilities.isCreativeMode && isBlockHeadOfBed(meta)) {
            SleepingBagPlacement placement = resolvePlacementFromBlock(new BlockPos(x, y, z), meta);
            BlockPos footPos = placement.footPos();
            if (footPos.getBlock(world) == this) {
                world.setBlockToAir(footPos.getX(), footPos.getY(), footPos.getZ());
            }
        }
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion boom) {
        this.onBlockDestroyedByPlayer(world, x, y, z, world.getBlockMetadata(x, y, z));
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
        BlockPos pos = new BlockPos(x, y, z);
        SleepingBagPlacement placement = resolvePlacementFromBlock(pos, meta);
        removeOtherPart(world, pos, placement);
        if (placement.ownerPos()
            .getTileEntity(world) instanceof TileBackpack teBackpack) {
            teBackpack.setSleepingBagDeployed(false);
        }
    }

    @Override
    public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0) {
            return Blocks.planks.getBlockTextureFromSide(side);
        } else {
            int k = getDirection(meta);
            int l = Direction.bedDirection[k][side];
            int isHead = isBlockHeadOfBed(meta) ? 1 : 0;
            return (isHead != 1 || l != 2) && (isHead != 0 || l != 3)
                ? (l != 5 && l != 4 ? this.topIcons[isHead] : this.sideIcons[isHead])
                : this.endIcons[isHead];
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        this.topIcons = new IIcon[] { iconRegister.registerIcon(Reference.PREFIX_MOD + "sleepingBag_feet_top"),
            iconRegister.registerIcon(Reference.PREFIX_MOD + "sleepingBag_head_top") };

        this.endIcons = new IIcon[] { iconRegister.registerIcon(Reference.PREFIX_MOD + "sleepingBag_feet_end"),
            iconRegister.registerIcon(Reference.PREFIX_MOD + "sleepingBag_head_end") };

        this.sideIcons = new IIcon[] { iconRegister.registerIcon(Reference.PREFIX_MOD + "sleepingBag_feet_side"),
            iconRegister.registerIcon(Reference.PREFIX_MOD + "sleepingBag_head_side") };
    }

    @Override
    public int getRenderType() {
        return 14;
    }

    public static SleepingBagResult canDeploySleepingBag(World world, EntityPlayer player, BlockPos pos,
        boolean isTile) {
        int switchBy = -1;
        if (isTile) {
            if (pos.getTileEntity(world) instanceof TileBackpack te && !te.isSleepingBagDeployed()) {
                switchBy = te.getRotation()
                    .ordinal() & 3;
            }
        } else {
            int playerDirection = MathHelper.floor_double((player.rotationYaw * 4F) / 360F + 0.5D) & 3;

            int[] tileSequence = { 2, 0, 3, 1 };
            switchBy = tileSequence[playerDirection];
        }
        SleepingBagPlacement placement = resolvePlacementFromSwitch(switchBy, pos);
        if (placement != null && canPlaceSleepingBag(world, placement)) {
            return new SleepingBagResult(placement.direction(), placement.footPos());
        }
        return SleepingBagResult.failure();
    }

    private static SleepingBagPlacement resolvePlacementFromSwitch(int switchBy, BlockPos pos) {
        return switch (switchBy) {
            case 0 -> createPlacement(1, pos.add(-1, 0, 0));
            case 1 -> createPlacement(3, pos.add(1, 0, 0));
            case 2 -> createPlacement(0, pos.add(0, 0, 1));
            case 3 -> createPlacement(2, pos.add(0, 0, -1));
            default -> null;
        };
    }

    private static SleepingBagPlacement createPlacement(int direction, BlockPos footPos) {
        return new SleepingBagPlacement(direction, footPos, offsetByDirection(footPos, direction));
    }

    private static SleepingBagPlacement resolvePlacementFromBlock(BlockPos pos, int meta) {
        int direction = getDirection(meta);

        if (isBlockHeadOfBed(meta)) {
            pos = pos.add(-footBlockToHeadBlockMap[direction][0], 0, -footBlockToHeadBlockMap[direction][1]);
        }

        return createPlacement(direction, pos);
    }

    private static boolean canPlaceSleepingBag(World world, SleepingBagPlacement placement) {
        return canOccupySleepingBagBlock(world, placement.footPos())
            && canOccupySleepingBagBlock(world, placement.headPos());
    }

    private static boolean canOccupySleepingBagBlock(World world, BlockPos pos) {
        Block block = world.getBlock(pos.getX(), pos.getY(), pos.getZ());

        return (world.isAirBlock(pos.getX(), pos.getY(), pos.getZ())
            || block.isReplaceable(world, pos.getX(), pos.getY(), pos.getZ()))
            && world.getBlock(pos.getX(), pos.getY() - 1, pos.getZ())
                .getMaterial()
                .isSolid();
    }

    private static void removeOtherPart(World world, BlockPos pos, SleepingBagPlacement placement) {
        BlockPos otherPos = pos.equals(placement.footPos()) ? placement.headPos() : placement.footPos();

        if (world.getBlock(otherPos.getX(), otherPos.getY(), otherPos.getZ())
            == BlockSleepingBagConfig._instance.getInstance()) {
            world.setBlockToAir(otherPos.getX(), otherPos.getY(), otherPos.getZ());
        }
    }

    public static boolean spawnSleepingBag(EntityPlayer player, World world, int direction, BlockPos footPos) {
        SleepingBagPlacement placement = createPlacement(direction & 3, footPos);
        if (!canPlaceSleepingBag(world, placement)) return false;
        Block sleepingBag = BlockSleepingBagConfig._instance.getInstance();
        BlockPos headPos = placement.headPos();
        if (world.setBlock(footPos.getX(), footPos.getY(), footPos.getZ(), sleepingBag, direction, 3)) {
            world.playSoundAtEntity(player, Block.soundTypeCloth.func_150496_b(), 0.5f, 1.0f);
            if (world.setBlock(headPos.getX(), headPos.getY(), headPos.getZ(), sleepingBag, direction + 8, 3)) {
                return true;
            }
            world.setBlockToAir(footPos.getX(), footPos.getY(), footPos.getZ());
        }
        return false;
    }

    private static BlockPos offsetByDirection(BlockPos pos, int direction) {
        return pos.add(footBlockToHeadBlockMap[direction][0], 0, footBlockToHeadBlockMap[direction][1]);
    }
}
