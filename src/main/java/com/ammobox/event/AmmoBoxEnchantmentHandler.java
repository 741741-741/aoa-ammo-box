package com.ammobox.event;

import com.ammobox.item.AmmoBoxItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Map;

public class AmmoBoxEnchantmentHandler {
    private static Enchantment greed = null;
    private static Enchantment getGreed() {
        if (greed == null) greed = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("aoa3", "greed"));
        return greed;
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!(left.getItem() instanceof AmmoBoxItem)) return;
        if (right.isEmpty() || right.getItem() != Items.ENCHANTED_BOOK) return;
        Enchantment g = getGreed();
        if (g == null) return;
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(right);
        if (!enchants.containsKey(g)) return;
        int level = enchants.get(g);
        if (level < 1 || level > 3) return;
        ItemStack result = left.copy();
        result.addEnchantment(g, level);
        event.setOutput(result);
        event.setCost(level * 5);
        event.setMaterialCost(1);
    }
}
