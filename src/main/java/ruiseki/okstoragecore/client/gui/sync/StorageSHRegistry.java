
package ruiseki.okstoragecore.client.gui.sync;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class StorageSHRegistry {

    @FunctionalInterface
    public interface Handler<S> {

        void handle(S handler, PacketBuffer buf) throws IOException;
    }

    public static final class Entry<S> {

        private final Class<S> type;

        private final Map<ResourceLocation, Handler<S>> serverHandlers = new Object2ObjectOpenHashMap<>();

        private final Map<ResourceLocation, Handler<S>> clientHandlers = new Object2ObjectOpenHashMap<>();

        private Entry(Class<S> type) {
            this.type = type;
        }

        public Entry<S> registerServer(ResourceLocation id, Handler<S> handler) {
            registerUnique(serverHandlers, id, handler);
            getId(id);
            return this;
        }

        public Entry<S> registerClient(ResourceLocation id, Handler<S> handler) {
            registerUnique(clientHandlers, id, handler);
            getId(id);
            return this;
        }

        public Entry<S> register(ResourceLocation id, Handler<S> serverHandler, Handler<S> clientHandler) {
            if (serverHandler != null) {
                registerServer(id, serverHandler);
            }

            if (clientHandler != null) {
                registerClient(id, clientHandler);
            }

            return this;
        }

        private static <S> void registerUnique(Map<ResourceLocation, Handler<S>> handlers, ResourceLocation id,
            Handler<S> handler) {
            if (id == null || handler == null) {
                throw new IllegalArgumentException("Sync handler ID and handler must not be null");
            }

            if (handlers.containsKey(id)) {
                throw new IllegalArgumentException("Duplicate sync handler ID: " + id);
            }

            handlers.put(id, handler);
        }

        private Handler<S> getServerHandler(ResourceLocation id) {
            return serverHandlers.get(id);
        }

        private Handler<S> getClientHandler(ResourceLocation id) {
            return clientHandlers.get(id);
        }

        public Class<S> getType() {
            return type;
        }

        public Map<ResourceLocation, Handler<S>> getServerHandlers() {
            return Collections.unmodifiableMap(serverHandlers);
        }

        public Map<ResourceLocation, Handler<S>> getClientHandlers() {
            return Collections.unmodifiableMap(clientHandlers);
        }
    }

    private static final Map<Class<?>, Entry<?>> REGISTRIES = new Object2ObjectOpenHashMap<>();

    private static final Object2IntOpenHashMap<ResourceLocation> LOCATION_TO_ID = new Object2IntOpenHashMap<>();

    private static final Int2ObjectOpenHashMap<ResourceLocation> ID_TO_LOCATION = new Int2ObjectOpenHashMap<>();

    static {
        LOCATION_TO_ID.defaultReturnValue(-1);
    }

    private StorageSHRegistry() {}

    @SuppressWarnings("unchecked")
    public static synchronized <S> Entry<S> get(Class<S> type) {
        Entry<?> entry = REGISTRIES.get(type);

        if (entry == null) {
            entry = new Entry<>(type);
            REGISTRIES.put(type, entry);
        }

        return (Entry<S>) entry;
    }

    /**
     * Creates a deterministic numeric ID from a ResourceLocation.
     * The same ResourceLocation receives the same ID on both sides,
     * regardless of registration order.
     *
     * Hash collisions are rejected rather than silently overwriting
     * another handler's mapping.
     */
    public static synchronized int getId(ResourceLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("ResourceLocation must not be null");
        }

        int existing = LOCATION_TO_ID.getInt(location);

        if (existing != -1) {
            return existing;
        }

        int id = stableHash(location.toString()) & 0x7FFFFFFF;

        ResourceLocation collision = ID_TO_LOCATION.get(id);

        if (collision != null && !collision.equals(location)) {
            throw new IllegalStateException(
                "Sync handler ID hash collision: " + id + " (" + collision + " and " + location + ")");
        }

        LOCATION_TO_ID.put(location, id);
        ID_TO_LOCATION.put(id, location);

        return id;
    }

    private static int stableHash(String value) {
        // FNV-1a 32-bit hash. Independent of JVM hash randomization.
        int hash = 0x811C9DC5;

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            hash ^= b & 0xFF;
            hash *= 0x01000193;
        }

        return hash;
    }

    public static synchronized ResourceLocation getLocation(int id) {
        return ID_TO_LOCATION.get(id);
    }

    public static void handleServer(Object handler, int id, PacketBuffer buf) throws IOException {
        dispatch(handler, resolveLocation(id), buf, true);
    }

    public static void handleClient(Object handler, int id, PacketBuffer buf) throws IOException {
        dispatch(handler, resolveLocation(id), buf, false);
    }

    private static synchronized ResourceLocation resolveLocation(int id) {
        ResourceLocation location = ID_TO_LOCATION.get(id);

        if (location == null) {
            throw new IllegalArgumentException("Unknown sync handler ID: " + id);
        }

        return location;
    }

    private static void dispatch(Object handler, ResourceLocation id, PacketBuffer buf, boolean server)
        throws IOException {
        if (handler == null) {
            throw new IllegalArgumentException("SyncHandler cannot be null");
        }

        Class<?> type = handler.getClass();

        while (type != null && type != Object.class) {
            Entry<?> entry;

            synchronized (StorageSHRegistry.class) {
                entry = REGISTRIES.get(type);
            }

            if (entry != null) {
                Handler<?> registered = server ? entry.getServerHandler(id) : entry.getClientHandler(id);

                if (registered != null) {
                    invoke(registered, handler, buf);
                    return;
                }
            }

            type = type.getSuperclass();
        }

        throw new IllegalArgumentException(
            "No " + (server ? "server" : "client")
                + " handler registered for "
                + id
                + " on "
                + handler.getClass()
                    .getName());
    }

    @SuppressWarnings("unchecked")
    private static void invoke(Handler<?> handler, Object instance, PacketBuffer buf) throws IOException {
        ((Handler<Object>) handler).handle(instance, buf);
    }
}
