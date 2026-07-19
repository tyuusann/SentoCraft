package dev.bluerotor.sentocraft;

import com.mojang.logging.LogUtils;
import dev.bluerotor.sentocraft.registry.ModBlocks;
import dev.bluerotor.sentocraft.registry.ModCreativeTabs;
import dev.bluerotor.sentocraft.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(SentoCraft.MOD_ID)
public class SentoCraft {

    public static final String MOD_ID = "sentocraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SentoCraft(IEventBus modEventBus, ModContainer modContainer) {

        // MODの共通初期化イベント
        modEventBus.addListener(this::commonSetup);

        // クリエイティブタブへのアイテム追加イベント
        modEventBus.addListener(this::addCreative);

        // 各種登録
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // コンフィグ登録
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("SentoCraft initialized.");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.TANK);
        }
    }
}