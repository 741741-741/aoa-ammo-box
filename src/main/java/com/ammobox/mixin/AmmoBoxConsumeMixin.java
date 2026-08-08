package com.ammobox.mixin;

import com.ammobox.AmmoBoxMod;
import com.ammobox.item.AmmoBoxItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.tslat.aoa3.common.registration.AoAEnchantments;
import net.tslat.aoa3.content.entity.projectile.gun.BaseBullet;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;

@Mixin(BaseGun.class)
public class AmmoBoxConsumeMixin {

    @Inject(method = "findAndConsumeAmmo", at = @At("HEAD"), cancellable = false, remap = false)
    private void onFindAndConsumeAmmo(LivingEntity shooter, ItemStack gunStack, Hand hand,
                                       CallbackInfoReturnable<BaseBullet> cir) {
        if (!(shooter instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) shooter;
        BaseGun gun = (BaseGun)(Object)this;

        Item ammoItem = gun.getAmmoItem();
        int needed = 1 + EnchantmentHelper.getEnchantmentLevel(AoAEnchantments.GREED.get(), gunStack);

        ItemStack pouchStack = ItemStack.EMPTY;
        LazyOptional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        if (curiosOpt.isPresent()) {
            ICuriosItemHandler curios = curiosOpt.orElse(null);
            if (curios != null) {
                Optional<ICurioStacksHandler> backHandlerOpt = curios.getStacksHandler("back");
                if (backHandlerOpt.isPresent()) {
                    ICurioStacksHandler backHandler = backHandlerOpt.get();
                    IItemHandler backInv = backHandler.getStacks();
                    for (int i = 0; i < backInv.getSlots(); i++) {
                        ItemStack slotStack = backInv.getStackInSlot(i);
                        if (slotStack.getItem() instanceof AmmoBoxItem) {
                            pouchStack = slotStack;
                            break;
                        }
                    }
                }
            }
        }
        if (pouchStack.isEmpty()) {
            AmmoBoxMod.LOGGER.info("AmmoBox: No ammo box found in back slot");
            return;
        }

        LazyOptional<IItemHandler> cap = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (!cap.isPresent()) {
            AmmoBoxMod.LOGGER.warn("AmmoBox: No item handler capability");
            return;
        }
        IItemHandler handler = cap.orElse(null);
        if (!(handler instanceof ItemStackHandler)) {
            AmmoBoxMod.LOGGER.warn("AmmoBox: Handler not ItemStackHandler");
            return;
        }
        ItemStackHandler stackHandler = (ItemStackHandler) handler;

        int totalFound = 0;
        for (int slot = 0; slot < stackHandler.getSlots(); slot++) {
            ItemStack stack = stackHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == ammoItem) {
                totalFound += stack.getCount();
            }
        }
        if (totalFound < needed) {
            AmmoBoxMod.LOGGER.info("AmmoBox: Not enough ammo, found {} need {}", totalFound, needed);
            return;
        }

        ItemStack ammoToAdd = new ItemStack(ammoItem, needed);
        boolean added = player.inventory.addItemStackToInventory(ammoToAdd);
        AmmoBoxMod.LOGGER.info("AmmoBox: Adding {} ammo to inventory, success: {}", needed, added);
        if (!added) {
            return;
        }

        int toRemove = needed;
        for (int slot = 0; slot < stackHandler.getSlots(); slot++) {
            ItemStack stack = stackHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == ammoItem) {
                int take = Math.min(toRemove, stack.getCount());
                stack.shrink(take);
                toRemove -= take;
                if (toRemove == 0) break;
            }
        }

        CompoundNBT tag = pouchStack.getOrCreateTag();
        tag.put("Inventory", stackHandler.serializeNBT());
        AmmoBoxMod.LOGGER.info("AmmoBox: Ammo transfer complete");
    }
}
