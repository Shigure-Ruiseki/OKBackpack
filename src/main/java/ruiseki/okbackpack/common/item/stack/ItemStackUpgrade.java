package ruiseki.okbackpack.common.item.stack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.api.wrapper.IBatteryUpgrade;
import ruiseki.okbackpack.api.wrapper.IStackSizeUpgrade;
import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okbackpack.common.item.battery.BatteryUpgradeWrapper;
import ruiseki.okbackpack.common.item.tank.ItemTankUpgrade;
import ruiseki.okbackpack.common.item.tank.TankUpgradeWrapper;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemStackUpgrade extends ItemUpgrade<StackUpgradeWrapper> {

    public static final int META_UPGRADE_T1 = 0;
    public static final int META_UPGRADE_T2 = 1;
    public static final int META_UPGRADE_T3 = 2;
    public static final int META_UPGRADE_T4 = 3;
    public static final int META_UPGRADE_OMEGA = 4;
    public static final int META_UPGRADE_STARTER = 5;
    public static final int META_DOWNGRADE_T1 = 6;
    public static final int META_DOWNGRADE_T2 = 7;
    public static final int META_DOWNGRADE_T3 = 8;

    @SideOnly(Side.CLIENT)
    protected IIcon tier1, tier2, tier3, tier4, tierOmega, tierStarter, downTier1, downTier2, downTier3;

    public ItemStackUpgrade() {
        super("stack_upgrade");
        setMaxStackSize(1);
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem.findConflictSlots(wrapper, targetSlot, ItemStackUpgrade.class);
        if (conflicts.length >= 3) {
            return UpgradeSlotChangeResult.failOnlyXAllowed(
                conflicts,
                3,
                LangHelpers.localize("item.stack_upgrade.name"),
                wrapper.getDisplayName());
        }
        if (isDowngrade(upgradeStack)) {
            return checkCapacityConflict(wrapper, -1, upgradeStack);
        }
        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tabs, List<ItemStack> list) {
        list.add(new ItemStack(item, 1, META_UPGRADE_T1));
        list.add(new ItemStack(item, 1, META_UPGRADE_T2));
        list.add(new ItemStack(item, 1, META_UPGRADE_T3));
        list.add(new ItemStack(item, 1, META_UPGRADE_T4));
        list.add(new ItemStack(item, 1, META_UPGRADE_OMEGA));
        list.add(new ItemStack(item, 1, META_UPGRADE_STARTER));
        list.add(new ItemStack(item, 1, META_DOWNGRADE_T1));
        list.add(new ItemStack(item, 1, META_DOWNGRADE_T2));
        list.add(new ItemStack(item, 1, META_DOWNGRADE_T3));
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        return switch (meta) {
            case META_UPGRADE_T2 -> super.getUnlocalizedName(stack) + ".gold";
            case META_UPGRADE_T3 -> super.getUnlocalizedName(stack) + ".diamond";
            case META_UPGRADE_T4 -> super.getUnlocalizedName(stack) + ".obsidian";
            case META_UPGRADE_OMEGA -> super.getUnlocalizedName(stack) + ".omega";
            case META_UPGRADE_STARTER -> super.getUnlocalizedName(stack) + ".starter";
            case META_DOWNGRADE_T1 -> super.getUnlocalizedName(stack) + ".down_t1";
            case META_DOWNGRADE_T2 -> super.getUnlocalizedName(stack) + ".down_t2";
            case META_DOWNGRADE_T3 -> super.getUnlocalizedName(stack) + ".down_t3";
            default -> super.getUnlocalizedName(stack) + ".iron";
        };
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return switch (meta) {
            case META_UPGRADE_T2 -> tier2;
            case META_UPGRADE_T3 -> tier3;
            case META_UPGRADE_T4 -> tier4;
            case META_UPGRADE_OMEGA -> tierOmega;
            case META_UPGRADE_STARTER -> tierStarter;
            case META_DOWNGRADE_T1 -> downTier1;
            case META_DOWNGRADE_T2 -> downTier2;
            case META_DOWNGRADE_T3 -> downTier3;
            default -> tier1;
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister reg) {
        tier1 = reg.registerIcon(Reference.PREFIX_MOD + "stack_upgrade_tier_1");
        tier2 = reg.registerIcon(Reference.PREFIX_MOD + "stack_upgrade_tier_2");
        tier3 = reg.registerIcon(Reference.PREFIX_MOD + "stack_upgrade_tier_3");
        tier4 = reg.registerIcon(Reference.PREFIX_MOD + "stack_upgrade_tier_4");
        tierOmega = reg.registerIcon(Reference.PREFIX_MOD + "stack_upgrade_tier_omega");
        tierStarter = reg.registerIcon(Reference.PREFIX_MOD + "stack_upgrade_starter_tier");
        downTier1 = reg.registerIcon(Reference.PREFIX_MOD + "stack_downgrade_tier_1");
        downTier2 = reg.registerIcon(Reference.PREFIX_MOD + "stack_downgrade_tier_2");
        downTier3 = reg.registerIcon(Reference.PREFIX_MOD + "stack_downgrade_tier_3");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        if (isDowngrade(itemstack)) {
            list.add(LangHelpers.localize("tooltip.backpack.stack_downgrade", divisor(itemstack)));
        } else {
            list.add(LangHelpers.localize("tooltip.backpack.stack_upgrade", formatMultiplier(multiplier(itemstack))));
        }
    }

    @Override
    public StackUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new StackUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    public static double multiplier(ItemStack stack) {
        return switch (stack.getItemDamage()) {
            case META_UPGRADE_T2 -> ModConfig.stackUpgradeTier2Mul;
            case META_UPGRADE_T3 -> ModConfig.stackUpgradeTier3Mul;
            case META_UPGRADE_T4 -> ModConfig.stackUpgradeTier4Mul;
            case META_UPGRADE_OMEGA -> ModConfig.stackUpgradeTierOmegaMul;
            case META_UPGRADE_STARTER -> ModConfig.stackUpgradeStarterMul;
            case META_DOWNGRADE_T1, META_DOWNGRADE_T2, META_DOWNGRADE_T3 -> divisorFraction(stack);
            default -> ModConfig.stackUpgradeTier1Mul;
        };
    }

    public static boolean isDowngrade(ItemStack stack) {
        int meta = stack.getItemDamage();
        return meta == META_DOWNGRADE_T1 || meta == META_DOWNGRADE_T2 || meta == META_DOWNGRADE_T3;
    }

    public static double divisorFraction(ItemStack stack) {
        return 1.0 / divisor(stack);
    }

    public static int divisor(ItemStack stack) {
        return switch (stack.getItemDamage()) {
            case META_DOWNGRADE_T2 -> ModConfig.stackDowngradeTier2Div;
            case META_DOWNGRADE_T3 -> ModConfig.stackDowngradeTier3Div;
            default -> ModConfig.stackDowngradeTier1Div;
        };
    }

    public static UpgradeSlotChangeResult checkCapacityConflict(IStorageWrapper wrapper, int excludeSlot,
        ItemStack downgradeStack) {

        double additiveTotal = 0;
        double downgradeProduct = 1;
        boolean hasAdditive = false;
        for (Map.Entry<Integer, IStackSizeUpgrade> e : wrapper.gatherCapabilityUpgrades(IStackSizeUpgrade.class)
            .entrySet()) {
            if (e.getKey() == excludeSlot) continue;
            IStackSizeUpgrade mod = e.getValue();
            if (mod.isDowngrade()) {
                downgradeProduct *= mod.getMultiplier();
            } else {
                additiveTotal += mod.getMultiplier();
                hasAdditive = true;
            }
        }

        downgradeProduct *= divisorFraction(downgradeStack);
        double newRaw = (hasAdditive ? additiveTotal : 1) * downgradeProduct;
        double effectiveNew = newRaw <= 0 ? 1.0 : newRaw;
        String formattedMul = formatMultiplier(effectiveNew);

        List<Integer> inventoryConflicts = new ArrayList<>();
        for (int i = 0; i < wrapper.getSlots(); i++) {
            ItemStack stack = wrapper.getStackInSlot(i);
            if (stack == null) continue;

            double rawLimit = stack.getMaxStackSize() * effectiveNew;
            int newLimit = rawLimit >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(1, Math.ceil(rawLimit));
            if (stack.stackSize > newLimit) {
                inventoryConflicts.add(i);
            }
        }

        if (!inventoryConflicts.isEmpty()) {
            return UpgradeSlotChangeResult.failStackLowMultiplier(
                inventoryConflicts.stream()
                    .mapToInt(Integer::intValue)
                    .toArray(),
                formattedMul);
        }

        for (Map.Entry<Integer, IBatteryUpgrade> entry : wrapper.gatherCapabilityUpgrades(IBatteryUpgrade.class)
            .entrySet()) {
            IBatteryUpgrade battery = entry.getValue();
            int newMaxEnergy = (int) (BatteryUpgradeWrapper.BASE_ENERGY_PER_SLOT * wrapper.getStackHandler()
                .getSlots() * effectiveNew);
            if (battery.getEnergyStored() > newMaxEnergy) {
                return UpgradeSlotChangeResult.failStorageCapacityLow(new int[] { entry.getKey() }, formattedMul);
            }
        }

        for (Map.Entry<Integer, ITankUpgrade> entry : wrapper.gatherCapabilityUpgrades(ITankUpgrade.class)
            .entrySet()) {
            ITankUpgrade tank = entry.getValue();
            int newMaxFluid = (int) (ItemTankUpgrade.SLOTS_NEEDED * TankUpgradeWrapper.BASE_CAPACITY_PER_SLOT
                * effectiveNew);
            if (tank.getContents() != null && tank.getContents().amount > newMaxFluid) {
                return UpgradeSlotChangeResult.failStorageCapacityLow(new int[] { entry.getKey() }, formattedMul);
            }
        }

        return UpgradeSlotChangeResult.success();
    }

    public static String formatMultiplier(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return new BigDecimal(value).stripTrailingZeros()
            .toPlainString();
    }
}
