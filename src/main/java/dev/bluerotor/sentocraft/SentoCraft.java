package dev.bluerotor.sentocraft;

import com.mojang.logging.LogUtils;
import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import dev.bluerotor.sentocraft.registry.ModBlocks;
import dev.bluerotor.sentocraft.registry.ModCreativeTabs;
import dev.bluerotor.sentocraft.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(SentoCraft.MOD_ID)
public class SentoCraft {

    public static final String MOD_ID = "sentocraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SentoCraft(IEventBus modEventBus) {

        // レジストリ登録
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // イベント登録
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 今後の初期化処理
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.TANK);
        }
    }
}