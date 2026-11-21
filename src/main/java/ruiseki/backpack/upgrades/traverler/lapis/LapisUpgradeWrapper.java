package ruiseki.backpack.upgrades.traverler.lapis;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.IPickupXpUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class LapisUpgradeWrapper extends UpgradeWrapperBase<LapisUpgradeWrapper, ItemLapisUpgrade>
    implements ITravelersUpgrade, IPickupXpUpgrade {

    private static final float LUCKY_LAPIS_CHANCE = 0.15F;

    public LapisUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean onPickupXp(PlayerPickupXpEvent event) {
        if (event.entityPlayer.getRNG()
            .nextFloat() < LUCKY_LAPIS_CHANCE) {
            event.orb.xpValue *= 2;
        }
        return false;
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }
}
