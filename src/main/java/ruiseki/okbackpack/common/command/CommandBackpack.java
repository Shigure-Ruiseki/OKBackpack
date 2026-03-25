package ruiseki.okbackpack.common.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.helpers.BackpackJsonReader;
import ruiseki.okbackpack.common.helpers.BackpackJsonWriter;
import ruiseki.okbackpack.common.helpers.BackpackMaterial;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okcore.command.CommandMod;
import ruiseki.okcore.init.ModBase;

public class CommandBackpack extends CommandMod {

    private final File backpackDir;

    public CommandBackpack(ModBase mod, Map<String, ICommand> subCommands) {
        super(mod, subCommands);
        this.backpackDir = new File("config/" + Reference.MOD_ID + "/dump");
        if (!backpackDir.exists()) {
            backpackDir.mkdirs();
        }

        addSubcommands("give", new CommandGive());
        addSubcommands("export", new CommandExport());
        addSubcommands("import", new CommandImport());
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP required
    }

    @Override
    public void processCommandHelp(ICommandSender sender, String[] args) throws CommandException {
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Usage:"));
        sender.addChatMessage(
            new ChatComponentText(EnumChatFormatting.WHITE + "  /okbackpack give <name> - Give backpack template"));
        sender.addChatMessage(
            new ChatComponentText(
                EnumChatFormatting.WHITE + "  /okbackpack export <name> - Export held backpack to JSON"));
        sender.addChatMessage(
            new ChatComponentText(
                EnumChatFormatting.WHITE + "  /okbackpack import <name> - Import JSON template to held backpack"));
    }

    private List<String> getJsonFiles() {
        List<String> files = new ArrayList<>();
        if (backpackDir.exists() && backpackDir.isDirectory()) {
            File[] list = backpackDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (list != null) {
                for (File f : list) {
                    files.add(
                        f.getName()
                            .replace(".json", ""));
                }
            }
        }
        return files;
    }

    private class CommandGive extends CommandBase {

        @Override
        public String getCommandName() {
            return "give";
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/ok backpack give <name>";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (args.length < 1) throw new WrongUsageException(getCommandUsage(sender));
            EntityPlayer player = CommandBase.getCommandSenderAsPlayer(sender);

            File file = new File(backpackDir, args[0] + ".json");
            if (!file.exists()) {
                throw new CommandException("Template not found: " + args[0]);
            }

            try {
                BackpackMaterial mat = new BackpackJsonReader(file).read();
                if (mat == null) throw new CommandException("Failed to read template");

                ItemStack backpack = createBackpackFromMaterial(mat);
                if (!player.inventory.addItemStackToInventory(backpack)) {
                    player.dropPlayerItemWithRandomChoice(backpack, false);
                }
                sender.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.GREEN + "Gave backpack template: " + args[0]));
            } catch (IOException e) {
                throw new CommandException("Error reading file: " + e.getMessage());
            }
        }

        @Override
        public List addTabCompletionOptions(ICommandSender sender, String[] args) {
            if (args.length == 1) {
                return getListOfStringsMatchingLastWord(args, getJsonFiles().toArray(new String[0]));
            }
            return null;
        }
    }

    private class CommandExport extends CommandBase {

        @Override
        public String getCommandName() {
            return "export";
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/ok backpack export <name>";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (args.length < 1) throw new WrongUsageException(getCommandUsage(sender));
            EntityPlayer player = CommandBase.getCommandSenderAsPlayer(sender);
            ItemStack held = player.getHeldItem();

            if (held == null || !(held.getItem() instanceof BlockBackpack.ItemBackpack)) {
                throw new CommandException("You must hold a backpack to export it!");
            }

            BackpackWrapper wrapper = new BackpackWrapper(held, (BlockBackpack.ItemBackpack) held.getItem());
            wrapper.readFromItem();
            BackpackMaterial mat = createMaterialFromWrapper(wrapper);

            File file = new File(backpackDir, args[0] + ".json");
            try {
                new BackpackJsonWriter(file).write(mat);
                sender.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.GREEN + "Exported backpack to: " + file.getPath()));
            } catch (Exception e) {
                throw new CommandException("Error writing file: " + e.getMessage());
            }
        }
    }

    private class CommandImport extends CommandBase {

        @Override
        public String getCommandName() {
            return "import";
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/ok backpack import <name>";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (args.length < 1) throw new WrongUsageException(getCommandUsage(sender));
            EntityPlayer player = CommandBase.getCommandSenderAsPlayer(sender);
            ItemStack held = player.getHeldItem();

            if (held == null || !(held.getItem() instanceof BlockBackpack.ItemBackpack)) {
                throw new CommandException("You must hold a backpack to import to it!");
            }

            File file = new File(backpackDir, args[0] + ".json");
            if (!file.exists()) {
                throw new CommandException("Template not found: " + args[0]);
            }

            try {
                BackpackMaterial mat = new BackpackJsonReader(file).read();
                if (mat == null) throw new CommandException("Failed to read template");

                BackpackWrapper wrapper = new BackpackWrapper(held, (BlockBackpack.ItemBackpack) held.getItem());
                wrapper.readFromItem();
                applyMaterialToWrapper(mat, wrapper);
                wrapper.writeToItem();

                sender.addChatMessage(
                    new ChatComponentText(
                        EnumChatFormatting.GREEN + "Imported template " + args[0] + " to held backpack"));
            } catch (IOException e) {
                throw new CommandException("Error reading file: " + e.getMessage());
            }
        }

        @Override
        public List addTabCompletionOptions(ICommandSender sender, String[] args) {
            if (args.length == 1) {
                return getListOfStringsMatchingLastWord(args, getJsonFiles().toArray(new String[0]));
            }
            return null;
        }
    }

    // Helper methods for conversion between Material and Wrapper
    private ItemStack createBackpackFromMaterial(BackpackMaterial mat) {
        String tier = mat.getBackpackTier();
        ItemStack stack = null;
        for (ModBlocks block : ModBlocks.VALUES) {
            if (block.name()
                .toLowerCase()
                .contains(tier.toLowerCase())) {
                stack = block.newItemStack();
                break;
            }
        }
        if (stack == null) stack = ModBlocks.BACKPACK_BASE.newItemStack();

        BackpackWrapper wrapper = new BackpackWrapper(stack, (BlockBackpack.ItemBackpack) stack.getItem());
        applyMaterialToWrapper(mat, wrapper);
        wrapper.writeToItem();
        return stack;
    }

    private BackpackMaterial createMaterialFromWrapper(BackpackWrapper wrapper) {
        BackpackMaterial mat = new BackpackMaterial();
        // Determine tier name
        String tier = "Base";
        for (ModBlocks block : ModBlocks.VALUES) {
            if (block.getItem() == wrapper.getBackpack()
                .getItem()) {
                tier = block.name()
                    .replace("BACKPACK_", "");
                break;
            }
        }
        mat.setBackpackTier(tier);
        mat.setMainColor(BackpackMaterial.toHexColor(wrapper.getMainColor()));
        mat.setAccentColor(BackpackMaterial.toHexColor(wrapper.getAccentColor()));

        // Inventory
        for (int i = 0; i < wrapper.getBackpackHandler()
            .getSlots(); i++) {
            ItemStack stack = wrapper.getBackpackHandler()
                .getStackInSlot(i);
            if (stack != null) {
                mat.getInventory()
                    .add(BackpackMaterial.BackpackEntry.fromItemStack(i, stack));
            }
        }

        // Upgrades
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            ItemStack stack = wrapper.getUpgradeHandler()
                .getStackInSlot(i);
            if (stack != null) {
                mat.getUpgrade()
                    .add(BackpackMaterial.BackpackEntry.fromItemStack(i, stack));
            }
        }

        return mat;
    }

    private void applyMaterialToWrapper(BackpackMaterial mat, BackpackWrapper wrapper) {
        wrapper.setMainColor(mat.parseMainColor());
        wrapper.setAccentColor(mat.parseAccentColor());

        // Clear existing
        for (int i = 0; i < wrapper.getBackpackHandler()
            .getSlots(); i++)
            wrapper.getBackpackHandler()
                .setStackInSlot(i, null);
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++)
            wrapper.getUpgradeHandler()
                .setStackInSlot(i, null);

        // Set new
        for (BackpackMaterial.BackpackEntry entry : mat.getInventory()) {
            if (entry.slot < wrapper.getBackpackHandler()
                .getSlots()) {
                wrapper.getBackpackHandler()
                    .setStackInSlot(entry.slot, entry.toItemStack());
            }
        }
        for (BackpackMaterial.BackpackEntry entry : mat.getUpgrade()) {
            if (entry.slot < wrapper.getUpgradeHandler()
                .getSlots()) {
                wrapper.getUpgradeHandler()
                    .setStackInSlot(entry.slot, entry.toItemStack());
            }
        }
    }

}
