package dev.bluerotor.sentocraft.client;

import dev.bluerotor.sentocraft.SentoCraft;
import dev.bluerotor.sentocraft.client.screen.TankScreen;
import dev.bluerotor.sentocraft.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = SentoCraft.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                ModMenus.TANK.get(),
                TankScreen::new
        );
    }
}
