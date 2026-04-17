package ruiseki.okbackpack.compat.thaumcraft;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public final class ThaumcraftChargeHelper {

    private static final Map<Class<?>, ChargeMethods> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final ChargeMethods NO_METHODS = new ChargeMethods(null, null, null, null);

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
        if (stack == null || stack.getItem() == null || consumer == null) return false;

        ChargeMethods methods = resolveMethods(stack);
        if (methods == NO_METHODS) return false;

        try {
            AspectList aspectsWithRoom = (AspectList) methods.getAspectsWithRoom.invoke(stack.getItem(), stack);
            if (aspectsWithRoom == null || aspectsWithRoom.size() <= 0) return false;

            boolean charged = false;
            int maxVis = ((Number) methods.getMaxVis.invoke(stack.getItem(), stack)).intValue();

            for (Aspect aspect : aspectsWithRoom.getAspects()) {
                if (aspect == null) continue;

                int currentVis = ((Number) methods.getVis.invoke(stack.getItem(), stack, aspect)).intValue();
                int requested = Math.min(5, maxVis - currentVis);
                if (requested <= 0) continue;

                int drained = consumer.consume(aspect, requested);
                if (drained <= 0) continue;

                methods.addRealVis.invoke(stack.getItem(), stack, aspect, drained, true);
                charged = true;
            }

            return charged;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static ChargeMethods resolveMethods(ItemStack stack) {
        return METHOD_CACHE.computeIfAbsent(
            stack.getItem()
                .getClass(),
            ThaumcraftChargeHelper::findMethods);
    }

    private static ChargeMethods findMethods(Class<?> itemClass) {
        try {
            return new ChargeMethods(
                itemClass.getMethod("getAspectsWithRoom", ItemStack.class),
                itemClass.getMethod("getMaxVis", ItemStack.class),
                itemClass.getMethod("getVis", ItemStack.class, Aspect.class),
                itemClass.getMethod("addRealVis", ItemStack.class, Aspect.class, int.class, boolean.class));
        } catch (NoSuchMethodException ignored) {
            return NO_METHODS;
        }
    }

    @FunctionalInterface
    public interface VisConsumer {

        int consume(Aspect aspect, int amount);
    }

    private static final class ChargeMethods {

        private final Method getAspectsWithRoom;
        private final Method getMaxVis;
        private final Method getVis;
        private final Method addRealVis;

        private ChargeMethods(Method getAspectsWithRoom, Method getMaxVis, Method getVis, Method addRealVis) {
            this.getAspectsWithRoom = getAspectsWithRoom;
            this.getMaxVis = getMaxVis;
            this.getVis = getVis;
            this.addRealVis = addRealVis;
        }
    }
}
