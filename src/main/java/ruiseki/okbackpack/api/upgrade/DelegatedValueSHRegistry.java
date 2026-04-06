package ruiseki.okbackpack.api.upgrade;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.PacketBuffer;

import ruiseki.okbackpack.client.gui.syncHandler.value.DelegatedValueSH;

public class DelegatedValueSHRegistry {

    public interface ServerHandler {

        void handle(DelegatedValueSH<?> value, PacketBuffer buf) throws IOException;
    }

    public interface ClientHandler {

        void handle(DelegatedValueSH<?> value, PacketBuffer buf) throws IOException;
    }

    private static final Map<Integer, ServerHandler> SERVER_HANDLERS = new HashMap<>();
    private static final Map<Integer, ClientHandler> CLIENT_HANDLERS = new HashMap<>();
    private static final Map<String, Integer> NAME_TO_ID = new HashMap<>();

    public static int registerServer(String name, ServerHandler handler) {
        int id = name.hashCode();
        SERVER_HANDLERS.put(id, handler);
        NAME_TO_ID.put(name, id);
        return id;
    }

    public static int registerClient(String name, ClientHandler handler) {
        int id = name.hashCode();
        CLIENT_HANDLERS.put(id, handler);
        NAME_TO_ID.put(name, id);
        return id;
    }

    public static int getId(String name) {
        return NAME_TO_ID.getOrDefault(name, name.hashCode());
    }

    public static void handleServer(DelegatedValueSH<?> value, int id, PacketBuffer buf) throws IOException {
        ServerHandler handler = SERVER_HANDLERS.get(id);
        if (handler != null) handler.handle(value, buf);
    }

    public static void handleClient(DelegatedValueSH<?> value, int id, PacketBuffer buf) throws IOException {
        ClientHandler handler = CLIENT_HANDLERS.get(id);
        if (handler != null) handler.handle(value, buf);
    }

    public static boolean isServerEmpty() {
        return SERVER_HANDLERS.isEmpty();
    }

    public static boolean isClientEmpty() {
        return CLIENT_HANDLERS.isEmpty();
    }

    public static Map<Integer, ServerHandler> getServerHandlers() {
        return SERVER_HANDLERS;
    }

    public static Map<Integer, ClientHandler> getClientHandlers() {
        return CLIENT_HANDLERS;
    }
}
