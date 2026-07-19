package dev.bluerotor.sentocraft.menu;

import dev.bluerotor.sentocraft.blockentity.TankBlockEntity;
import dev.bluerotor.sentocraft.registry.ModBlocks;
import dev.bluerotor.sentocraft.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class TankMenu extends AbstractContainerMenu {

    private static final int DATA_COUNT = 2;
    private static final int WATER_AMOUNT_INDEX = 0;
    private static final int HOT_WATER_AMOUNT_INDEX = 1;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    /**
     * クライアント側で使用されるコンストラクタ。
     */
    public TankMenu(int containerId, Inventory inventory) {
        this(
                containerId,
                inventory,
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    /**
     * サーバー側で使用されるコンストラクタ。
     */
    public TankMenu(
            int containerId,
            Inventory inventory,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(ModMenus.TANK.get(), containerId);

        checkContainerDataCount(data, DATA_COUNT);

        this.data = data;
        this.access = access;

        addDataSlots(data);
    }

    public int getWaterAmount() {
        return data.get(WATER_AMOUNT_INDEX);
    }

    public int getHotWaterAmount() {
        return data.get(HOT_WATER_AMOUNT_INDEX);
    }

    public int getMaximumAmount() {
        return TankBlockEntity.MAX_FLUID;
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
                ModBlocks.TANK.get()
        );
    }
}