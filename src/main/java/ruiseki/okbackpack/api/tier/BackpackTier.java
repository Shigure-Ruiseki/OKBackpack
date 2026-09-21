package ruiseki.okbackpack.api.tier;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class BackpackTier {

    private final String id;
    private final int backpackSlots;
    private final int upgradeSlots;
    private final ResourceLocation clipTexturePath;

    public BackpackTier(@NotNull String id, int backpackSlots, int upgradeSlots,
        @NotNull ResourceLocation clipTexturePath) {
        this.id = id;
        this.backpackSlots = backpackSlots;
        this.upgradeSlots = upgradeSlots;
        this.clipTexturePath = clipTexturePath;
    }

    public String getId() {
        return id;
    }

    public int getBackpackSlots() {
        return backpackSlots;
    }

    public int getUpgradeSlots() {
        return upgradeSlots;
    }

    public ResourceLocation getClipTexturePath() {
        return clipTexturePath;
    }
}
