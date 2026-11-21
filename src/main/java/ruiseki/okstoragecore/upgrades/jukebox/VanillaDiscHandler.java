package ruiseki.okstoragecore.upgrades.jukebox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.joml.Vector3d;

import cpw.mods.fml.common.registry.GameData;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.RecordHelpers;
import ruiseki.okstoragecore.api.IDiscHandler;

public class VanillaDiscHandler implements IDiscHandler<ItemRecord> {

    private static List<Item> musicDiscs;

    private static Supplier<List<? extends String>> discBlockListGetter = Collections::emptyList;

    @Override
    public Optional<ItemRecord> getSongInfo(ItemStack itemStack, World world, EntityPlayer player) {
        if (ItemHelpers.isEmpty(itemStack)) return Optional.empty();
        Item item = ItemHelpers.getItem(itemStack);
        if (item instanceof ItemRecord) return Optional.of((ItemRecord) item);
        return Optional.empty();
    }

    @Override
    public void playDisc(WorldServer serverWorld, EntityPlayer player, BlockPos position, UUID storageUuid,
        ItemStack discItemStack, Runnable onFinished) {
        getSongInfo(discItemStack, serverWorld, player).ifPresent(
            record -> ServerStorageSoundHandler
                .startPlayingDisc(serverWorld, player, position, storageUuid, record, onFinished));
    }

    @Override
    public void playDisc(WorldServer serverWorld, EntityPlayer player, Vector3d position, UUID storageUuid,
        ItemStack discItemStack, int entityId, Runnable onFinished) {
        getSongInfo(discItemStack, serverWorld, player).ifPresent(
            record -> ServerStorageSoundHandler
                .startPlayingDisc(serverWorld, player, position, storageUuid, entityId, record, onFinished));
    }

    @Override
    public Optional<Integer> getMusicLengthInTicks(ItemStack itemStack, World world, EntityPlayer player) {
        return getSongInfo(itemStack, world, player).map(record -> RecordHelpers.getDurationTicks(player, record));
    }

    @Override
    public boolean supports(ItemStack itemStack) {
        return !ItemHelpers.isEmpty(itemStack) && ItemHelpers.getItem(itemStack) instanceof ItemRecord;
    }

    @Override
    public Optional<ItemStack> getRandomDisc(Random random) {
        List<Item> discs = getMusicDiscs();
        if (discs.isEmpty()) return Optional.empty();
        Item selected = discs.get(random.nextInt(discs.size()));
        return Optional.of(new ItemStack(selected));
    }

    @Override
    public int getMusicDiscSize() {
        return getMusicDiscs().size();
    }

    private static List<Item> getMusicDiscs() {
        if (musicDiscs == null) {
            musicDiscs = new ArrayList<>();
            Set<String> blockedDiscs = new HashSet<>(discBlockListGetter.get());
            for (Object object : Item.itemRegistry) {
                if (!(object instanceof ItemRecord item)) continue;
                String registryName = GameData.getItemRegistry()
                    .getNameForObject(item);
                if (registryName != null && blockedDiscs.contains(registryName)) {
                    continue;
                }
                musicDiscs.add(item);
            }
        }
        return musicDiscs;
    }

    public static void setDiscBlockListGetter(Supplier<List<? extends String>> getter) {
        discBlockListGetter = getter;
        musicDiscs = null;
    }
}
