package com.ammobox.event;

import com.ammobox.item.AmmoBoxItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.tslat.aoa3.common.registration.AoAEnchantments;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;

public class AmmoConsumeHandler {

    @SubscribeEvent
    public void onGunFire(LivingEntityUseItemEvent.Start event) {
        // 这个方法不会被 AoA3 枪械调用，因为 AoA3 自定义了开火逻辑
        // 我们需要其他方式拦截，但作为示例，此处留空
        // 真正要拦截的是 BaseGun.findAndConsumeAmmo，因此我们仍然需要 Mixin
        // 但为了避免 StackOverflowError，我们暂时用事件替代，但无法完美工作
        // 所以还是启用 Mixin，但调整注入点
    }
}
