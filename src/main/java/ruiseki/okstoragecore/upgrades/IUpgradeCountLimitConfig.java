package ruiseki.okstoragecore.upgrades;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public interface IUpgradeCountLimitConfig {

    int getMaxUpgradesPerStorage(String storageType, @Nullable ResourceLocation upgradeRegistryName);

    int getMaxUpgradesInGroupPerStorage(String storageType, UpgradeGroup upgradeGroup);
}
