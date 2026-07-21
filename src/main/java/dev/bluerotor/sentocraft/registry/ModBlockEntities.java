package dev.bluerotor.sentocraft.registry;

import dev.bluerotor.sentocraft.SentoCraft;
import dev.bluerotor.sentocraft.blockentity.BathBlockEntity;
import dev.bluerotor.sentocraft.blockentity.BoilerBlockEntity;
import dev.bluerotor.sentocraft.blockentity.TankBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    SentoCraft.MOD_ID
            );

    public static final Supplier<BlockEntityType<TankBlockEntity>> TANK =
            BLOCK_ENTITY_TYPES.register(
                    "tank",
                    () -> BlockEntityType.Builder.of(
                            TankBlockEntity::new,
                            ModBlocks.TANK.get()
                    ).build(null)
            );

    public static final Supplier<BlockEntityType<BoilerBlockEntity>> BOILER =
            BLOCK_ENTITY_TYPES.register(
                    "boiler",
                    () -> BlockEntityType.Builder.of(
                            BoilerBlockEntity::new,
                            ModBlocks.BOILER.get()
                    ).build(null)
            );

    public static final Supplier<BlockEntityType<BathBlockEntity>> BATH =
            BLOCK_ENTITY_TYPES.register(
                    "bath",
                    () -> BlockEntityType.Builder.of(
                            BathBlockEntity::new,
                            ModBlocks.BATH.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}