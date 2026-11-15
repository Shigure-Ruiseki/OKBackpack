package ruiseki.okstoragecore.api;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.joml.Vector3d;

import ruiseki.okcore.datastructure.BlockPos;

public interface IDiscHandler<I> {

    Optional<I> getSongInfo(ItemStack itemStack, World level, EntityPlayer player);

    void playDisc(WorldServer serverLevel, EntityPlayer player, BlockPos position, UUID storageUuid,
        ItemStack discItemStack, Runnable onFinished);

    void playDisc(WorldServer serverLevel, EntityPlayer player, Vector3d position, UUID storageUuid,
        ItemStack discItemStack, int entityId, Runnable onFinished);

    Optional<Integer> getMusicLengthInTicks(ItemStack itemStack, World level, EntityPlayer player);

    boolean supports(ItemStack itemStack);

    Optional<ItemStack> getRandomDisc(Random randomSource);

    int getMusicDiscSize();

}
