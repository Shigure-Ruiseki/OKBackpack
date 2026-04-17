package ruiseki.okbackpack.mixins;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ruiseki.okbackpack.compat.Mods;

@RequiredArgsConstructor
public enum Mixins implements IMixins {

    THAUMCRAFT(new MixinBuilder("Thaumcraft Mixin").addCommonMixins("Thaumcraft.MixinTileMagicWorkbenchCharger")
        .setPhase(Phase.LATE)
        .addRequiredMod(Mods.Thaumcraft)),

    ;

    @Getter
    private final MixinBuilder builder;

    Mixins(Side side, String... mixins) {
        this.builder = new MixinBuilder().addSidedMixins(side, mixins)
            .setPhase(Phase.EARLY);
    }
}
