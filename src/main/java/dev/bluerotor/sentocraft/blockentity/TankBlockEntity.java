package dev.bluerotor.sentocraft.blockentity;

import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TankBlockEntity extends BlockEntity {

    public static final int MAX_FLUID = 8000;

    private static final int DATA_WATER_AMOUNT = 0;
    private static final int DATA_HOT_WATER_AMOUNT = 1;
    private static final int DATA_COUNT = 2;

    private int waterAmount = 0;
    private int hotWaterAmount = 0;

    private final ContainerData containerData = new ContainerData() {

        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_WATER_AMOUNT -> waterAmount;
                case DATA_HOT_WATER_AMOUNT -> hotWaterAmount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_WATER_AMOUNT -> setWaterAmount(value);
                case DATA_HOT_WATER_AMOUNT -> setHotWaterAmount(value);
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TANK.get(), pos, state);
    }

    public int getWaterAmount() {
        return waterAmount;
    }

    public int getHotWaterAmount() {
        return hotWaterAmount;
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    public void setWaterAmount(int amount) {
        waterAmount = clampFluidAmount(amount);
        setChanged();
    }

    public void setHotWaterAmount(int amount) {
        hotWaterAmount = clampFluidAmount(amount);
        setChanged();
    }

    public void addWater(int amount) {
        setWaterAmount(waterAmount + amount);
    }

    public void addHotWater(int amount) {
        setHotWaterAmount(hotWaterAmount + amount);
    }

    public void removeWater(int amount) {
        setWaterAmount(waterAmount - amount);
    }

    public void removeHotWater(int amount) {
        setHotWaterAmount(hotWaterAmount - amount);
    }

    private static int clampFluidAmount(int amount) {
        return Math.max(0, Math.min(MAX_FLUID, amount));
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        tag.putInt("WaterAmount", waterAmount);
        tag.putInt("HotWaterAmount", hotWaterAmount);
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        waterAmount = clampFluidAmount(tag.getInt("WaterAmount"));
        hotWaterAmount = clampFluidAmount(tag.getInt("HotWaterAmount"));
    }
}