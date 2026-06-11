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
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okcore.block.BlockOK;

public class BlockSleepingBag extends BlockOK {

    private static final int[][] footBlockToHeadBlockMap = new int[][] { { 0, 1 }, { -1, 0 }, { 0, -1 }, { 1, 0 } };

    private record SleepingBagPlacement(int direction, int footX, int footY, int footZ, int headX, int headY,
        int headZ) {

        int ownerX() {
            return footX - footBlockToHeadBlockMap[direction][0];
        }

        int ownerZ() {
            return footZ - footBlockToHeadBlockMap[direction][1];
        }
    }

    @SideOnly(Side.CLIENT)
    private IIcon[] endIcons;

    @SideOnly(Side.CLIENT)
    private IIcon[] sideIcons;

    @SideOnly(Side.CLIENT)
    private IIcon[] topIcons;

    public BlockSleepingBag() {
        super("sleeping_bag", Material.cloth);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
        this.setHardness(0F);
        this.isFullSize = this.isOpaque = false;
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

    public void onPortableBlockActivated(World world, EntityPlayer player, int cX, int cY, int cZ) {
        if (world.isRemote) return;
        if (!isSleepingInPortableBag(player)) return;
        if (!onBlockActivated(world, cX, cY, cZ, player, 1, 0f, 0f, 0f)) packPortableSleepingBag(player);
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
        int meta = world.getBlockMetadata(x, y, z);
        SleepingBagPlacement placement = resolvePlacementFromBlock(x, y, z, meta);
        int otherX = isBlockHeadOfBed(meta) ? placement.footX() : placement.headX();
        int otherZ = isBlockHeadOfBed(meta) ? placement.footZ() : placement.headZ();
        if (world.getBlock(otherX, y, otherZ) != this) {
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
            SleepingBagPlacement placement = resolvePlacementFromBlock(x, y, z, meta);
            if (world.getBlock(placement.footX(), y, placement.footZ()) == this) {
                world.setBlockToAir(placement.footX(), y, placement.footZ());
            }
        }
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion boom) {
        this.onBlockDestroyedByPlayer(world, x, y, z, world.getBlockMetadata(x, y, z));
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
        SleepingBagPlacement placement = resolvePlacementFromBlock(x, y, z, meta);
        removeOtherPart(world, x, y, z, placement);
        if (world.getTileEntity(placement.ownerX(), y, placement.ownerZ()) instanceof TEBackpack teBackpack) {
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

    public static int[] canDeploySleepingBag(World world, EntityPlayer player, int cX, int cY, int cZ, boolean isTile) {
        int switchBy = -1;
        if (isTile) {
            TEBackpack te = (TEBackpack) world.getTileEntity(cX, cY, cZ);
            if (!te.isSleepingBagDeployed()) switchBy = te.getFacing()
                .ordinal() & 3;
        } else {
            int playerDirection = MathHelper.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;
            int[] tileSequence = { 2, 0, 3, 1 };
            for (int i = 0; i < tileSequence.length; i++) // converts to use isTile format
            {
                if (playerDirection == i) {
                    switchBy = tileSequence[i];
                    break;
                }
            }
        }
        SleepingBagPlacement placement = resolvePlacementFromSwitch(switchBy, cX, cY, cZ);
        if (placement != null && canPlaceSleepingBag(world, placement)) {
            return new int[] { placement.direction(), placement.footX(), placement.footY(), placement.footZ() };
        }
        return new int[] { -1, cX, cY, cZ };
    }

    private static SleepingBagPlacement resolvePlacementFromSwitch(int switchBy, int cX, int cY, int cZ) {
        switch (switchBy) {
            case 0:
                return createPlacement(1, cX - 1, cY, cZ);
            case 1:
                return createPlacement(3, cX + 1, cY, cZ);
            case 2:
                return createPlacement(0, cX, cY, cZ + 1);
            case 3:
                return createPlacement(2, cX, cY, cZ - 1);
            default:
                return null;
        }
    }

    private static SleepingBagPlacement createPlacement(int direction, int footX, int footY, int footZ) {
        return new SleepingBagPlacement(
            direction,
            footX,
            footY,
            footZ,
            footX + footBlockToHeadBlockMap[direction][0],
            footY,
            footZ + footBlockToHeadBlockMap[direction][1]);
    }

    private static SleepingBagPlacement resolvePlacementFromBlock(int x, int y, int z, int meta) {
        int direction = getDirection(meta);
        if (isBlockHeadOfBed(meta)) {
            x -= footBlockToHeadBlockMap[direction][0];
            z -= footBlockToHeadBlockMap[direction][1];
        }
        return createPlacement(direction, x, y, z);
    }

    private static boolean canPlaceSleepingBag(World world, SleepingBagPlacement placement) {
        return canOccupySleepingBagBlock(world, placement.footX(), placement.footY(), placement.footZ())
            && canOccupySleepingBagBlock(world, placement.headX(), placement.headY(), placement.headZ());
    }

    private static boolean canOccupySleepingBagBlock(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return canReplaceSleepingBagBlock(world, block, x, y, z) && hasSolidSupport(world, x, y, z);
    }

    private static boolean canReplaceSleepingBagBlock(World world, Block block, int x, int y, int z) {
        return world.isAirBlock(x, y, z) || block.isReplaceable(world, x, y, z);
    }

    private static boolean hasSolidSupport(World world, int x, int y, int z) {
        return world.getBlock(x, y - 1, z)
            .getMaterial()
            .isSolid();
    }

    private static void removeOtherPart(World world, int x, int y, int z, SleepingBagPlacement placement) {
        int otherX = x == placement.footX() && z == placement.footZ() ? placement.headX() : placement.footX();
        int otherZ = x == placement.footX() && z == placement.footZ() ? placement.headZ() : placement.footZ();
        if (world.getBlock(otherX, y, otherZ) == ModBlocks.SLEEPING_BAG.getBlock()) {
            world.setBlockToAir(otherX, y, otherZ);
        }
    }

    public static boolean spawnSleepingBag(EntityPlayer player, World world, int meta, int cX, int cY, int cZ) {
        SleepingBagPlacement placement = createPlacement(meta & 3, cX, cY, cZ);
        if (!canPlaceSleepingBag(world, placement)) {
            return false;
        }

        Block sleepingBag = ModBlocks.SLEEPING_BAG.getBlock();
        if (world.setBlock(placement.footX(), placement.footY(), placement.footZ(), sleepingBag, meta, 3)) {
            world.playSoundAtEntity(player, Block.soundTypeCloth.func_150496_b(), 0.5f, 1.0f);
            if (world.setBlock(placement.headX(), placement.headY(), placement.headZ(), sleepingBag, meta + 8, 3)) {
                return true;
            }
            world.setBlockToAir(placement.footX(), placement.footY(), placement.footZ());
        }
        return false;
    }
}
