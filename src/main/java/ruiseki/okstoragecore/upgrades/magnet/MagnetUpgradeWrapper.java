
package ruiseki.okstoragecore.upgrades.magnet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.IItemHandlerSimpleInserter;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.XpHelpers;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.upgrades.ContentsFilterLogic;
import ruiseki.okstoragecore.upgrades.IContentsFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.IPickupResponseUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class MagnetUpgradeWrapper extends UpgradeWrapperBase<MagnetUpgradeWrapper, ItemMagnetUpgrade>
    implements IContentsFilteredUpgrade, ITickableUpgrade, IPickupResponseUpgrade {

    private static final String PREVENT_REMOTE_MOVEMENT = "PreventRemoteMovement";
    private static final String ALLOW_MACHINE_MOVEMENT = "AllowMachineRemoteMovement";

    private static final int COOLDOWN_TICKS = 10;
    private static final int FULL_COOLDOWN_TICKS = 40;

    private static final Set<IMagnetPreventionChecker> magnetCheckers = new HashSet<>();

    private final ContentsFilterLogic filterLogic;
    private final int slotCount;

    public static void addMagnetPreventionChecker(IMagnetPreventionChecker checker) {
        magnetCheckers.add(checker);
    }

    protected MagnetUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);

        this.slotCount = upgradeItem.getFilterSlotCount();

        this.filterLogic = new ContentsFilterLogic(
            upgrade,
            upgradeSaveHandler,
            slotCount,
            storageWrapper::getInventoryHandler,
            storageWrapper.getSettingsHandler()
                .getTypeCategory(MemorySettingsCategory.class));
    }

    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public ContentsFilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Override
    public ItemStack pickup(World world, ItemStack stack, boolean simulate) {
        if (!shouldPickupItems() || !filterLogic.matchesFilter(stack)) return stack;
        return storageWrapper.getInventoryForUpgradeProcessing()
            .insertItem(stack, simulate);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world)) return;
        int cooldown = shouldPickupItems() ? pickupItems(entity, world, pos) : FULL_COOLDOWN_TICKS;
        if (shouldPickupXp() && canFillStorageWithXp()) {
            cooldown = Math.min(cooldown, pickupXpOrbs(entity, world, pos));
        }
        setCooldown(world, cooldown);
    }

    private boolean canFillStorageWithXp() {
        return storageWrapper.getFluidHandler()
            .map(fluidHandler -> fluidHandler.fill(new FluidStack(XpHelpers.getXpJuiceFluid(), 1), false, false) > 0)
            .orElse(false);
    }

    private int pickupXpOrbs(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        AxisAlignedBB area = AxisAlignedBB
            .getBoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
            .expand(upgradeItem.getRadius(), upgradeItem.getRadius(), upgradeItem.getRadius());
        List<EntityXPOrb> xpEntities = world.getEntitiesWithinAABB(EntityXPOrb.class, area);
        if (xpEntities.isEmpty()) return COOLDOWN_TICKS;
        int cooldown = COOLDOWN_TICKS;
        EntityPlayer player = entity instanceof EntityPlayer ? (EntityPlayer) entity : null;
        for (EntityXPOrb xpOrb : xpEntities) {
            if (xpOrb.isDead || xpOrb.xpValue <= 0 || canNotPickup(xpOrb, entity)) continue;
            if (player != null) {
                boolean canceled = MinecraftForge.EVENT_BUS.post(new PlayerPickupXpEvent(player, xpOrb));
                if (xpOrb.isDead || xpOrb.xpValue <= 0) continue;
                if (canceled) {
                    cooldown = FULL_COOLDOWN_TICKS;
                    continue;
                }
            }
            if (!tryToFillTank(xpOrb, entity, world)) {
                cooldown = FULL_COOLDOWN_TICKS;
                break;
            }
        }
        return cooldown;
    }

    private boolean tryToFillTank(EntityXPOrb xpOrb, @Nullable EntityLivingBase entity, World world) {
        int amountToTransfer = XpHelpers.experienceToLiquid(xpOrb.xpValue);
        return storageWrapper.getFluidHandler()
            .map(fluidHandler -> {
                int amountAdded = fluidHandler
                    .fill(new FluidStack(XpHelpers.getXpJuiceFluid(), amountToTransfer), true);

                if (amountAdded > 0) {
                    BlockPos pos = new BlockPos(xpOrb);
                    xpOrb.xpValue = 0;
                    xpOrb.setDead();

                    EntityPlayer player = entity instanceof EntityPlayer ? (EntityPlayer) entity : null;

                    if (player != null) {
                        XpHelpers.playXpPickupSound(world, player);
                    }

                    if (amountToTransfer > amountAdded) {
                        world.spawnEntityInWorld(
                            new EntityXPOrb(
                                world,
                                pos.x(),
                                pos.y(),
                                pos.z(),
                                (int) XpHelpers.liquidToExperience(amountToTransfer - amountAdded)));
                    }
                    return true;
                }
                return false;
            })
            .orElse(false);
    }

    private int pickupItems(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        AxisAlignedBB area = AxisAlignedBB
            .getBoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
            .expand(upgradeItem.getRadius(), upgradeItem.getRadius(), upgradeItem.getRadius());

        List<EntityItem> itemEntities = world.getEntitiesWithinAABB(EntityItem.class, area);
        if (itemEntities.isEmpty()) return COOLDOWN_TICKS;

        int cooldown = COOLDOWN_TICKS;
        EntityPlayer player = entity instanceof EntityPlayer ? (EntityPlayer) entity : null;

        for (EntityItem itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getEntityItem();

            if (itemEntity.isDead || ItemHelpers.isEmpty(stack)
                || !filterLogic.matchesFilter(stack)
                || canNotPickup(itemEntity, entity)) {
                continue;
            }

            if (player != null) {
                boolean canceled = MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(player, itemEntity));
                if (itemEntity.isDead || ItemHelpers.isEmpty(itemEntity.getEntityItem())) continue;
                if (canceled) {
                    cooldown = FULL_COOLDOWN_TICKS;
                    continue;
                }
            }

            if (!tryToInsertItem(itemEntity)) {
                cooldown = FULL_COOLDOWN_TICKS;
            }
        }
        return cooldown;
    }

    private boolean isBlockedBySomething(Entity entity) {
        for (IMagnetPreventionChecker checker : magnetCheckers) {
            if (checker.isBlocked(entity)) {
                return true;
            }
        }
        return false;
    }

    private boolean canNotPickup(Entity entity, @Nullable EntityLivingBase player) {
        if (isBlockedBySomething(entity)) {
            return true;
        }

        NBTTagCompound data = entity.getEntityData();
        return player != null ? data.hasKey(PREVENT_REMOTE_MOVEMENT)
            : data.hasKey(PREVENT_REMOTE_MOVEMENT) && !data.hasKey(ALLOW_MACHINE_MOVEMENT);
    }

    private boolean tryToInsertItem(EntityItem itemEntity) {
        ItemStack stack = itemEntity.getEntityItem();
        IItemHandlerSimpleInserter inventory = storageWrapper.getInventoryForUpgradeProcessing();
        ItemStack remaining = inventory.insertItem(stack, true);
        boolean insertedSomething = false;
        if (ItemHelpers.getCount(remaining) != ItemHelpers.getCount(stack)) {
            insertedSomething = true;
            remaining = inventory.insertItem(stack, false);
            if (ItemHelpers.isEmpty(remaining)) itemEntity.setDead();
            itemEntity.setEntityItemStack(remaining);
        }
        return insertedSomething;
    }

    public void setPickupItems(boolean pickupItems) {
        NBTHelpers.setBoolean(upgrade, "pickupItems", pickupItems);
        save();
    }

    public boolean shouldPickupItems() {
        return NBTHelpers.getBoolean(upgrade, "pickupItems")
            .orElse(true);
    }

    public void setPickupXp(boolean pickupXp) {
        NBTHelpers.setBoolean(upgrade, "pickupXp", pickupXp);
        save();
    }

    public boolean shouldPickupXp() {
        return NBTHelpers.getBoolean(upgrade, "pickupXp")
            .orElse(true);
    }
}
