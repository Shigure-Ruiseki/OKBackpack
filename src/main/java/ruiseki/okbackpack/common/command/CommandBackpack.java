package ruiseki.okbackpack.common.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.block.BlockDiamondBackpackConfig;
import ruiseki.okbackpack.common.block.BlockGoldBackpackConfig;
import ruiseki.okbackpack.common.block.BlockIronBackpackConfig;
import ruiseki.okbackpack.common.block.BlockLeatherBackpackConfig;
import ruiseki.okbackpack.common.block.BlockObsidianBackpackConfig;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.common.helpers.BackpackJsonReader;
import ruiseki.okbackpack.common.helpers.BackpackJsonWriter;
import ruiseki.okbackpack.common.helpers.BackpackMaterial;
import ruiseki.okbackpack.common.helpers.BackpackSettingsTemplate;
import ruiseki.okcore.command.CommandMod;
import ruiseki.okcore.init.ModBase;

public class CommandBackpack extends CommandMod {

    private final MinecraftServer server;
    private final File backpackDir;

    public CommandBackpack(ModBase mod, MinecraftServer server) {
        super(mod, "backpack");
        this.server = server;
        this.backpackDir = new File("config/" + Reference.MOD_ID + "/dump");
        if (!backpackDir.exists()) {
            backpackDir.mkdirs();
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP required
    }

    @Override
    public LiteralArgumentBuilder<ICommandSender> make() {
        return super.make()
            // /okbackpack give <player> <name> [count]
            .then(
                LiteralArgumentBuilder.<ICommandSender>literal("give")
                    .then(
                        RequiredArgumentBuilder.<ICommandSender, String>argument("player", StringArgumentType.string())
                            // Suggest online player usernames using injection server instance
                            .suggests((context, suggestionsBuilder) -> {
                                String[] usernames = this.server.getAllUsernames();
                                for (String name : usernames) {
                                    if (name.toLowerCase()
                                        .startsWith(
                                            suggestionsBuilder.getRemaining()
                                                .toLowerCase())) {
                                        suggestionsBuilder.suggest(name);
                                    }
                                }
                                return suggestionsBuilder.buildFuture();
                            })
                            .then(
                                RequiredArgumentBuilder
                                    .<ICommandSender, String>argument("name", StringArgumentType.string())
                                    // Suggest existing JSON templates
                                    .suggests((context, suggestionsBuilder) -> {
                                        for (String file : getJsonFiles()) {
                                            if (file.toLowerCase()
                                                .startsWith(
                                                    suggestionsBuilder.getRemaining()
                                                        .toLowerCase())) {
                                                suggestionsBuilder.suggest(file);
                                            }
                                        }
                                        return suggestionsBuilder.buildFuture();
                                    })
                                    .executes(ctx -> executeGive(ctx, 1)) // Default count to 1
                                    .then(
                                        RequiredArgumentBuilder
                                            .<ICommandSender, Integer>argument(
                                                "count",
                                                IntegerArgumentType.integer(1, 64))
                                            .executes(
                                                ctx -> executeGive(
                                                    ctx,
                                                    IntegerArgumentType.getInteger(ctx, "count")))))))
            // /okbackpack export <name>
            .then(
                LiteralArgumentBuilder.<ICommandSender>literal("export")
                    .then(
                        RequiredArgumentBuilder.<ICommandSender, String>argument("name", StringArgumentType.string())
                            .executes(this::executeExport)))
            // /okbackpack import <name>
            .then(
                LiteralArgumentBuilder.<ICommandSender>literal("import")
                    .then(
                        RequiredArgumentBuilder.<ICommandSender, String>argument("name", StringArgumentType.string())
                            .suggests((context, suggestionsBuilder) -> {
                                for (String file : getJsonFiles()) {
                                    if (file.toLowerCase()
                                        .startsWith(
                                            suggestionsBuilder.getRemaining()
                                                .toLowerCase())) {
                                        suggestionsBuilder.suggest(file);
                                    }
                                }
                                return suggestionsBuilder.buildFuture();
                            })
                            .executes(this::executeImport)));
    }

    @Override
    public int run(CommandContext<ICommandSender> context) {
        context.getSource()
            .addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Usage:"));
        context.getSource()
            .addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.WHITE
                        + "  /okbackpack backpack give <player> <name> [count] - Give backpack template"));
        context.getSource()
            .addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.WHITE + "  /okbackpack backpack export <name> - Export held backpack to JSON"));
        context.getSource()
            .addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.WHITE
                        + "  /okbackpack backpack import <name> - Import JSON template to held backpack"));
        return 1;
    }

    private int executeGive(CommandContext<ICommandSender> ctx, int count) throws CommandSyntaxException {
        ICommandSender sender = ctx.getSource();
        String playerName = StringArgumentType.getString(ctx, "player");
        String template = StringArgumentType.getString(ctx, "name");

        // Use the instance field 'this.server' instead of MinecraftServer.getServer()
        EntityPlayerMP player = this.server.getConfigurationManager()
            .func_152612_a(playerName);
        if (player == null) {
            printErrorToChat(sender, "Player not found: " + playerName);
            return 0;
        }

        File file = new File(backpackDir, template + ".json");
        if (!file.exists()) {
            printErrorToChat(sender, "Template not found: " + template);
            return 0;
        }

        BackpackMaterial mat;
        try {
            mat = new BackpackJsonReader(file).read();
        } catch (IOException e) {
            printErrorToChat(sender, "Error reading file: " + e.getMessage());
            return 0;
        }

        if (mat == null) {
            printErrorToChat(sender, "Failed to read template");
            return 0;
        }

        for (int k = 0; k < count; ++k) {
            ItemStack stack = createBackpackFromMaterial(mat);
            if (!player.inventory.addItemStackToInventory(stack)) {
                player.dropPlayerItemWithRandomChoice(stack, false);
            }
        }

        printLineToChat(
            sender,
            String.format("Gave backpack template %s x%s to %s", template, count, player.getCommandSenderName()));
        return 1;
    }

    private int executeExport(CommandContext<ICommandSender> ctx) throws CommandSyntaxException {
        ICommandSender sender = ctx.getSource();
        if (!(sender instanceof EntityPlayer player)) {
            printErrorToChat(sender, "Only players can execute this command!");
            return 0;
        }

        ItemStack held = player.getHeldItem();
        String name = StringArgumentType.getString(ctx, "name");

        if (!BackpackEntityHelpers.isBackpackStack(held, false)) {
            printErrorToChat(sender, "You must hold a backpack to export it!");
            return 0;
        }

        BackpackWrapper wrapper = new BackpackWrapper(held, (BlockBackpack.ItemBackpack) held.getItem());
        wrapper.readFromItem();
        BackpackMaterial mat = createMaterialFromWrapper(wrapper);

        File file = new File(backpackDir, name + ".json");
        try {
            new BackpackJsonWriter(file).write(mat);
            printLineToChat(sender, EnumChatFormatting.GREEN + "Exported backpack to: " + file.getPath());
        } catch (Exception e) {
            printErrorToChat(sender, "Error writing file: " + e.getMessage());
            return 0;
        }
        return 1;
    }

    private int executeImport(CommandContext<ICommandSender> ctx) throws CommandSyntaxException {
        ICommandSender sender = ctx.getSource();
        if (!(sender instanceof EntityPlayer player)) {
            printErrorToChat(sender, "Only players can execute this command!");
            return 0;
        }

        ItemStack held = player.getHeldItem();
        String name = StringArgumentType.getString(ctx, "name");

        if (!BackpackEntityHelpers.isBackpackStack(held, false)) {
            printErrorToChat(sender, "You must hold a backpack to import to it!");
            return 0;
        }

        File file = new File(backpackDir, name + ".json");
        if (!file.exists()) {
            printErrorToChat(sender, "Template not found: " + name);
            return 0;
        }

        try {
            BackpackMaterial mat = new BackpackJsonReader(file).read();
            if (mat == null) {
                printErrorToChat(sender, "Failed to read template");
                return 0;
            }

            BackpackWrapper wrapper = new BackpackWrapper(held, (BlockBackpack.ItemBackpack) held.getItem());
            wrapper.readFromItem();
            applyMaterialToWrapper(mat, wrapper);
            wrapper.writeToItem();

            printLineToChat(sender, EnumChatFormatting.GREEN + "Imported template " + name + " to held backpack");
        } catch (IOException e) {
            printErrorToChat(sender, "Error reading file: " + e.getMessage());
            return 0;
        }
        return 1;
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
    }// Helper methods for conversion between Material and Wrapper

    private ItemStack createBackpackFromMaterial(BackpackMaterial mat) {
        String tier = mat.getBackpackTier()
            .toLowerCase();
        ItemStack stack = null;
        Block targetBlock;

        switch (tier) {
            case "iron":
                targetBlock = BlockIronBackpackConfig._instance.getInstance();
                break;
            case "gold":
                targetBlock = BlockGoldBackpackConfig._instance.getInstance();
                break;
            case "diamond":
                targetBlock = BlockDiamondBackpackConfig._instance.getInstance();
                break;
            case "obsidian":
                targetBlock = BlockObsidianBackpackConfig._instance.getInstance();
                break;
            case "leather":
            case "base":
            default:
                targetBlock = BlockLeatherBackpackConfig._instance.getInstance();
                break;
        }

        stack = new ItemStack(targetBlock);
        if (stack.getItem() instanceof BlockBackpack.ItemBackpack) {
            BackpackWrapper wrapper = new BackpackWrapper(stack, (BlockBackpack.ItemBackpack) stack.getItem());
            applyMaterialToWrapper(mat, wrapper);
            wrapper.writeToItem();
        } else {
            Item item = Item.getItemFromBlock(targetBlock);
            if (item instanceof BlockBackpack.ItemBackpack) {
                BackpackWrapper wrapper = new BackpackWrapper(stack, (BlockBackpack.ItemBackpack) item);
                applyMaterialToWrapper(mat, wrapper);
                wrapper.writeToItem();
            }
        }

        return stack;
    }

    private BackpackMaterial createMaterialFromWrapper(BackpackWrapper wrapper) {
        BackpackMaterial mat = new BackpackMaterial();
        String tier = "leather";

        Item currentItem = wrapper.backpack.getItem();

        if (currentItem == Item.getItemFromBlock(BlockIronBackpackConfig._instance.getInstance())) {
            tier = "iron";
        } else if (currentItem == Item.getItemFromBlock(BlockGoldBackpackConfig._instance.getInstance())) {
            tier = "gold";
        } else if (currentItem == Item.getItemFromBlock(BlockDiamondBackpackConfig._instance.getInstance())) {
            tier = "diamond";
        } else if (currentItem == Item.getItemFromBlock(BlockObsidianBackpackConfig._instance.getInstance())) {
            tier = "obsidian";
        } else if (currentItem == Item.getItemFromBlock(BlockLeatherBackpackConfig._instance.getInstance())) {
            tier = "leather";
        }

        mat.setBackpackTier(tier);
        mat.setMainColor(BackpackMaterial.toHexColor(wrapper.getMainColor()));
        mat.setAccentColor(BackpackMaterial.toHexColor(wrapper.getAccentColor()));

        for (int i = 0; i < wrapper.getSlots(); i++) {
            ItemStack stack = wrapper.getStackInSlot(i);
            if (stack != null) {
                mat.getInventory()
                    .add(BackpackMaterial.BackpackEntry.fromItemStack(i, stack));
            }
        }

        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            ItemStack stack = wrapper.getUpgradeHandler()
                .getStackInSlot(i);
            if (stack != null) {
                mat.getUpgrade()
                    .add(BackpackMaterial.BackpackEntry.fromItemStack(i, stack));
            }
        }

        mat.setSettingsFromTemplate(BackpackSettingsTemplate.fromWrapper(wrapper));
        return mat;
    }

    private void applyMaterialToWrapper(BackpackMaterial mat, BackpackWrapper wrapper) {
        wrapper.setColors(mat.parseMainColor(), mat.parseAccentColor());

        for (int i = 0; i < wrapper.getSlots(); i++) wrapper.setStackInSlot(i, null);
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            wrapper.getUpgradeHandler()
                .setStackInSlot(i, null);
        }

        for (BackpackMaterial.BackpackEntry entry : mat.getInventory()) {
            if (entry.slot < wrapper.getSlots()) {
                wrapper.setStackInSlot(entry.slot, entry.toItemStack());
            }
        }
        for (BackpackMaterial.BackpackEntry entry : mat.getUpgrade()) {
            if (entry.slot < wrapper.getUpgradeHandler()
                .getSlots()) {
                wrapper.getUpgradeHandler()
                    .setStackInSlot(entry.slot, entry.toItemStack());
            }
        }

        if (mat.hasSettings()) {
            mat.toSettingsTemplate(wrapper.getSlots())
                .applyTo(wrapper);
        }
    }
}
