package ruiseki.backpack.helpers;

import java.util.*;
import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.backpack.backpack.ItemBackpack;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.MinecraftHelpers;

public class PlayerInventoryProvider {

    public static final String MAIN_INVENTORY = "main";
    public static final String ARMOR_INVENTORY = "armor";

    private final Map<String, PlayerInventoryHandler> playerInventoryHandlers = new LinkedHashMap<>();
    private final List<String> renderedHandlers = new ArrayList<>();

    private static final PlayerInventoryProvider serverProvider = new PlayerInventoryProvider();
    private static final PlayerInventoryProvider clientProvider = new PlayerInventoryProvider();

    public static PlayerInventoryProvider get() {
        if (MinecraftHelpers.isClientSide()) {
            return clientProvider;
        } else {
            return serverProvider;
        }
    }

    private PlayerInventoryProvider() {
        addPlayerInventoryHandler(
            MAIN_INVENTORY,
            gameTime -> PlayerInventoryHandler.SINGLE_IDENTIFIER,
            (player, identifier) -> player.inventory.getSizeInventory(),
            (player, identifier, slot) -> player.inventory.getStackInSlot(slot),
            true,
            false,
            false,
            false);
        addPlayerInventoryHandler(
            ARMOR_INVENTORY,
            gameTime -> PlayerInventoryHandler.SINGLE_IDENTIFIER,
            (player, identifier) -> 1,
            (player, identifier, slot) -> player.inventory.armorInventory[1],
            false,
            true,
            false,
            true);
    }

    public void addPlayerInventoryHandler(String name, Function<Long, Set<String>> identifiersGetter,
        PlayerInventoryHandler.SlotCountGetter slotCountGetter, PlayerInventoryHandler.SlotStackGetter slotStackGetter,
        boolean visibleInGui, boolean rendered, boolean ownRenderer, boolean accessibleByAnotherPlayer) {
        Map<String, PlayerInventoryHandler> temp = new LinkedHashMap<>(playerInventoryHandlers);
        playerInventoryHandlers.clear();
        playerInventoryHandlers.put(
            name,
            new PlayerInventoryHandler(
                identifiersGetter,
                slotCountGetter,
                slotStackGetter,
                visibleInGui,
                ownRenderer,
                accessibleByAnotherPlayer));
        playerInventoryHandlers.putAll(temp);

        if (rendered) {
            ArrayList<String> tempRendered = new ArrayList<>(renderedHandlers);
            renderedHandlers.clear();
            renderedHandlers.add(name);
            renderedHandlers.addAll(tempRendered);
        }
    }

    private Map<String, PlayerInventoryHandler> getPlayerInventoryHandlers() {
        return playerInventoryHandlers;
    }

    public Optional<PlayerInventoryHandler> getPlayerInventoryHandler(String name) {
        return Optional.ofNullable(getPlayerInventoryHandlers().get(name));
    }

    public void runOnBackpacks(EntityPlayer player, BackpackInventorySlotConsumer backpackInventorySlotConsumer) {
        runOnBackpacks(player, backpackInventorySlotConsumer, false);
    }

    public void runOnBackpacks(EntityPlayer player, BackpackInventorySlotConsumer backpackInventorySlotConsumer,
        boolean onlyAccessibleByAnotherPlayer) {
        for (Map.Entry<String, PlayerInventoryHandler> entry : getPlayerInventoryHandlers().entrySet()) {
            PlayerInventoryHandler invHandler = entry.getValue();
            if (onlyAccessibleByAnotherPlayer && !invHandler.isAccessibleByAnotherPlayer()) {
                continue;
            }

            for (String identifier : invHandler.getIdentifiers(player.worldObj.getWorldTime())) {
                for (int slot = 0; slot < invHandler.getSlotCount(player, identifier); slot++) {
                    ItemStack slotStack = invHandler.getStackInSlot(player, identifier, slot);
                    if (!ItemHelpers.isEmpty(slotStack) && slotStack.getItem() instanceof ItemBackpack
                        && backpackInventorySlotConsumer.accept(slotStack, entry.getKey(), identifier, slot)) {
                        return;
                    }
                }
            }
        }
    }

    public interface BackpackInventorySlotConsumer {

        boolean accept(ItemStack backpack, String inventoryHandlerName, String identifier, int slot);
    }
}
