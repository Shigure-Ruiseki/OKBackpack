package ruiseki.backpack.backpack;

import net.minecraft.item.Item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.init.TierRegistries;
import ruiseki.okcore.config.extendedconfig.BlockContainerConfig;

public class BlockBackpackTestConfig extends BlockContainerConfig {

    public static BlockBackpackTestConfig _instance = null;

    public BlockBackpackTestConfig() {
        super(
            OKBackpack._instance,
            true,
            "test_backpack",
            null,
            blockConfig -> new BlockBackpack(TierRegistries.OBSIDIAN));
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return ItemBackpack.class;
    }
}
