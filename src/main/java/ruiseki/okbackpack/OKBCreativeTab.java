package ruiseki.okbackpack;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okcore.helper.LangHelpers;

public class OKBCreativeTab extends CreativeTabs {

    public static final OKBCreativeTab INSTANCE = new OKBCreativeTab();

    public OKBCreativeTab() {
        super("okBackpack");
    }

    @Override
    public Item getTabIconItem() {
        return ModBlocks.BACKPACK_BASE.getItem();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel() {
        return LangHelpers.localize("creativetab." + getTabLabel());
    }
}
