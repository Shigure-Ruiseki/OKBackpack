package ruiseki.backpack.backpack;

import net.minecraft.item.Item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.init.TierRegistries;
import ruiseki.okcore.config.extendedconfig.BlockContainerConfig;

public class BlockBackpackConfig extends BlockContainerConfig {

    public static BlockBackpackConfig _instance = null;

    public BlockBackpackConfig() {
        super(
            OKBackpack._instance,
            true,
            "test_backpack",
            null,
            blockConfig -> new BlockBackpack(TierRegistries.LEATHER));
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return ItemBackpack.class;
    }
}
