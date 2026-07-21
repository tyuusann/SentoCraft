package dev.bluerotor.sentocraft.menu;

import dev.bluerotor.sentocraft.registry.ModBlocks;
import dev.bluerotor.sentocraft.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class BoilerMenu extends AbstractContainerMenu {

    private static final int DATA_COUNT = 2;
    private static final int BURN_TIME_INDEX = 0;
    private static final int MAX_BURN_TIME_INDEX = 1;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    /**
     * クライアント側
     */
    public BoilerMenu(
            int containerId,
            Inventory inventory
    ) {
        this(
                containerId,
                inventory,
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    /**
     * サーバー側
     */
    public BoilerMenu(
            int containerId,
            Inventory inventory,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(ModMenus.BOILER.get(), containerId);

        checkContainerDataCount(data, DATA_COUNT);

        this.data = data;
        this.access = access;

        addDataSlots(data);
    }

    public int getBurnTime() {
        return data.get(BURN_TIME_INDEX);
    }

    public int getMaxBurnTime() {
        return data.get(MAX_BURN_TIME_INDEX);
    }

    public boolean isBurning() {
        return getBurnTime() > 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(
                access,
                player,
                ModBlocks.BOILER.get()
        );
    }
}