package com.ammobox.item;

import com.ammobox.inventory.AmmoBoxInventory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmmoBoxItem extends Item {
    public AmmoBoxItem(Properties properties) {
        super(properties.maxStackSize(1).maxDamage(500));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ICapabilityProvider() {
            private final AmmoBoxInventory inventory = new AmmoBoxInventory(stack);

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return inventory.getCapability(cap, side);
                }
                // 使用 CuriosApi 提供的辅助方法注册能力
                if (cap == CuriosApi.getCuriosCapability()) {
                    return LazyOptional.of(() -> new ICurio() {
                        @Override
                        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
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
                    }).cast();
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
