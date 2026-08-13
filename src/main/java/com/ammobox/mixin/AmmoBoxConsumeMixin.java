package com.ammobox.mixin;

import com.ammobox.item.AmmoBoxItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    @Inject(method = "findAndConsumeAmmo", at = @At("HEAD"), cancellable = true, remap = false)
    private void onFindAndConsumeAmmo(LivingEntity shooter, ItemStack gunStack, Hand hand,
                                       CallbackInfoReturnable<BaseBullet> cir) {
        // 关键：不限定 ServerPlayerEntity，客户端也要走这条路径，
        // 否则客户端原版 findAndConsumeAmmo 找不到弹药会 stopActiveHand，
        // 造成客户端/服务器使用状态失步（半自动只能射一发、全自动松手后自射）
        if (!(shooter instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) shooter;
        if (player.isCreative()) return; // 创造模式原版直接给弹，不消耗弹药箱

        BaseGun gun = (BaseGun)(Object)this;
        Item ammoItem = gun.getAmmoItem();
        if (ammoItem == null) return;
        int needed = 1 + EnchantmentHelper.getEnchantmentLevel(AoAEnchantments.GREED.get(), gunStack);

        ItemStack pouchStack = ItemStack.EMPTY;
        LazyOptional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        if (curiosOpt.isPresent()) {
            ICuriosItemHandler curios = curiosOpt.orElse(null);
            if (curios != null) {
                Optional<ICurioStacksHandler> backHandlerOpt = curios.getStacksHandler("back");
                if (backHandlerOpt.isPresent()) {
                    IItemHandler backInv = backHandlerOpt.get().getStacks();
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
        if (pouchStack.isEmpty()) return;

        LazyOptional<IItemHandler> cap = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (!cap.isPresent()) return;
        IItemHandler handler = cap.orElse(null);
        if (!(handler instanceof ItemStackHandler)) return;
        ItemStackHandler stackHandler = (ItemStackHandler) handler;

        int totalFound = 0;
        for (int slot = 0; slot < stackHandler.getSlots(); slot++) {
            ItemStack stack = stackHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == ammoItem) {
                totalFound += stack.getCount();
            }
        }
        if (totalFound < needed) return; // 弹药箱没货 → 走原版兜底（无弹停手，行为正确）

        // 消耗：两端都扣，保证客户端弹药箱显示与服务器同步
        // 耐久：只在服务器端扣，避免客户端重复损耗
        if (!player.world.isRemote) {
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
            // AmmoBoxInventory.onContentsChanged 已自动 save()，无需手动 serializeNBT
            pouchStack.damageItem(1, player, (p) -> p.sendBreakAnimation(hand));
        }

        BaseBullet bullet = gun.createProjectileEntity(shooter, gunStack, hand);
        if (bullet != null) {
            cir.setReturnValue(bullet);
        }
    }
}
