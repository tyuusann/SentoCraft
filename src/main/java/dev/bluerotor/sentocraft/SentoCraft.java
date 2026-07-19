package dev.bluerotor.sentocraft;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.bluerotor.sentocraft.registry.ModBlocks;
import dev.bluerotor.sentocraft.registry.ModItems;
import dev.bluerotor.sentocraft.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(SentoCraft.MOD_ID)
public class SentoCraft {

    public static final String MOD_ID = "sentocraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SentoCraft(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);

        // Register
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("SentoCraft initialized.");
    }
}