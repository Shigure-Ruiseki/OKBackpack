package ruiseki.okstoragecore.config;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.config.IChangedCallback;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeGroup;

public class MaxUpgradesPerStorageConfig implements IUpgradeCountLimitConfig, IChangedCallback {

    private static final Map<String, Integer> maxUpgradesPerStorage = new HashMap<>();

    private static void parseConfig(String config) {
        maxUpgradesPerStorage.clear();

        if (config == null || config.trim()
            .isEmpty()) {
            return;
        }

        for (String entry : config.split(";")) {
            String[] parts = entry.trim()
                .split("\\|", 2);

            if (parts.length != 2) {
                continue;
            }

            String name = parts[0].trim();
            String value = parts[1].trim();

            if (name.isEmpty()) {
                continue;
            }

            try {
                int max = Integer.parseInt(value);

                if (max >= 0) {
                    maxUpgradesPerStorage.put(name, max);
                }
            } catch (NumberFormatException ignored) {
                // Ignore invalid entries.
            }
        }
    }

    @Override
    public int getMaxUpgradesPerStorage(String storageType, @Nullable ResourceLocation upgradeRegistryName) {
        if (upgradeRegistryName == null) return Integer.MAX_VALUE;
        return maxUpgradesPerStorage.getOrDefault(upgradeRegistryName.getResourcePath(), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxUpgradesInGroupPerStorage(String storageType, UpgradeGroup upgradeGroup) {
        if (upgradeGroup == null) return Integer.MAX_VALUE;
        return maxUpgradesPerStorage.getOrDefault(upgradeGroup.name(), Integer.MAX_VALUE);
    }

    @Override
    public void onChanged(Object value) {
        parseConfig(value instanceof String ? (String) value : null);
    }

    @Override
    public void onRegisteredPostInit(Object value) {
        parseConfig(value instanceof String ? (String) value : null);
    }
}
