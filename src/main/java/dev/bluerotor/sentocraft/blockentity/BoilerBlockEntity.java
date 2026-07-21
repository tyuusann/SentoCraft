package dev.bluerotor.sentocraft.blockentity;

import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BoilerBlockEntity extends BlockEntity {

    /**
     * 燃料スロット
     */
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

    /**
     * 残り燃焼時間
     */
    private int burnTime = 0;

    /**
     * 燃焼開始時の最大燃焼時間
     * GUIの燃焼ゲージ表示に使用します。
     */
    private int maxBurnTime = 0;

    /**
     * 水からお湯への変換間隔
     * 20tick = 約1秒
     */
    public static final int CONVERT_INTERVAL = 20;

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOILER.get(), pos, state);
    }

    /**
     * BoilerBlockから毎tick呼び出されます。
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

        /*
         * 現在燃焼中なら、残り時間を1tick減らします。
         */
        if (boiler.burnTime > 0) {
            boiler.burnTime--;
            changed = true;
        }

        /*
         * 燃焼していない場合は、燃料スロットを確認します。
         */
        if (boiler.burnTime <= 0) {
            ItemStack fuelStack = boiler.inventory.getStackInSlot(0);

            if (!fuelStack.isEmpty()) {
                int fuelBurnTime = fuelStack.getBurnTime(null);

                if (fuelBurnTime > 0) {
                    boiler.burnTime = fuelBurnTime;
                    boiler.maxBurnTime = fuelBurnTime;

                    /*
                     * 燃料を1個消費します。
                     */
                    boiler.inventory.extractItem(0, 1, false);

                    changed = true;
                }
            }
        }

        if (changed) {
            boiler.setChanged();
        }
    }

    /**
     * 現在燃焼中かどうかを返します。
     */
    public boolean isLit() {
        return burnTime > 0;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = Math.max(0, burnTime);
        setChanged();
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public void setMaxBurnTime(int maxBurnTime) {
        this.maxBurnTime = Math.max(0, maxBurnTime);
        setChanged();
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        tag.put("Inventory", inventory.serializeNBT(registries));
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

        burnTime = Math.max(0, tag.getInt("BurnTime"));
        maxBurnTime = Math.max(0, tag.getInt("MaxBurnTime"));
    }
}