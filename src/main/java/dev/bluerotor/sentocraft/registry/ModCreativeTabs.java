package dev.bluerotor.sentocraft.registry;

import dev.bluerotor.sentocraft.SentoCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    SentoCraft.MOD_ID
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab>
            SENTOCRAFT_TAB =
            CREATIVE_MODE_TABS.register(
                    "sentocraft",
                    () -> CreativeModeTab.builder()
                            .title(
                                    Component.literal("SentoCraft")
                            )
                            .icon(
                                    () -> new ItemStack(
                                            ModItems.TANK.get()
                                    )
                            )
                            .displayItems(
                                    (parameters, output) -> {
                                        output.accept(
                                                ModItems.TANK.get()
                                        );

                                        output.accept(
                                                ModItems.BOILER.get()
                                        );

                                        output.accept(
                                                ModItems.BATH.get()
                                        );
                                    }
                            )
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}