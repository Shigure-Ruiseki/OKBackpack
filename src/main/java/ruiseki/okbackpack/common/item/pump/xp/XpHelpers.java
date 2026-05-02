package ruiseki.okbackpack.common.item.pump.xp;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Optional;
import ganymedes01.etfuturum.ModEnchantments;
import ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions;

/**
 * Helper utilities for converting between vanilla XP points / levels and the {@code xpjuice} fluid
 * volume. Mirrors the math used by Sophisticated Backpacks (1 XP point = 20 mB).
 */
public final class XpHelpers {

    public static final int RATIO_MB_PER_XP = 20;

    private XpHelpers() {}

    public static int experienceToLiquid(float xp) {
        if (xp <= 0) return 0;
        return (int) (xp * RATIO_MB_PER_XP);
    }

    public static float liquidToExperience(int liquid) {
        if (liquid <= 0) return 0f;
        return (float) liquid / RATIO_MB_PER_XP;
    }

    /**
     * Returns the total amount of XP needed to reach the given level from level 0.
     */
    public static int getExperienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level < 16) {
            return level * (12 + level * 2) / 2;
        } else if (level < 31) {
            return (level - 15) * (69 + (level - 15) * 5) / 2 + 315;
        } else {
            return (int) Math.min(Integer.MAX_VALUE, (level - 30L) * (215 + (level - 30) * 9L) / 2 + 1395);
        }
    }

    /**
     * XP needed to advance from the start of the given level to the next one.
     */
    public static int getExperienceLimitOnLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }

    public static int getPlayerTotalExperience(EntityPlayer player) {
        int forLevel = getExperienceForLevel(player.experienceLevel);
        int onCurrent = (int) (player.experience * player.xpBarCap());
        return forLevel + onCurrent;
    }

    public static void setPlayerTotalExperience(EntityPlayer player, int totalXp) {
        if (totalXp < 0) totalXp = 0;
        player.experienceLevel = 0;
        player.experience = 0f;
        player.experienceTotal = 0;
        player.addExperience(totalXp);
    }

    /**
     * Adds the given amount of XP to the player. Negative amounts are subtracted.
     */
    public static void addPlayerExperience(EntityPlayer player, int xpDelta) {
        int total = getPlayerTotalExperience(player) + xpDelta;
        if (total < 0) total = 0;
        setPlayerTotalExperience(player, total);
    }

    public static boolean isMendingEnabled() {
        return readMendingFlag();
    }

    @Optional.Method(modid = "etfuturum")
    private static boolean readMendingFlag() {
        return ConfigEnchantsPotions.enableMending;
    }

    /**
     * Returns the level of the EFR Mending enchantment on the given stack, or {@code 0} when
     * Et-Futurum-Requiem is not loaded or the enchantment is disabled in its config.
     */
    public static int getMendingLevel(ItemStack stack) {
        if (stack == null) return 0;
        return readMendingLevel(stack);
    }

    @Optional.Method(modid = "etfuturum")
    private static int readMendingLevel(ItemStack stack) {
        if (ModEnchantments.mending == null) return 0;
        return EnchantmentHelper.getEnchantmentLevel(ModEnchantments.mending.effectId, stack);
    }
}
