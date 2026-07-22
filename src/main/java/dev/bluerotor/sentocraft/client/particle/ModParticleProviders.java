package dev.bluerotor.sentocraft.client.particle;

import dev.bluerotor.sentocraft.SentoCraft;
import dev.bluerotor.sentocraft.registry.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(
        modid = SentoCraft.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ModParticleProviders {

    private ModParticleProviders() {
    }

    @SubscribeEvent
    public static void registerParticleProviders(
            RegisterParticleProvidersEvent event
    ) {
        event.registerSpriteSet(
                ModParticles.STEAM,
                SteamParticle.Provider::new
        );
    }
}