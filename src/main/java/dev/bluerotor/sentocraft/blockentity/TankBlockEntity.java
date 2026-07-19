package dev.bluerotor.sentocraft.blockentity;

import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TankBlockEntity extends BlockEntity {

    public static final int MAX_FLUID = 8000;

    private int waterAmount = 0;
    private int hotWaterAmount = 0;

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TANK.get(), pos, state);
    }

    public int getWaterAmount() {
        return waterAmount;
    }

    public int getHotWaterAmount() {
        return hotWaterAmount;
    }

    public void setWaterAmount(int amount) {
        waterAmount = Math.max(0, Math.min(MAX_FLUID, amount));
        setChanged();
    }

    public void setHotWaterAmount(int amount) {
        hotWaterAmount = Math.max(0, Math.min(MAX_FLUID, amount));
        setChanged();
    }

    public void addWater(int amount) {
        setWaterAmount(waterAmount + amount);
    }

    public void addHotWater(int amount) {
        setHotWaterAmount(hotWaterAmount + amount);
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

        waterAmount = Math.max(
                0,
                Math.min(MAX_FLUID, tag.getInt("WaterAmount"))
        );

        hotWaterAmount = Math.max(
                0,
                Math.min(MAX_FLUID, tag.getInt("HotWaterAmount"))
        );
    }
}