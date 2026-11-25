package ruiseki.okbackpack.api.upgrade;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.PacketBuffer;

import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;

public class DelegatedStackHandlerSHRegistry {

    public interface ServerHandler {

        void handle(DelegatedStackHandlerSH slot, PacketBuffer buf) throws IOException;
    }

    public interface ClientHandler {

        void handle(DelegatedStackHandlerSH slot, PacketBuffer buf) throws IOException;
    }

    private static final Map<Integer, DelegatedStackHandlerSHRegistry.ServerHandler> SERVER_HANDLERS = new HashMap<>();
    private static final Map<Integer, DelegatedStackHandlerSHRegistry.ClientHandler> CLIENT_HANDLERS = new HashMap<>();
    private static final Map<String, Integer> NAME_TO_ID = new HashMap<>();

    public static int registerServer(String name, DelegatedStackHandlerSHRegistry.ServerHandler handler) {
        int id = name.hashCode();
        SERVER_HANDLERS.put(id, handler);
        NAME_TO_ID.put(name, id);
        return id;
    }

    public static int registerClient(String name, DelegatedStackHandlerSHRegistry.ClientHandler handler) {
        int id = name.hashCode();
        CLIENT_HANDLERS.put(id, handler);
        NAME_TO_ID.put(name, id);
        return id;
    }

    public static int getId(String name) {
        return NAME_TO_ID.getOrDefault(name, name.hashCode());
    }

    public static void handleServer(DelegatedStackHandlerSH slot, int id, PacketBuffer buf) throws IOException {
        DelegatedStackHandlerSHRegistry.ServerHandler handler = SERVER_HANDLERS.get(id);
        if (handler != null) handler.handle(slot, buf);
    }

    public static void handleClient(DelegatedStackHandlerSH slot, int id, PacketBuffer buf) throws IOException {
        DelegatedStackHandlerSHRegistry.ClientHandler handler = CLIENT_HANDLERS.get(id);
        if (handler != null) handler.handle(slot, buf);
    }

    public static boolean isServerEmpty() {
        return SERVER_HANDLERS.isEmpty();

    }

    public static boolean isClientEmpty() {
        return CLIENT_HANDLERS.isEmpty();
    }

    public static Map<Integer, DelegatedStackHandlerSHRegistry.ClientHandler> getClientHandlers() {
        return CLIENT_HANDLERS;
    }

    public static Map<Integer, DelegatedStackHandlerSHRegistry.ServerHandler> getServerHandlers() {
        return SERVER_HANDLERS;
    }

}
