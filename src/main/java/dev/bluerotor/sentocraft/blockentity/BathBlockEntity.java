package dev.bluerotor.sentocraft.blockentity;

import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BathBlockEntity extends BlockEntity {

    /** 浴槽内部のお湯容量 */
    public static final int MAX_HOT_WATER = 2000;

    /** 隣接タンクから1tickに受け取る最大量 */
    public static final int TRANSFER_AMOUNT_PER_TICK = 100;

    /** 1秒ごとのお湯消費量 */
    public static final int CONSUME_AMOUNT = 100;

    /** 20tick = 1秒 */
    public static final int CONSUME_INTERVAL = 20;

    /** 横方向だけを移送対象にする */
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    private int hotWaterAmount = 0;
    private int consumeTimer = 0;

    public BathBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(ModBlockEntities.BATH.get(), pos, state);
    }

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            BathBlockEntity bath
    ) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;

        if (bath.hotWaterAmount < MAX_HOT_WATER) {
            changed |= bath.transferFromAdjacentTank(level, pos);
        }

        if (bath.hotWaterAmount > 0) {
            bath.consumeTimer++;

            if (bath.consumeTimer >= CONSUME_INTERVAL) {
                bath.consumeTimer = 0;

                int consumedAmount = Math.min(
                        CONSUME_AMOUNT,
                        bath.hotWaterAmount
                );

                bath.hotWaterAmount -= consumedAmount;
                changed = true;
            }
        } else if (bath.consumeTimer != 0) {
            bath.consumeTimer = 0;
            changed = true;
        }

        if (changed) {
            bath.setChanged();
        }
    }

    /**
     * 東西南北に隣接するタンクからお湯を受け取ります。
     *
     * 1tickにつき合計最大100mBだけ移送します。
     */
    private boolean transferFromAdjacentTank(
            Level level,
            BlockPos bathPos
    ) {
        int remainingTransfer = Math.min(
                TRANSFER_AMOUNT_PER_TICK,
                MAX_HOT_WATER - hotWaterAmount
        );

        if (remainingTransfer <= 0) {
            return false;
        }

        boolean transferred = false;

        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            BlockPos adjacentPos =
                    bathPos.relative(direction);

            BlockEntity adjacentBlockEntity =
                    level.getBlockEntity(adjacentPos);

            if (!(adjacentBlockEntity
                    instanceof TankBlockEntity tank)) {
                continue;
            }

            int transferAmount = Math.min(
                    remainingTransfer,
                    tank.getHotWaterAmount()
            );

            if (transferAmount <= 0) {
                continue;
            }

            tank.removeHotWater(transferAmount);
            addHotWater(transferAmount);

            remainingTransfer -= transferAmount;
            transferred = true;

            if (remainingTransfer <= 0) {
                break;
            }
        }

        return transferred;
    }

    public int getHotWaterAmount() {
        return hotWaterAmount;
    }

    public int getRemainingCapacity() {
        return MAX_HOT_WATER - hotWaterAmount;
    }

    public boolean isEmpty() {
        return hotWaterAmount <= 0;
    }

    public boolean isFull() {
        return hotWaterAmount >= MAX_HOT_WATER;
    }

    public void setHotWaterAmount(int amount) {
        int clampedAmount = clampHotWaterAmount(amount);

        if (hotWaterAmount == clampedAmount) {
            return;
        }

        hotWaterAmount = clampedAmount;
        setChanged();
    }

    public int addHotWater(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int acceptedAmount = Math.min(
                amount,
                getRemainingCapacity()
        );

        if (acceptedAmount <= 0) {
            return 0;
        }

        hotWaterAmount += acceptedAmount;
        setChanged();

        return acceptedAmount;
    }

    public int removeHotWater(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int removedAmount = Math.min(
                amount,
                hotWaterAmount
        );

        if (removedAmount <= 0) {
            return 0;
        }

        hotWaterAmount -= removedAmount;
        setChanged();

        return removedAmount;
    }

    private static int clampHotWaterAmount(int amount) {
        return Math.max(
                0,
                Math.min(MAX_HOT_WATER, amount)
        );
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        tag.putInt(
                "HotWaterAmount",
                hotWaterAmount
        );

        tag.putInt(
                "ConsumeTimer",
                consumeTimer
        );
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        hotWaterAmount = clampHotWaterAmount(
                tag.getInt("HotWaterAmount")
        );

        consumeTimer = Math.max(
                0,
                Math.min(
                        CONSUME_INTERVAL - 1,
                        tag.getInt("ConsumeTimer")
                )
        );
    }
}