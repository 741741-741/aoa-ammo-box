package com.ammobox;

import com.ammobox.client.screen.AmmoBoxScreen;
import com.ammobox.event.ItemEventHandler;
import com.ammobox.event.AmmoBoxEnchantmentHandler;
import com.ammobox.init.ModContainers;
import com.ammobox.init.ModItems;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(AmmoBoxMod.MOD_ID)
public class AmmoBoxMod {
    public static final String MOD_ID = "aoaammobox";
    public static final Logger LOGGER = LogManager.getLogger();

    public AmmoBoxMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModContainers.CONTAINERS.register(bus);
        bus.addListener(this::clientSetup);
        bus.addListener(this::enqueueIMC);
        MinecraftForge.EVENT_BUS.register(new ItemEventHandler());
        MinecraftForge.EVENT_BUS.register(new AmmoBoxEnchantmentHandler());
        LOGGER.info("Ammo Box mod initialized!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> ScreenManager.registerFactory(ModContainers.AMMO_BOX.get(), AmmoBoxScreen::new));
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // 使用与符文袋完全相同的方式注册槽位
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.BACK.getMessageBuilder().build());
        // 额外日志
        LOGGER.info("Registered curios slot: back via IMC");
    }
}
