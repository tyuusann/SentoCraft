package dev.bluerotor.sentocraft.registry;

import dev.bluerotor.sentocraft.SentoCraft;
import dev.bluerotor.sentocraft.block.TankBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(SentoCraft.MOD_ID);

    public static final DeferredBlock<TankBlock> TANK =
            BLOCKS.register(
                    "tank",
                    () -> new TankBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.5F)
                                    .requiresCorrectToolForDrops()
                    )
            );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}