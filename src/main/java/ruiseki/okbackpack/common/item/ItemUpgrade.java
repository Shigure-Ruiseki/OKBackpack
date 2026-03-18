package ruiseki.okbackpack.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.OKBCreativeTab;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.common.item.wrapper.IUpgradeWrapperFactory;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.item.ItemOK;

public class ItemUpgrade<T extends UpgradeWrapper> extends ItemOK implements IUpgradeWrapperFactory<T> {

    public ItemUpgrade(String name) {
        super(name);
        setNoRepair();
        setTextureName(Reference.PREFIX_MOD + "upgrade_base");
        this.setCreativeTab(OKBCreativeTab.INSTANCE);
    }

    public ItemUpgrade() {
        this("upgrade_base");
    }

    public boolean hasTab() {
        return false;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.upgrade_base"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public T createWrapper(ItemStack stack) {
        return (T) new UpgradeWrapper(stack);
    }
}
