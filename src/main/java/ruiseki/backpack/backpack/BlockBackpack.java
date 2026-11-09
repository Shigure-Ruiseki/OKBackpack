package ruiseki.backpack.backpack;

import static ruiseki.okbackpack.common.init.TierRegistries.LEATHER;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;

import ruiseki.okbackpack.api.tier.BackpackTier;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.api.wrapper.IBookshelfUpgrade;
import ruiseki.okbackpack.common.block.property.TierProperty;
import ruiseki.okcore.block.BlockTile;
import ruiseki.okcore.block.property.BlockProperty;
import ruiseki.okcore.helper.TileHelpers;
import ruiseki.okstoragecore.upgrades.ILightUpgrade;
import ruiseki.okstoragecore.upgrades.IRedstoneUpgrade;

public class BlockBackpack extends BlockTile {

    @BlockProperty
    public final static TierProperty TIER_PROPERTY = TierProperty.tier(LEATHER, (world, x, y, z) -> {
        Block block = world.getBlock(x, y, z);
        if (block instanceof ruiseki.okbackpack.common.block.BlockBackpack backpack) {
            return backpack.getTierId();
        }
        return LEATHER;
    }, (world, x, y, z, value) -> {});

    private final String tierId;

    public BlockBackpack(String tierId) {
        super(Material.cloth, TileBackpack.class);
        this.tierId = tierId;
    }

    /**
     * Returns the id of the tier of this block.
     * <p>
     * The id is stored instead of the tier object because the blocks are created before the tiers are
     * registered, so an early lookup would hand every block the fallback tier.
     *
     * @return the tier id, never {@code null}
     */
    public String getTierId() {
        return tierId;
    }

    /**
     * Returns the tier of this block, resolved at the time of the call.
     *
     * @return the tier, or the fallback tier when the id is not registered
     */
    public BackpackTier getTier() {
        return TierRegistry.getTier(tierId);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack);
        TileHelpers.get(world, x, y, z, TileBackpack.class)
            .ifPresent(tile -> { tile.setBackpack(stack); });
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        return TileHelpers.get(worldIn, x, y, z, TileBackpack.class)
            .map(tile -> tile.onBlockActivated(worldIn, player, ForgeDirection.getOrientation(side), subX, subY, subZ))
            .orElse(super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ));
    }

    @Override
    public boolean shouldDropInventory(World world, int x, int y, int z) {
        return false;
    }

    @Override
    public boolean isDropBlockItem(IBlockAccess world, int x, int y, int z, int fortune) {
        return false;
    }

    @Override
    public boolean isKeepNBTOnDrop() {
        return false;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z,
        @Nullable EntityPlayer player) {
        ItemStack stack = new ItemStack(this);
        TileHelpers.get(world, x, y, z, TileBackpack.class)
            .ifPresent(
                tile -> {
                    stack.setTagCompound(
                        tile.getBackpackWrapper()
                            .getBackpack()
                            .getTagCompound());
                });
        return stack;
    }

    @Override
    public float getEnchantPowerBonus(World world, int x, int y, int z) {
        return TileHelpers.get(world, x, y, z, TileBackpack.class)
            .map(
                te -> te.getBackpackWrapper()
                    .getUpgradeHandler()
                    .getListOfWrappersThatImplement(IBookshelfUpgrade.class)
                    .stream()
                    .mapToDouble(IBookshelfUpgrade::getEnchantPowerBonus)
                    .sum())
            .orElse(0.0)
            .floatValue();
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return TileHelpers.get(world, x, y, z, TileBackpack.class)
            .map(
                te -> te.getBackpackWrapper()
                    .getUpgradeHandler()
                    .getListOfWrappersThatImplement(ILightUpgrade.class)
                    .stream()
                    .mapToInt(ILightUpgrade::getLightLevel)
                    .sum())
            .orElse(super.getLightValue(world, x, y, z));
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return TileHelpers.get(world, x, y, z, TileBackpack.class)
            .map(
                te -> te.getBackpackWrapper()
                    .getUpgradeHandler()
                    .getListOfWrappersThatImplement(IRedstoneUpgrade.class)
                    .stream()
                    .mapToInt(IRedstoneUpgrade::getRedstonePower)
                    .sum())
            .orElse(super.isProvidingWeakPower(world, x, y, z, side));
    }
}
