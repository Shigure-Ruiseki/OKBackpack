package ruiseki.okbackpack.common.init;

import net.minecraft.util.ResourceLocation;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.tier.BackpackTier;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.init.IInitListener;

public class TierRegistries implements IInitListener {

    public static final String LEATHER = "leather_backpack";
    public static final String IRON = "iron_backpack";
    public static final String GOLD = "gold_backpack";
    public static final String DIAMOND = "diamond_backpack";
    public static final String OBSIDIAN = "obsidian_backpack";

    @Override
    public void onInit(Step step) {
        if (step != Step.PREINIT) return;

        TierRegistry.register(
            new BackpackTier(
                LEATHER,
                ModConfig.leatherBackpackSlots,
                ModConfig.leatherUpgradeSlots,
                new ResourceLocation(Reference.MOD_ID, "leather_clips")));

        TierRegistry.register(
            new BackpackTier(
                IRON,
                ModConfig.ironBackpackSlots,
                ModConfig.ironUpgradeSlots,
                new ResourceLocation(Reference.MOD_ID, "iron_clips")));

        TierRegistry.register(
            new BackpackTier(
                GOLD,
                ModConfig.goldBackpackSlots,
                ModConfig.goldUpgradeSlots,
                new ResourceLocation(Reference.MOD_ID, "gold_clips")));

        TierRegistry.register(
            new BackpackTier(
                DIAMOND,
                ModConfig.diamondBackpackSlots,
                ModConfig.diamondUpgradeSlots,
                new ResourceLocation(Reference.MOD_ID, "diamond_clips")));

        TierRegistry.register(
            new BackpackTier(
                OBSIDIAN,
                ModConfig.obsidianBackpackSlots,
                ModConfig.obsidianUpgradeSlots,
                new ResourceLocation(Reference.MOD_ID, "obsidian_clips")));
    }
}
