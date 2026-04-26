package ruiseki.okbackpack.mixins;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.config.ModConfig;

@RequiredArgsConstructor
public enum Mixins implements IMixins {

    SPIDER_CLIMB(new MixinBuilder("Spider climb mixin").addCommonMixins("minecraft.MixinEntityLivingBase")
        .setApplyIf(() -> ModConfig.enableTravelersUpgrades)
        .setPhase(Phase.EARLY)),

    GHAST_NEUTRAL(new MixinBuilder("Ghast neutral mixin").addCommonMixins("minecraft.MixinEntityGhast")
        .setApplyIf(() -> ModConfig.enableTravelersUpgrades)
        .setPhase(Phase.EARLY)),

    THAUMCRAFT(new MixinBuilder("Thaumcraft Mixin")
        .addCommonMixins("thaumcraft.MixinTileMagicWorkbench", "thaumcraft.MixinTileMagicWorkbenchCharger")
        .setPhase(Phase.LATE)
        .setApplyIf(() -> ModConfig.enableArcaneCraftingUpgrade)
        .addRequiredMod(Mods.Thaumcraft)),

    GUI_BACKPACK_OPENER(new MixinBuilder("Backpack GUI opener")
        .addSidedMixins(
            Side.CLIENT,
            "minecraft.MixinGuiContainerBackpackOpener",
            "modularui2.MixinItemSlotBackpackOpener")
        .setPhase(Phase.EARLY)),

    INVENTORY_INTERACTION(new MixinBuilder("Backpack inventory interaction")
        .addCommonMixins("minecraft.MixinNetHandlerPlayServerBackpackInteraction")
        .addSidedMixins(
            Side.CLIENT,
            "modularui2.MixinItemSlotBackpackInteraction",
            "minecraft.MixinGuiContainerBackpackInteraction",
            "minecraft.MixinPlayerControllerMPBackpackInteraction")
        .setApplyIf(() -> ModConfig.enableBackpackInventoryInteraction)
        .setPhase(Phase.EARLY)),

    ;

    @Getter
    private final MixinBuilder builder;

    Mixins(Side side, String... mixins) {
        this.builder = new MixinBuilder().addSidedMixins(side, mixins)
            .setPhase(Phase.EARLY);
    }
}
