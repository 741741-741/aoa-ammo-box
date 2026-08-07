package com.ammobox.item;

import com.ammobox.AmmoBoxMod;
import com.ammobox.inventory.AmmoBoxInventory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmmoBoxItem extends Item implements ICurio {
    public AmmoBoxItem() {
        super(new Properties().maxStackSize(1).maxDamage(500));
    }

    public AmmoBoxItem(Properties properties) {
        super(properties.maxStackSize(1).maxDamage(500));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        AmmoBoxMod.LOGGER.info("AmmoBoxItem.canEquip called, slot: {}", slotContext.getIdentifier());
        return slotContext.getIdentifier().equals("back");
    }

    @Override
    public void curioTick(String identifier, int index, LivingEntity livingEntity) {}

    @Override
    public void onEquip(String identifier, int index, LivingEntity livingEntity) {}

    @Override
    public void onUnequip(String identifier, int index, LivingEntity livingEntity) {}

    @Override
    public boolean canRightClickEquip() { return true; }

    @Override
    public DropRule getDropRule(LivingEntity livingEntity) { return DropRule.DEFAULT; }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier) {
        return HashMultimap.create();
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        AmmoBoxMod.LOGGER.info("AmmoBoxItem.initCapabilities called");
        return new ICapabilityProvider() {
            private final AmmoBoxInventory inventory = new AmmoBoxInventory(stack);
            private final LazyOptional<ICurio> curioOptional = LazyOptional.of(() -> AmmoBoxItem.this);

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                AmmoBoxMod.LOGGER.info("getCapability called, cap: {}", cap.getName());
                if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    AmmoBoxMod.LOGGER.info("Providing inventory capability");
                    return inventory.getCapability(cap, side);
                }
                if (cap == CuriosCapability.ITEM) {
                    AmmoBoxMod.LOGGER.info("Providing Curios capability (self)");
                    return curioOptional.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(h -> {
                    if (h instanceof AmmoBoxInventory) {
                        ((AmmoBoxInventory) h).save();
                    }
                });
        return stack.getOrCreateTag();
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            stack.setTag(nbt);
            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .ifPresent(h -> {
                        if (h instanceof AmmoBoxInventory) {
                            ((AmmoBoxInventory) h).save();
                        }
                    });
        }
        super.readShareTag(stack, nbt);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }
}
