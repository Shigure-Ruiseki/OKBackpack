package ruiseki.okbackpack.api.tier;

import static ruiseki.okbackpack.common.init.TierRegistries.LEATHER;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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

    public static Set<String> getTierIds() {
        return REGISTRY.keySet();
    }

    public static Collection<BackpackTier> getAllTiers() {
        return REGISTRY.values();
    }
}
