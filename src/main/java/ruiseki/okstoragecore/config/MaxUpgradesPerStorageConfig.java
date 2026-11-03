package ruiseki.okstoragecore.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeGroup;

public class MaxUpgradesPerStorageConfig implements IUpgradeCountLimitConfig {

    private final Supplier<String> supplier;

    public MaxUpgradesPerStorageConfig(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    private Map<String, Integer> getConfig() {
        Map<String, Integer> configMap = new HashMap<>();
        String config = supplier.get();

        if (config == null || config.trim()
            .isEmpty()) {
            return configMap;
        }

        for (String entry : config.split(";")) {
            String[] parts = entry.trim()
                .split("\\|", 2);

            if (parts.length != 2) continue;

            String name = parts[0].trim();
            if (name.isEmpty()) continue;

            try {
                int max = Integer.parseInt(parts[1].trim());
                if (max >= 0) {
                    configMap.put(name, max);
                }
            } catch (NumberFormatException ignored) {
                // Ignore invalid entries.
            }
        }

        return configMap;
    }

    @Override
    public int getMaxUpgradesPerStorage(String storageType, @Nullable ResourceLocation upgradeRegistryName) {
        if (upgradeRegistryName == null) return Integer.MAX_VALUE;
        return getConfig().getOrDefault(upgradeRegistryName.getResourcePath(), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxUpgradesInGroupPerStorage(String storageType, UpgradeGroup upgradeGroup) {
        if (upgradeGroup == null) return Integer.MAX_VALUE;
        return getConfig().getOrDefault(upgradeGroup.name(), Integer.MAX_VALUE);
    }
}
