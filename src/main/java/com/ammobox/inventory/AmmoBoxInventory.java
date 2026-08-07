package com.ammobox.inventory;

import com.ammobox.item.AmmoBoxItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class AmmoBoxInventory implements ICapabilityProvider {
    public static final int SLOTS = 27;
    private final ItemStackHandler handler;
    private final LazyOptional<ItemStackHandler> optional;
    private final ItemStack pouchStack;

    public AmmoBoxInventory(ItemStack stack) {
        this.pouchStack = stack;
        this.handler = new ItemStackHandler(SLOTS) {
            @Override protected void onContentsChanged(int slot) { save(); }
            @Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (stack.isEmpty()) return false;
                if (stack.getItem() instanceof AmmoBoxItem) return false;
                ResourceLocation reg = stack.getItem().getRegistryName();
                if (reg == null) return false;
                String path = reg.getPath().toLowerCase(Locale.ROOT);
                return path.contains("bullet") || path.contains("slug") ||
                       path.contains("spreadshot") || path.contains("cannonball") ||
                       path.contains("balloon") || path.contains("discharge") ||
                       path.contains("pop_shot");
            }
        };
        this.optional = LazyOptional.of(() -> handler);
        load();
    }

    private void load() {
        CompoundNBT tag = pouchStack.getOrCreateTag();
        if (tag.contains("Inventory")) handler.deserializeNBT(tag.getCompound("Inventory"));
    }

    public void save() {
        CompoundNBT tag = pouchStack.getOrCreateTag();
        tag.put("Inventory", handler.serializeNBT());
    }

    public ItemStackHandler getHandler() { return handler; }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return optional.cast();
        return LazyOptional.empty();
    }
}
