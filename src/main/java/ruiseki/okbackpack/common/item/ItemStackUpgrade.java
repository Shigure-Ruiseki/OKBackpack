package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapper;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemStackUpgrade extends ItemUpgrade<UpgradeWrapper> {

    @SideOnly(Side.CLIENT)
    protected IIcon tier1, tier2, tier3, tier4, tierOmega;

    public ItemStackUpgrade() {
        super("stack_upgrade");
        setMaxStackSize(1);
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
                return super.getUnlocalizedName(stack) + ".Gold";
            case 2:
                return super.getUnlocalizedName(stack) + ".Diamond";
            case 3:
                return super.getUnlocalizedName(stack) + ".Netherite";
            case 4:
                return super.getUnlocalizedName(stack) + ".Omega";
            default:
                return super.getUnlocalizedName(stack) + ".Iron";
        }
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        switch (meta) {
            case 1:
                return tier2;
            case 2:
                return tier3;
            case 3:
                return tier4;
            case 4:
                return tierOmega;
            default:
                return tier1;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister reg) {
        tier1 = reg.registerIcon(Reference.PREFIX_MOD + "backpack/stack_upgrade_tier_1");
        tier2 = reg.registerIcon(Reference.PREFIX_MOD + "backpack/stack_upgrade_tier_2");
        tier3 = reg.registerIcon(Reference.PREFIX_MOD + "backpack/stack_upgrade_tier_3");
        tier4 = reg.registerIcon(Reference.PREFIX_MOD + "backpack/stack_upgrade_tier_4");
        tierOmega = reg.registerIcon(Reference.PREFIX_MOD + "backpack/stack_upgrade_tier_omega");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.stack_upgrade"));
    }

    public int multiplier(ItemStack stack) {
        switch (stack.getItemDamage()) {
            case 1:
                return ModConfig.stackUpgradeTier2Mul;
            case 2:
                return ModConfig.stackUpgradeTier3Mul;
            case 3:
                return ModConfig.stackUpgradeTier4Mul;
            case 4:
                return ModConfig.stackUpgradeTierOmegaMul;
            default:
                return ModConfig.stackUpgradeTier1Mul;
        }
    }
}
