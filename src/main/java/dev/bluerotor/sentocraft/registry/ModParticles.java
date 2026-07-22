package dev.bluerotor.sentocraft.registry;

import dev.bluerotor.sentocraft.SentoCraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(
        modid = SentoCraft.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD
)
public final class ModParticles {

    /**
     * 専用湯気パーティクルのIDです。
     */
    public static final ResourceLocation STEAM_ID =
            ResourceLocation.fromNamespaceAndPath(
                    SentoCraft.MOD_ID,
                    "steam"
            );

    /**
     * SentoCraft専用の白い湯気パーティクルです。
     *
     * falseにすることで、パーティクル設定が「最小」の場合も
     * 強制表示対象にはしません。
     */
    public static final SimpleParticleType STEAM =
            new SimpleParticleType(false);

    private ModParticles() {
    }

    /**
     * パーティクルタイプをMinecraftのレジストリへ登録します。
     */
    @SubscribeEvent
    public static void registerParticles(
            RegisterEvent event
    ) {
        event.register(
                Registries.PARTICLE_TYPE,
                STEAM_ID,
                () -> STEAM
        );
    }
}