package ruiseki.okbackpack.mixins;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.config.ModConfig;

@RequiredArgsConstructor
public enum Mixins implements IMixins {

    SPIDER_CLIMB(new MixinBuilder("Spider climb mixin").addCommonMixins("Minecraft.MixinEntityLivingBase")
        .setApplyIf(() -> ModConfig.enableTravelersUpgrades)
        .setPhase(Phase.EARLY)),

    THAUMCRAFT(new MixinBuilder("Thaumcraft Mixin")
        .addCommonMixins("Thaumcraft.MixinTileMagicWorkbench", "Thaumcraft.MixinTileMagicWorkbenchCharger")
        .setPhase(Phase.LATE)
        .setApplyIf(() -> ModConfig.enableArcaneCraftingUpgrade)
        .addRequiredMod(Mods.Thaumcraft)),

    ;

    @Getter
    private final MixinBuilder builder;

    Mixins(Side side, String... mixins) {
        this.builder = new MixinBuilder().addSidedMixins(side, mixins)
            .setPhase(Phase.EARLY);
    }
}
