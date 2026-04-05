package ruiseki.okbackpack.common.item.stack;

import java.util.List;
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
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemStackUpgrade extends ItemUpgrade<StackUpgradeWrapper> {

    @SideOnly(Side.CLIENT)
    protected IIcon tier1, tier2, tier3, tier4, tierOmega;

    public ItemStackUpgrade() {
        super("stack_upgrade");
        setMaxStackSize(1);
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem.findConflictSlots(wrapper, targetSlot, ItemStackUpgrade.class);
        if (conflicts.length >= 3) {
            return UpgradeSlotChangeResult.fail(
                "gui.backpack.error.add.only_x_upgrades_allowed",
                conflicts,
                3,
                upgradeStack.getDisplayName(),
                wrapper.getDisplayName());
        }
        return UpgradeSlotChangeResult.success();
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tabs, List<ItemStack> list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
        list.add(new ItemStack(item, 1, 3));
        list.add(new ItemStack(item, 1, 4));
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        switch (meta) {
            case 1:
                return super.getUnlocalizedName(stack) + ".gold";
            case 2:
                return super.getUnlocalizedName(stack) + ".diamond";
            case 3:
                return super.getUnlocalizedName(stack) + ".obsidian";
            case 4:
                return super.getUnlocalizedName(stack) + ".omega";
            default:
                return super.getUnlocalizedName(stack) + ".iron";
        }
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return switch (meta) {
            case 1 -> tier2;
            case 2 -> tier3;
            case 3 -> tier4;
            case 4 -> tierOmega;
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
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.stack_upgrade", multiplier(itemstack)));
    }

    @Override
    public StackUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new StackUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    public static int multiplier(ItemStack stack) {
        return switch (stack.getItemDamage()) {
            case 1 -> ModConfig.stackUpgradeTier2Mul;
            case 2 -> ModConfig.stackUpgradeTier3Mul;
            case 3 -> ModConfig.stackUpgradeTier4Mul;
            case 4 -> ModConfig.stackUpgradeTierOmegaMul;
            default -> ModConfig.stackUpgradeTier1Mul;
        };
    }
}
