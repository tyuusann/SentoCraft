package dev.bluerotor.sentocraft.menu;

import dev.bluerotor.sentocraft.blockentity.BoilerBlockEntity;
import dev.bluerotor.sentocraft.registry.ModBlocks;
import dev.bluerotor.sentocraft.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class BoilerMenu extends AbstractContainerMenu {

    private static final int DATA_COUNT = 2;
    private static final int BURN_TIME_INDEX = 0;
    private static final int MAX_BURN_TIME_INDEX = 1;

    private static final int BOILER_SLOT_COUNT = 1;

    private static final int PLAYER_INVENTORY_START = BOILER_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END =
            PLAYER_INVENTORY_START + 27;

    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private static final int FUEL_SLOT_X = 80;
    private static final int FUEL_SLOT_Y = 35;

    private static final int PLAYER_INVENTORY_X = 8;
    private static final int PLAYER_INVENTORY_Y = 84;

    private static final int HOTBAR_Y = 142;

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

        ItemStackHandler boilerInventory =
                getBoilerInventory(access);

        addBoilerSlot(boilerInventory);
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        addDataSlots(data);
    }

    private static ItemStackHandler getBoilerInventory(
            ContainerLevelAccess access
    ) {
        return access.evaluate(
                (level, pos) -> {
                    BlockEntity blockEntity =
                            level.getBlockEntity(pos);

                    if (blockEntity
                            instanceof BoilerBlockEntity boilerBlockEntity) {

                        return boilerBlockEntity.getInventory();
                    }

                    return new ItemStackHandler(BOILER_SLOT_COUNT);
                }
        ).orElseGet(
                () -> new ItemStackHandler(BOILER_SLOT_COUNT)
        );
    }

    private void addBoilerSlot(
            ItemStackHandler boilerInventory
    ) {
        addSlot(
                new SlotItemHandler(
                        boilerInventory,
                        0,
                        FUEL_SLOT_X,
                        FUEL_SLOT_Y
                )
        );
    }

    private void addPlayerInventory(
            Inventory inventory
    ) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {

                int inventoryIndex =
                        column + row * 9 + 9;

                int x =
                        PLAYER_INVENTORY_X
                                + column * 18;

                int y =
                        PLAYER_INVENTORY_Y
                                + row * 18;

                addSlot(
                        new net.minecraft.world.inventory.Slot(
                                inventory,
                                inventoryIndex,
                                x,
                                y
                        )
                );
            }
        }
    }

    private void addPlayerHotbar(
            Inventory inventory
    ) {
        for (int column = 0; column < 9; column++) {

            int x =
                    PLAYER_INVENTORY_X
                            + column * 18;

            addSlot(
                    new net.minecraft.world.inventory.Slot(
                            inventory,
                            column,
                            x,
                            HOTBAR_Y
                    )
            );
        }
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
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        net.minecraft.world.inventory.Slot sourceSlot =
                slots.get(index);

        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack =
                sourceSlot.getItem();

        ItemStack copiedStack =
                sourceStack.copy();

        if (index < BOILER_SLOT_COUNT) {

            if (!moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }

        } else {

            if (sourceStack.getBurnTime(null) <= 0) {
                return ItemStack.EMPTY;
            }

            if (!moveItemStackTo(
                    sourceStack,
                    0,
                    BOILER_SLOT_COUNT,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount()
                == copiedStack.getCount()) {

            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(
                player,
                sourceStack
        );

        return copiedStack;
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