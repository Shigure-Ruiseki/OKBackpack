package ruiseki.backpack.backpack;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.backpack.helpers.PlayerInventoryProvider;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;

/**
 * This class has logic for deduplicating backpack UUIDs because people duplicating backpack including its UUID seems to
 * happen fairly regularly
 * as well as some mods also add enchants / legit ways to duplicate loot when mobs are killed which can again include
 * backpack
 */
public class UUIDDeduplicator {

    private UUIDDeduplicator() {}

    public static void checkForDuplicateBackpacksAndRemoveTheirUUID(EntityPlayer player, UUID backpackUuid,
        ItemStack backpack) {
        PlayerInventoryProvider.get()
            .runOnBackpacks(player, (otherBackpack, inventoryHandlerName, identifier, slot) -> {
                if (otherBackpack != backpack) {
                    CapabilityHelpers.getCapability(otherBackpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                        .ifPresent(
                            wrapper -> wrapper.getContentsUuid()
                                .ifPresent(uuid -> {
                                    if (uuid.equals(backpackUuid)) {
                                        wrapper.removeContentsUUIDTag();
                                        wrapper.onContentsNbtUpdated();
                                    }
                                }));
                }
                return false;
            });
    }

    public static void dedupeBackpackItemEntityInArea(EntityItem newBackpackItemEntity) {
        CapabilityHelpers
            .getCapability(newBackpackItemEntity.getEntityItem(), CapabilityBackpackWrapperConfig.CAPABILITY)
            .ifPresent(
                newBackpackWrapper -> newBackpackWrapper.getContentsUuid()
                    .ifPresent(
                        backpackId -> dedupeBackpackItemEntityInArea(
                            newBackpackWrapper,
                            newBackpackItemEntity,
                            backpackId)));
    }

    private static void dedupeBackpackItemEntityInArea(IBackpackWrapper newBackpackWrapper,
        EntityItem newBackpackItemEntity, UUID backpackId) {
        AxisAlignedBB searchArea = newBackpackItemEntity.boundingBox.expand(10.0D, 10.0D, 10.0D);
        List<EntityItem> entities = newBackpackItemEntity.worldObj.getEntitiesWithinAABB(EntityItem.class, searchArea);
        for (EntityItem entity : entities) {
            if (entity == newBackpackItemEntity || entity.isDead) {
                continue;
            }

            if (checkEntityBackpackIdMatchAndRemoveIfItDoes(newBackpackWrapper, backpackId, entity)) {
                break;
            }
        }
    }

    private static boolean checkEntityBackpackIdMatchAndRemoveIfItDoes(IBackpackWrapper newBackpackWrapper,
        UUID newBackpackId, EntityItem entity) {
        return CapabilityHelpers.getCapability(entity.getEntityItem(), CapabilityBackpackWrapperConfig.CAPABILITY)
            .resolve()
            .flatMap(IStorageWrapper::getContentsUuid)
            .map(backpackId -> {
                if (backpackId.equals(newBackpackId)) {
                    newBackpackWrapper.removeContentsUUIDTag();
                    newBackpackWrapper.onContentsNbtUpdated();
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
}
