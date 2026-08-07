package com.ammobox.init;

import com.ammobox.AmmoBoxMod;
import com.ammobox.item.AmmoBoxItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AmmoBoxMod.MOD_ID);

    public static final RegistryObject<Item> AMMO_BOX = ITEMS.register("ammo_box",
            () -> new AmmoBoxItem(new Item.Properties().group(ItemGroup.TOOLS)));
}
