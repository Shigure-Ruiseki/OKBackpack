package ruiseki.okbackpack.compat.thaumcraft;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;

public final class ThaumcraftChargeHelper {

    private ThaumcraftChargeHelper() {}

    public static boolean chargeStacks(Iterable<ItemStack> stacks, VisConsumer consumer) {
        if (stacks == null || consumer == null) return false;

        boolean chargedAny = false;
        for (ItemStack stack : stacks) {
            chargedAny |= chargeStack(stack, consumer);
        }
        return chargedAny;
    }

    public static boolean chargeStack(ItemStack stack, VisConsumer consumer) {
        return chargeStackInternal(stack, consumer, 5);
    }

    public static boolean canChargeStack(ItemStack stack) {
        return ThaumcraftHelpers.isWand(stack);
    }

    public static AspectBudget createPrimalBudget(int amountPerAspect) {
        return new AspectBudget(amountPerAspect);
    }

    public static AspectBudget createBudget(Map<Aspect, Integer> amountByAspect) {
        return new AspectBudget(amountByAspect);
    }

    public static boolean chargeStacks(Iterable<ItemStack> stacks, AspectBudget budget) {
        if (stacks == null || budget == null || budget.isExhausted()) return false;

        boolean chargedAny = false;
        for (ItemStack stack : stacks) {
            if (budget.isExhausted()) break;
            chargedAny |= chargeStack(stack, budget);
        }
        return chargedAny;
    }

    public static boolean chargeStack(ItemStack stack, AspectBudget budget) {
        if (budget == null || budget.isExhausted()) return false;
        return chargeStackInternal(stack, budget::consume, Integer.MAX_VALUE);
    }

    public static boolean chargeStackInternal(ItemStack stack, VisConsumer consumer, int maxRequestPerAspect) {
        if (!ThaumcraftHelpers.isWand(stack) || consumer == null) return false;

        ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        AspectList aspectsWithRoom = wand.getAspectsWithRoom(stack);
        if (aspectsWithRoom == null || aspectsWithRoom.size() <= 0) return false;

        boolean charged = false;
        int maxVis = wand.getMaxVis(stack);

        for (Aspect aspect : aspectsWithRoom.getAspects()) {
            if (aspect == null) continue;

            int currentVis = wand.getVis(stack, aspect);
            int requested = Math.min(maxRequestPerAspect, maxVis - currentVis);
            if (requested <= 0) continue;

            int drained = consumer.consume(aspect, requested);
            if (drained <= 0) continue;

            wand.addRealVis(stack, aspect, drained, true);
            charged = true;
        }

        return charged;
    }

    @FunctionalInterface
    public interface VisConsumer {

        int consume(Aspect aspect, int amount);
    }

    public static final class AspectBudget {

        public final Map<Aspect, Integer> remainingByAspect = new LinkedHashMap<>();

        public AspectBudget(int amountPerAspect) {
            int normalized = Math.max(0, amountPerAspect);
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (aspect != null) {
                    remainingByAspect.put(aspect, normalized);
                }
            }
        }

        public AspectBudget(Map<Aspect, Integer> amountByAspect) {
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (aspect != null) {
                    int amount = amountByAspect == null ? 0 : amountByAspect.getOrDefault(aspect, 0);
                    remainingByAspect.put(aspect, Math.max(0, amount));
                }
            }
        }

        public int consume(Aspect aspect, int amount) {
            if (aspect == null || amount <= 0) return 0;

            int remaining = remainingByAspect.getOrDefault(aspect, 0);
            if (remaining <= 0) return 0;

            int drained = Math.min(remaining, amount);
            remainingByAspect.put(aspect, remaining - drained);
            return drained;
        }

        public boolean isExhausted() {
            for (int remaining : remainingByAspect.values()) {
                if (remaining > 0) {
                    return false;
                }
            }
            return true;
        }
    }

}
