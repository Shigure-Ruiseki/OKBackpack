package ruiseki.okbackpack.api.tier;

import static ruiseki.okbackpack.common.init.TierRegistries.LEATHER;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;

import cpw.mods.fml.common.registry.GameRegistry;
import ruiseki.okbackpack.Reference;
import ruiseki.okcore.helper.LangHelpers;

public class TierRegistry {

    private static final Map<String, BackpackTier> REGISTRY = Collections.synchronizedMap(new LinkedHashMap<>());

    public static void register(BackpackTier tier) {
        if (tier == null || tier.getId() == null) return;
        REGISTRY.put(tier.getId(), tier);
    }

    public static BackpackTier getTier(String id) {
        BackpackTier tier = REGISTRY.get(id);
        if (tier != null) return tier;

        return getLeatherTier();
    }

    public static boolean isRegistered(String id) {
        return id != null && REGISTRY.containsKey(id);
    }

    private static BackpackTier getLeatherTier() {
        return REGISTRY.get(LEATHER);
    }

    public static String getDisplayName(BackpackTier tier) {
        if (tier == null) return null;

        Block block = getBlock(tier.getId());
        if (block != null) {
            return LangHelpers.localize(block.getUnlocalizedName() + ".name");
        }

        return LangHelpers.localize("tile.blocks." + Reference.MOD_ID + "." + tier.getId() + ".name");
    }

    public static Block getBlock(String tierId) {
        if (tierId == null) return null;

        return GameRegistry.findBlock(Reference.MOD_ID, tierId);
    }

    public static Set<String> getTierIds() {
        return REGISTRY.keySet();
    }

    public static Collection<BackpackTier> getAllTiers() {
        return REGISTRY.values();
    }
}
