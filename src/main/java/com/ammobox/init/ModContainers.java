package com.ammobox.init;

import com.ammobox.AmmoBoxMod;
import com.ammobox.container.AmmoBoxContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.Hand;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, AmmoBoxMod.MOD_ID);
    public static final RegistryObject<ContainerType<AmmoBoxContainer>> AMMO_BOX = CONTAINERS.register("ammo_box",
            () -> IForgeContainerType.create((id, inv, data) -> {
                Hand hand = data.readEnumValue(Hand.class);
                return new AmmoBoxContainer(id, inv, hand);
            }));
}
