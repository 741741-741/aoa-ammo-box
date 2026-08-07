package com.ammobox.event;

import com.ammobox.container.AmmoBoxContainer;
import com.ammobox.item.AmmoBoxItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemEventHandler {
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        Hand hand = event.getHand();
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof AmmoBoxItem) {
            event.setCanceled(true);
            if (!world.isRemote) {
                NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                        (id, inv, p) -> new AmmoBoxContainer(id, inv, hand),
                        new TranslationTextComponent("container.aoaammobox.ammo_box")
                ), buf -> buf.writeEnumValue(hand));
            }
        }
    }
}
