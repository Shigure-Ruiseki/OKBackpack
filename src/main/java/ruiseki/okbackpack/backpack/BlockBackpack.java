package ruiseki.okbackpack.backpack;

import static ruiseki.okbackpack.init.TierRegistries.LEATHER;

import java.util.ArrayList;

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

import com.gtnewhorizon.gtnhlib.api.BlockModelInfo;
import com.gtnewhorizon.gtnhlib.api.IBlockModelProvider;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockState;
import com.gtnewhorizon.gtnhlib.client.model.BakedModelQuadContext;
import com.gtnewhorizon.gtnhlib.client.model.baked.BakedModel;
import com.gtnewhorizon.gtnhlib.client.model.color.IBlockColor;

import ruiseki.okbackpack.api.tier.BackpackTier;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.client.renderer.model.BackpackModel;
import ruiseki.okbackpack.init.block.property.TierProperty;
import ruiseki.okcore.block.BlockTile;
import ruiseki.okcore.block.property.BlockProperty;
import ruiseki.okcore.block.property.DirectionProperty;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.DirectionHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.TileHelpers;
import ruiseki.okstoragecore.capabilities.CapabilityStorageWrapperConfig;
import ruiseki.okstoragecore.upgrades.IBookshelfUpgrade;
import ruiseki.okstoragecore.upgrades.ILightUpgrade;
import ruiseki.okstoragecore.upgrades.IRedstoneUpgrade;

public class BlockBackpack extends BlockTile implements IBlockModelProvider, BlockModelInfo, IBlockColor {

    @BlockProperty
    public final static TierProperty TIER_PROPERTY = TierProperty.tier(LEATHER, (world, x, y, z) -> {
        Block block = world.getBlock(x, y, z);
        if (block instanceof BlockBackpack backpack) return backpack.getTierId();
        return LEATHER;
    }, (world, x, y, z, value) -> {});

    @BlockProperty
    public static final DirectionProperty DIRECTION_PROPERTY = DirectionProperty.facing();

    private final String tierId;

    public BlockBackpack(String tierId) {
        super(Material.cloth, TileBackpack.class);
        this.tierId = tierId;
        this.setRotatable(true);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
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
        return this.tierId;
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
    public int colorMultiplier(@Nullable IBlockAccess world, int x, int y, int z, int tintIndex) {
        return CapabilityHelpers.getCapability(world, x, y, z, CapabilityStorageWrapperConfig.CAPABILITY)
            .map(wrapper -> {
                if (tintIndex == 0) {
                    return wrapper.getMainColor();
                } else {
                    return wrapper.getAccentColor();
                }
            })
            .orElse(-1);
    }

    @Override
    public int colorMultiplier(@Nullable ItemStack stack, int tintIndex) {
        return CapabilityHelpers.getCapability(stack, CapabilityStorageWrapperConfig.CAPABILITY)
            .map(wrapper -> {
                if (tintIndex == 0) {
                    return wrapper.getMainColor();
                } else {
                    return wrapper.getAccentColor();
                }
            })
            .orElse(-1);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack);
        TileHelpers.get(world, x, y, z, TileBackpack.class)
            .ifPresent(tile -> { tile.setBackpack(ItemHelpers.copy(stack)); });
    }

    @Override
    public BlockState getStateForPlacement(World world, BlockPos pos, ForgeDirection facing, float hitX, float hitY,
        float hitZ, int meta, EntityLivingBase placer) {
        BlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
        state.setPropertyValue(DIRECTION_PROPERTY, DirectionHelpers.yawToDirection4(placer));
        return state;
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
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z,
        @Nullable EntityPlayer player) {
        return TileHelpers.get(world, x, y, z, TileBackpack.class)
            .map(
                tile -> tile.getBackpackWrapper()
                    .getBackpack())
            .orElse(null);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        ArrayList<ItemStack> list = new ArrayList<>();
        TileHelpers.get(world, x, y, z, TileBackpack.class)
            .ifPresent(
                tile -> list.add(
                    tile.getBackpackWrapper()
                        .getBackpack()));
        return list;
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

    @Override
    public boolean nhlib$isModeled() {
        return true;
    }

    @Override
    public void nhlib$setModeled(boolean modeled) {

    }

    @Override
    public BakedModel getModel(BakedModelQuadContext context) {
        return new BackpackModel();
    }
}
