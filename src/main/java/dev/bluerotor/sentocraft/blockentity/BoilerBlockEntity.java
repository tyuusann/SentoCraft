package dev.bluerotor.sentocraft.blockentity;

import dev.bluerotor.sentocraft.menu.BoilerMenu;
import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BoilerBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler inventory = new ItemStackHandler(1) {

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getBurnTime(null) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** 残り燃焼時間 */
    private int burnTime = 0;

    /** 最大燃焼時間 */
    private int maxBurnTime = 0;

    /** GUI同期用 */
    private final ContainerData containerData = new SimpleContainerData(2) {

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> maxBurnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> maxBurnTime = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    /** 水→お湯変換間隔（20tick = 1秒） */
    public static final int CONVERT_INTERVAL = 20;

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOILER.get(), pos, state);
    }

    /**
     * 毎tick実行
     */
    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            BoilerBlockEntity boiler
    ) {
        if (level.isClientSide) {
            return;
        }

        boolean changed = false;

        if (boiler.burnTime > 0) {
            boiler.burnTime--;
            changed = true;
        }

        if (boiler.burnTime <= 0) {

            ItemStack fuel =
                    boiler.inventory.getStackInSlot(0);

            if (!fuel.isEmpty()) {

                int burn =
                        fuel.getBurnTime(null);

                if (burn > 0) {

                    boiler.burnTime = burn;
                    boiler.maxBurnTime = burn;

                    boiler.inventory.extractItem(
                            0,
                            1,
                            false
                    );

                    changed = true;
                }
            }
        }

        if (changed) {
            boiler.setChanged();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(
                "menu.sentocraft.boiler"
        );
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory inventory,
            Player player
    ) {
        return new BoilerMenu(
                containerId,
                inventory,
                containerData,
                ContainerLevelAccess.create(level, worldPosition)
        );
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public boolean isLit() {
        return burnTime > 0;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        tag.put(
                "Inventory",
                inventory.serializeNBT(registries)
        );

        tag.putInt("BurnTime", burnTime);
        tag.putInt("MaxBurnTime", maxBurnTime);
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        inventory.deserializeNBT(
                registries,
                tag.getCompound("Inventory")
        );

        burnTime = tag.getInt("BurnTime");
        maxBurnTime = tag.getInt("MaxBurnTime");
    }
}