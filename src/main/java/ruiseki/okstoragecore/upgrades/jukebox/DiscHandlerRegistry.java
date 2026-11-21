package ruiseki.okstoragecore.upgrades.jukebox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okstoragecore.api.IDiscHandler;

public class DiscHandlerRegistry {

    private static final List<IDiscHandler<?>> HANDLERS = new ArrayList<>();

    static {
        registerHandler(new VanillaDiscHandler());
    }

    public static List<IDiscHandler<?>> getHandlers() {
        return HANDLERS;
    }

    public static void registerHandler(IDiscHandler<?> handler) {
        HANDLERS.add(handler);
    }

    public static Optional<IDiscHandler<?>> findHandler(ItemStack itemStack) {
        for (IDiscHandler<?> handler : HANDLERS) {
            if (handler.supports(itemStack)) {
                return Optional.of(handler);
            }
        }
        return Optional.empty();
    }

    public static Optional<Integer> getMusicLengthInTicks(ItemStack itemStack, World level, EntityPlayer player) {
        return findHandler(itemStack).flatMap(handler -> handler.getMusicLengthInTicks(itemStack, level, player));
    }

    public static boolean isSupported(ItemStack itemStack) {
        return findHandler(itemStack).isPresent();
    }

    public static Optional<ItemStack> getRandomDisc(Random rnd) {
        Map<IDiscHandler<?>, Integer> handlerWeightMap = HANDLERS.stream()
            .collect(Collectors.toMap(handler -> handler, IDiscHandler::getMusicDiscSize));
        int totalWeight = handlerWeightMap.values()
            .stream()
            .mapToInt(Integer::intValue)
            .sum();

        int randomWeight = rnd.nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (Map.Entry<IDiscHandler<?>, Integer> entry : handlerWeightMap.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomWeight < cumulativeWeight) {
                return entry.getKey()
                    .getRandomDisc(rnd);
            }
        }
        return Optional.empty();
    }
}
