package dev.bluerotor.sentocraft.registry;

import dev.bluerotor.sentocraft.SentoCraft;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(SentoCraft.MOD_ID);

    // Tank BlockItem
    public static final DeferredItem<BlockItem> TANK =
            ITEMS.registerSimpleBlockItem("tank", ModBlocks.TANK);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}