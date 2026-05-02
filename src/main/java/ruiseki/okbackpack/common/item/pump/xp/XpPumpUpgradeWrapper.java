package ruiseki.okbackpack.common.item.pump.xp;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.api.wrapper.IXpPumpUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class XpPumpUpgradeWrapper extends UpgradeWrapperBase implements IXpPumpUpgrade {

    public static final String DIRECTION_TAG = "XpPumpDirection";
    public static final String LEVEL_TARGET_TAG = "XpPumpLevelTarget";
    public static final String LEVELS_TO_STORE_TAG = "XpPumpLevelsToStore";
    public static final String LEVELS_TO_TAKE_TAG = "XpPumpLevelsToTake";
    public static final String MENDING_TAG = "XpPumpMending";

    public static final int DEFAULT_LEVEL_TARGET = 10;
    public static final int MAX_LEVEL_TARGET = 10000;

    private static final int COOLDOWN_TICKS = 5;
    private static final int PLAYER_SEARCH_RANGE = 3;
    private static final String XPJUICE_FLUID_NAME = "xpjuice";

    private int cooldown = 0;

    public XpPumpUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    public static Fluid getXpJuiceFluid() {
        return FluidRegistry.getFluid(XPJUICE_FLUID_NAME);
    }

    @Override
    public XpPumpDirection getDirection() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, DIRECTION_TAG, XpPumpDirection.INPUT.ordinal());
        XpPumpDirection[] values = XpPumpDirection.values();
        if (ordinal < 0 || ordinal >= values.length) return XpPumpDirection.INPUT;
        return values[ordinal];
    }

    @Override
    public void setDirection(XpPumpDirection direction) {
        ItemNBTHelpers.setInt(upgrade, DIRECTION_TAG, direction.ordinal());
        save();
    }

    @Override
    public int getLevelTarget() {
        return ItemNBTHelpers.getInt(upgrade, LEVEL_TARGET_TAG, DEFAULT_LEVEL_TARGET);
    }

    @Override
    public void setLevelTarget(int level) {
        if (level < 0) level = 0;
        if (level > MAX_LEVEL_TARGET) level = MAX_LEVEL_TARGET;
        ItemNBTHelpers.setInt(upgrade, LEVEL_TARGET_TAG, level);
        save();
    }

    @Override
    public int getLevelsToStore() {
        return Math.max(1, ItemNBTHelpers.getInt(upgrade, LEVELS_TO_STORE_TAG, 1));
    }

    @Override
    public void setLevelsToStore(int levels) {
        if (levels < 1) levels = 1;
        ItemNBTHelpers.setInt(upgrade, LEVELS_TO_STORE_TAG, levels);
        save();
    }

    @Override
    public int getLevelsToTake() {
        return Math.max(1, ItemNBTHelpers.getInt(upgrade, LEVELS_TO_TAKE_TAG, 1));
    }

    @Override
    public void setLevelsToTake(int levels) {
        if (levels < 1) levels = 1;
        ItemNBTHelpers.setInt(upgrade, LEVELS_TO_TAKE_TAG, levels);
        save();
    }

    @Override
    public boolean isMending() {
        return ItemNBTHelpers.getBoolean(upgrade, MENDING_TAG, true);
    }

    @Override
    public void setMending(boolean mending) {
        ItemNBTHelpers.setBoolean(upgrade, MENDING_TAG, mending);
        save();
    }

    @Override
    public boolean isEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, ENABLED_TAG, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, ENABLED_TAG, enabled);
        save();
    }

    @Override
    public void toggle() {
        setEnabled(!isEnabled());
    }

    @Override
    public boolean hasTankAvailable() {
        return findTank() != null;
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.xp_pump_settings";
    }

    private ITankUpgrade findTank() {
        Map<Integer, ITankUpgrade> tanks = storage.gatherCapabilityUpgrades(ITankUpgrade.class);
        if (tanks.isEmpty()) return null;
        return tanks.values()
            .iterator()
            .next();
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player == null || player.worldObj.isRemote) return false;
        if (!isEnabled()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        cooldown = COOLDOWN_TICKS;
        ITankUpgrade tank = findTank();
        if (tank == null) return false;
        return interactWithPlayer(tank, player);
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world == null || world.isRemote) return false;
        if (!isEnabled()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        cooldown = COOLDOWN_TICKS;
        ITankUpgrade tank = findTank();
        if (tank == null) return false;

        boolean dirty = false;
        double cx = pos.x + 0.5;
        double cy = pos.y + 0.5;
        double cz = pos.z + 0.5;
        for (Object obj : world.playerEntities) {
            if (!(obj instanceof EntityPlayer player)) continue;
            double dx = player.posX - cx;
            double dy = player.posY - cy;
            double dz = player.posZ - cz;
            if (dx * dx + dy * dy + dz * dz > PLAYER_SEARCH_RANGE * PLAYER_SEARCH_RANGE) continue;
            dirty |= interactWithPlayer(tank, player);
        }
        return dirty;
    }

    private boolean interactWithPlayer(ITankUpgrade tank, EntityPlayer player) {
        XpPumpDirection direction = getDirection();
        boolean dirty = false;

        // Mending repair runs independently of XP transfer direction so that the toggle stays
        // useful even when the pump is set to OFF (passive repair mode).
        if (isMending() && Mods.EtFuturum.isModLoaded() && XpHelpers.isMendingEnabled()) {
            dirty |= tryRepairMendingItems(tank, player);
        }

        if (direction == XpPumpDirection.OFF) return dirty;

        int targetLevel = getLevelTarget();

        if ((direction == XpPumpDirection.INPUT || direction == XpPumpDirection.KEEP)
            && (targetLevel < player.experienceLevel
                || (targetLevel == player.experienceLevel && player.experience > 0))) {
            dirty |= tryFillTankWithPlayerXp(tank, player, targetLevel);
        } else if ((direction == XpPumpDirection.OUTPUT || direction == XpPumpDirection.KEEP)
            && targetLevel > player.experienceLevel) {
                dirty |= tryGivePlayerXpFromTank(tank, player, targetLevel);
            }
        return dirty;
    }

    /**
     * Iterates the player's held item and four armor pieces, draining xpjuice from the tank to
     * repair items enchanted with EFR Mending. Mirrors the EFR ratio of 1 XP point = 2 durability.
     */
    private boolean tryRepairMendingItems(ITankUpgrade tank, EntityPlayer player) {
        Fluid xpFluid = getXpJuiceFluid();
        if (xpFluid == null) return false;
        FluidStack contents = tank.getContents();
        if (contents == null || contents.amount < XpHelpers.RATIO_MB_PER_XP || contents.getFluid() != xpFluid)
            return false;

        ItemStack[] candidates = new ItemStack[] { player.getCurrentEquippedItem(), player.getEquipmentInSlot(1),
            player.getEquipmentInSlot(2), player.getEquipmentInSlot(3), player.getEquipmentInSlot(4) };

        boolean dirty = false;
        for (ItemStack stack : candidates) {
            if (stack == null) continue;
            if (stack.getItemDamage() <= 0) continue;
            if (stack.getItem() == null || !stack.getItem()
                .isRepairable()) continue;
            if (XpHelpers.getMendingLevel(stack) <= 0) continue;

            FluidStack current = tank.getContents();
            if (current == null || current.amount < XpHelpers.RATIO_MB_PER_XP) break;

            int xpAvailable = current.amount / XpHelpers.RATIO_MB_PER_XP;
            // 1 XP point repairs 2 durability points (matches EFR Mending behavior).
            int xpNeeded = (stack.getItemDamage() + 1) / 2;
            int xpToUse = Math.min(xpAvailable, xpNeeded);
            if (xpToUse <= 0) continue;

            FluidStack drained = tank.drain(xpToUse * XpHelpers.RATIO_MB_PER_XP, true);
            if (drained == null || drained.amount < XpHelpers.RATIO_MB_PER_XP) break;
            int xpUsed = drained.amount / XpHelpers.RATIO_MB_PER_XP;
            int newDamage = Math.max(0, stack.getItemDamage() - xpUsed * 2);
            stack.setItemDamage(newDamage);
            dirty = true;
        }
        return dirty;
    }

    private boolean tryFillTankWithPlayerXp(ITankUpgrade tank, EntityPlayer player, int stopAtLevel) {
        FluidStack tankContents = tank.getContents();
        Fluid xpFluid = getXpJuiceFluid();
        if (xpFluid == null) return false;
        if (tankContents != null && tankContents.getFluid() != xpFluid) return false;

        int playerXp = XpHelpers.getPlayerTotalExperience(player);
        int floorXp = XpHelpers.getExperienceForLevel(stopAtLevel);
        int xpAvailable = playerXp - floorXp;
        if (xpAvailable <= 0) return false;

        int liquidWanted = XpHelpers.experienceToLiquid(xpAvailable);
        if (liquidWanted <= 0) return false;

        FluidStack toFill = new FluidStack(xpFluid, liquidWanted);
        int filledSim = tank.fill(toFill, false);
        // Align to whole-XP units (20 mB each) so we never lose fractional XP to integer
        // truncation in the mB <-> XP conversion.
        int filledAligned = (filledSim / XpHelpers.RATIO_MB_PER_XP) * XpHelpers.RATIO_MB_PER_XP;
        if (filledAligned <= 0) return false;
        int xpToRemove = filledAligned / XpHelpers.RATIO_MB_PER_XP;
        tank.fill(new FluidStack(xpFluid, filledAligned), true);
        XpHelpers.addPlayerExperience(player, -xpToRemove);
        return true;
    }

    private boolean tryGivePlayerXpFromTank(ITankUpgrade tank, EntityPlayer player, int stopAtLevel) {
        Fluid xpFluid = getXpJuiceFluid();
        if (xpFluid == null) return false;
        FluidStack tankContents = tank.getContents();
        if (tankContents == null || tankContents.amount <= 0 || tankContents.getFluid() != xpFluid) return false;

        int playerXp = XpHelpers.getPlayerTotalExperience(player);
        int targetXp = XpHelpers.getExperienceForLevel(stopAtLevel);
        int xpNeeded = targetXp - playerXp;
        if (xpNeeded <= 0) return false;

        int liquidWanted = XpHelpers.experienceToLiquid(xpNeeded);
        if (liquidWanted <= 0) return false;

        // Align to whole-XP units (20 mB each) so we never give the player fractional XP.
        int requested = Math.min(liquidWanted, tankContents.amount);
        int aligned = (requested / XpHelpers.RATIO_MB_PER_XP) * XpHelpers.RATIO_MB_PER_XP;
        if (aligned <= 0) return false;
        FluidStack drained = tank.drain(aligned, false);
        if (drained == null || drained.amount < XpHelpers.RATIO_MB_PER_XP) return false;
        int drainAligned = (drained.amount / XpHelpers.RATIO_MB_PER_XP) * XpHelpers.RATIO_MB_PER_XP;
        if (drainAligned <= 0) return false;
        tank.drain(drainAligned, true);
        XpHelpers.addPlayerExperience(player, drainAligned / XpHelpers.RATIO_MB_PER_XP);
        return true;
    }

    @Override
    public void takeAllFromPlayer(EntityPlayer player) {
        ITankUpgrade tank = findTank();
        if (tank == null) return;
        tryFillTankWithPlayerXp(tank, player, 0);
    }

    @Override
    public void giveAllToPlayer(EntityPlayer player) {
        ITankUpgrade tank = findTank();
        if (tank == null) return;
        tryGivePlayerXpFromTank(tank, player, MAX_LEVEL_TARGET);
    }

    @Override
    public void takeLevelsFromPlayer(EntityPlayer player) {
        ITankUpgrade tank = findTank();
        if (tank == null) return;
        int target = Math.max(0, player.experienceLevel - getLevelsToStore());
        tryFillTankWithPlayerXp(tank, player, target);
    }

    @Override
    public void giveLevelsToPlayer(EntityPlayer player) {
        ITankUpgrade tank = findTank();
        if (tank == null) return;
        int target = player.experienceLevel + getLevelsToTake();
        tryGivePlayerXpFromTank(tank, player, target);
    }
}
