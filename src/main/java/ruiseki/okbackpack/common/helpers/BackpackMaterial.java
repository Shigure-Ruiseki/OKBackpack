package ruiseki.okbackpack.common.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import lombok.Getter;
import lombok.Setter;
import ruiseki.okbackpack.common.helpers.BackpackMaterial.SettingsSection.MemorySlotEntry;
import ruiseki.okcore.helper.JsonNBTHelpers;
import ruiseki.okcore.json.AbstractJsonMaterial;
import ruiseki.okcore.json.ItemJson;

/**
 * Material class representing a Backpack template JSON.
 */
public class BackpackMaterial extends AbstractJsonMaterial {

    @Getter
    @Setter
    private String backpackTier;
    @Getter
    @Setter
    private String mainColor = "#FFFFFF";
    @Getter
    @Setter
    private String accentColor = "#FFFFFF";
    @Getter
    private final List<BackpackEntry> inventory = new ArrayList<>();
    @Getter
    private final List<BackpackEntry> upgrade = new ArrayList<>();
    @Getter
    @Setter
    private boolean searchBackpack = true;
    @Getter
    @Setter
    private SettingsSection settings;

    @Override
    public void read(JsonObject json) {
        this.backpackTier = getString(json, "BackpackTier", "Leather");
        this.mainColor = getString(json, "MainColor", "#FFFFFF");
        this.accentColor = getString(json, "AccentColor", "#FFFFFF");
        this.searchBackpack = getBoolean(json, "SearchBackpack", true);

        this.settings = null;
        if (json.has("Settings") && json.get("Settings")
            .isJsonObject()) {
            SettingsSection section = new SettingsSection();
            section.read(json.getAsJsonObject("Settings"));
            this.settings = section;
        }

        // Inventory
        this.inventory.clear();
        if (json.has("Inventory") && json.get("Inventory")
            .isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray("Inventory")) {
                if (e.isJsonObject()) {
                    BackpackEntry entry = new BackpackEntry();
                    entry.read(e.getAsJsonObject());
                    this.inventory.add(entry);
                }
            }
        }

        // Upgrade
        this.upgrade.clear();
        if (json.has("Upgrade") && json.get("Upgrade")
            .isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray("Upgrade")) {
                if (e.isJsonObject()) {
                    BackpackEntry entry = new BackpackEntry();
                    entry.read(e.getAsJsonObject());
                    this.upgrade.add(entry);
                }
            }
        }

        captureUnknownProperties(
            json,
            "BackpackTier",
            "MainColor",
            "AccentColor",
            "Inventory",
            "Upgrade",
            "SearchBackpack",
            "Settings");
    }

    @Override
    public void write(JsonObject json) {
        json.addProperty("BackpackTier", backpackTier);
        json.addProperty("MainColor", mainColor);
        json.addProperty("AccentColor", accentColor);
        json.addProperty("SearchBackpack", searchBackpack);

        if (settings != null) {
            JsonObject settingsJson = new JsonObject();
            settings.write(settingsJson);
            json.add("Settings", settingsJson);
        }

        if (!inventory.isEmpty()) {
            JsonArray arr = new JsonArray();
            for (BackpackEntry entry : inventory) {
                JsonObject obj = new JsonObject();
                entry.write(obj);
                arr.add(obj);
            }
            json.add("Inventory", arr);
        }

        if (!upgrade.isEmpty()) {
            JsonArray arr = new JsonArray();
            for (BackpackEntry entry : upgrade) {
                JsonObject obj = new JsonObject();
                entry.write(obj);
                arr.add(obj);
            }
            json.add("Upgrade", arr);
        }

        writeUnknownProperties(json);
    }

    @Override
    public boolean validate() {
        if (backpackTier == null) {
            logValidationError("BackpackTier is missing");
            return false;
        }
        return true;
    }

    public static class BackpackEntry {

        public int slot;
        public String id;
        public int count = 1;
        public NBTTagCompound nbt;

        public void read(JsonObject json) {
            this.slot = json.has("Slot") ? json.get("Slot")
                .getAsInt() : 0;
            this.id = json.has("id") ? json.get("id")
                .getAsString() : null;
            this.count = json.has("Count") ? json.get("Count")
                .getAsInt() : 1;

            if (json.has("nbt") && json.get("nbt")
                .isJsonObject()) {
                this.nbt = JsonNBTHelpers.jsonToNBT(json.getAsJsonObject("nbt"));
            }
        }

        public void write(JsonObject json) {
            json.addProperty("Slot", slot);
            if (id != null) json.addProperty("id", id);
            if (count != 1) json.addProperty("Count", count);
            if (nbt != null) {
                json.add("nbt", JsonNBTHelpers.nbtToJSON(nbt));
            }
        }

        public ItemStack toItemStack() {
            ItemJson itemJson = new ItemJson();
            itemJson.name = id;
            itemJson.amount = count;

            ItemStack stack = ItemJson.resolveItemStack(itemJson);
            if (stack == null) return null;
            if (nbt != null) {
                stack.setTagCompound((NBTTagCompound) nbt.copy());
            }
            return stack;
        }

        public static BackpackEntry fromItemStack(int slot, ItemStack stack) {
            if (stack == null) return null;
            BackpackEntry entry = new BackpackEntry();
            entry.slot = slot;
            ItemJson itemJson = ItemJson.parseItemStack(stack);
            if (itemJson != null) {
                // Check if it has metadata in ItemJson
                if (itemJson.meta != 0) {
                    entry.id = itemJson.name + ":" + itemJson.meta;
                } else {
                    entry.id = itemJson.name;
                }
            }
            entry.count = stack.stackSize;
            if (stack.hasTagCompound()) {
                entry.nbt = (NBTTagCompound) stack.getTagCompound()
                    .copy();
            }
            return entry;
        }
    }

    public int parseMainColor() {
        return parseHexColor(mainColor);
    }

    public int parseAccentColor() {
        return parseHexColor(accentColor);
    }

    private int parseHexColor(String hex) {
        if (hex == null || !hex.startsWith("#")) return 0xFFFFFF;
        try {
            return Integer.parseInt(hex.substring(1), 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    public static String toHexColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public boolean hasSettings() {
        return settings != null;
    }

    public BackpackSettingsTemplate toSettingsTemplate(int slotCount) {
        if (settings == null) {
            return new BackpackSettingsTemplate(slotCount);
        }
        return settings.toTemplate(slotCount);
    }

    public void setSettingsFromTemplate(BackpackSettingsTemplate template) {
        this.settings = template == null ? null : SettingsSection.fromTemplate(template);
    }

    public static class SettingsSection {

        @Getter
        @Setter
        private boolean keepTab = true;
        @Getter
        @Setter
        private boolean shiftClickIntoOpenTab = false;
        @Getter
        @Setter
        private boolean keepSearchPhrase = false;
        @Getter
        @Setter
        private boolean lockBackpack = false;
        @Getter
        @Setter
        private boolean usePlayerSettings = false;
        @Getter
        @Setter
        private int noSortColorIndex = 0;
        @Getter
        private final List<MemorySlotEntry> memorySlots = new ArrayList<>();
        @Getter
        private final List<Integer> noSortSlots = new ArrayList<>();

        public void read(JsonObject json) {
            keepTab = !json.has("KeepTab") || json.get("KeepTab")
                .getAsBoolean();
            shiftClickIntoOpenTab = json.has("ShiftClickIntoOpenTab") && json.get("ShiftClickIntoOpenTab")
                .getAsBoolean();
            keepSearchPhrase = json.has("KeepSearchPhrase") && json.get("KeepSearchPhrase")
                .getAsBoolean();
            lockBackpack = json.has("LockBackpack") && json.get("LockBackpack")
                .getAsBoolean();
            usePlayerSettings = json.has("UsePlayerSettings") && json.get("UsePlayerSettings")
                .getAsBoolean();
            noSortColorIndex = json.has("NoSortColorIndex") ? json.get("NoSortColorIndex")
                .getAsInt() : 0;

            memorySlots.clear();
            if (json.has("MemorySlots") && json.get("MemorySlots")
                .isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray("MemorySlots")) {
                    if (element.isJsonObject()) {
                        MemorySlotEntry entry = new MemorySlotEntry();
                        entry.read(element.getAsJsonObject());
                        memorySlots.add(entry);
                    }
                }
            }

            noSortSlots.clear();
            if (json.has("NoSortSlots") && json.get("NoSortSlots")
                .isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray("NoSortSlots")) {
                    if (element.isJsonPrimitive()) {
                        noSortSlots.add(element.getAsInt());
                    }
                }
            }
        }

        public void write(JsonObject json) {
            json.addProperty("KeepTab", keepTab);
            json.addProperty("ShiftClickIntoOpenTab", shiftClickIntoOpenTab);
            json.addProperty("KeepSearchPhrase", keepSearchPhrase);
            json.addProperty("LockBackpack", lockBackpack);
            json.addProperty("UsePlayerSettings", usePlayerSettings);
            json.addProperty("NoSortColorIndex", noSortColorIndex);

            JsonArray memoryArray = new JsonArray();
            for (MemorySlotEntry entry : memorySlots) {
                JsonObject entryJson = new JsonObject();
                entry.write(entryJson);
                memoryArray.add(entryJson);
            }
            json.add("MemorySlots", memoryArray);

            JsonArray noSortArray = new JsonArray();
            for (Integer slot : noSortSlots) {
                noSortArray.add(new JsonPrimitive(slot));
            }
            json.add("NoSortSlots", noSortArray);
        }

        public BackpackSettingsTemplate toTemplate(int slotCount) {
            BackpackSettingsTemplate template = new BackpackSettingsTemplate(slotCount);
            template.setKeepTab(keepTab);
            template.setShiftClickIntoOpenTab(shiftClickIntoOpenTab);
            template.setKeepSearchPhrase(keepSearchPhrase);
            template.setLockBackpack(lockBackpack);
            template.setUsePlayerSettings(usePlayerSettings);
            template.setNoSortColorIndex(noSortColorIndex);

            for (MemorySlotEntry entry : memorySlots) {
                if (entry.slot >= 0 && entry.slot < slotCount) {
                    template.setMemorySlot(entry.slot, entry.toItemStack(), entry.isRespectNBT());
                }
            }
            for (Integer slot : noSortSlots) {
                if (slot != null && slot >= 0 && slot < slotCount) {
                    template.setLockedSlot(slot, true);
                }
            }
            return template;
        }

        public static SettingsSection fromTemplate(BackpackSettingsTemplate template) {
            SettingsSection section = new SettingsSection();
            section.keepTab = template.isKeepTab();
            section.shiftClickIntoOpenTab = template.isShiftClickIntoOpenTab();
            section.keepSearchPhrase = template.isKeepSearchPhrase();
            section.lockBackpack = template.isLockBackpack();
            section.usePlayerSettings = template.isUsePlayerSettings();
            section.noSortColorIndex = template.getNoSortColorIndex();

            for (int i = 0; i < template.getMemorizedStacks()
                .size(); i++) {
                ItemStack memorized = template.getMemorizedStacks()
                    .get(i);
                if (memorized != null) {
                    MemorySlotEntry entry = new MemorySlotEntry();
                    entry.slot = i;
                    BackpackEntry stackEntry = BackpackEntry.fromItemStack(i, memorized);
                    if (stackEntry != null) {
                        entry.id = stackEntry.id;
                        entry.count = stackEntry.count;
                        entry.nbt = stackEntry.nbt == null ? null : (NBTTagCompound) stackEntry.nbt.copy();
                    }
                    entry.setRespectNBT(
                        Boolean.TRUE.equals(
                            template.getRespectNbt()
                                .get(i)));
                    section.memorySlots.add(entry);
                }

                if (Boolean.TRUE.equals(
                    template.getLockedSlots()
                        .get(i))) {
                    section.noSortSlots.add(i);
                }
            }
            return section;
        }

        public static class MemorySlotEntry extends BackpackEntry {

            @Getter
            @Setter
            private boolean respectNBT;

            @Override
            public void read(JsonObject json) {
                super.read(json);
                respectNBT = json.has("RespectNBT") && json.get("RespectNBT")
                    .getAsBoolean();
            }

            @Override
            public void write(JsonObject json) {
                super.write(json);
                json.addProperty("RespectNBT", respectNBT);
            }
        }
    }
}
