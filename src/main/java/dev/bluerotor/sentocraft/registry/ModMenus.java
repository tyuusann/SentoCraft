package dev.bluerotor.sentocraft.registry;

import dev.bluerotor.sentocraft.SentoCraft;
import dev.bluerotor.sentocraft.menu.TankMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(
                    Registries.MENU,
                    SentoCraft.MOD_ID
            );

    public static final DeferredHolder<MenuType<?>, MenuType<TankMenu>> TANK =
            MENU_TYPES.register(
                    "tank",
                    () -> new MenuType<>(
                            TankMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
