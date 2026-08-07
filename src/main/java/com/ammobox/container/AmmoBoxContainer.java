package com.ammobox.container;

import com.ammobox.init.ModContainers;
import com.ammobox.inventory.AmmoBoxInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AmmoBoxContainer extends Container {
    private final IItemHandler handler;
    private final ItemStack pouchStack;

    public AmmoBoxContainer(int id, PlayerInventory inv, Hand hand) {
        super(ModContainers.AMMO_BOX.get(), id);
        this.pouchStack = inv.player.getHeldItem(hand);
        this.handler = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing capability"));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                addSlot(new SlotItemHandler(handler, index, 8 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    private void saveInventory() {
        pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(h -> {
                    if (h instanceof net.minecraftforge.items.ItemStackHandler) {
                        CompoundNBT tag = pouchStack.getOrCreateTag();
                        tag.put("Inventory", ((net.minecraftforge.items.ItemStackHandler)h).serializeNBT());
                    }
                });
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack copy = slot.getStack().copy();
            if (index < AmmoBoxInventory.SLOTS) {
                if (!this.mergeItemStack(slot.getStack(), AmmoBoxInventory.SLOTS, this.inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                if (!this.mergeItemStack(slot.getStack(), 0, AmmoBoxInventory.SLOTS, false))
                    return ItemStack.EMPTY;
            }
            if (slot.getStack().isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
            saveInventory();
            return copy;
        }
        return ItemStack.EMPTY;
    }

    @Override public boolean canInteractWith(PlayerEntity player) {
        return player.getHeldItemMainhand().getItem() instanceof com.ammobox.item.AmmoBoxItem ||
               player.getHeldItemOffhand().getItem() instanceof com.ammobox.item.AmmoBoxItem;
    }

    @Override public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote) saveInventory();
    }
}
