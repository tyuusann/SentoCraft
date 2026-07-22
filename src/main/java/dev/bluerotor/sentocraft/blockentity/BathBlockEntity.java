package dev.bluerotor.sentocraft.blockentity;

import dev.bluerotor.sentocraft.compat.BathWarmthManager;
import dev.bluerotor.sentocraft.compat.ColdSweatCompat;
import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import dev.bluerotor.sentocraft.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BathBlockEntity extends BlockEntity {

    /**
     * 浴槽内部に保存できるお湯の最大量です。
     */
    public static final int MAX_HOT_WATER =
            2000;

    /**
     * 隣接タンクから1tickに受け取る最大量です。
     */
    public static final int TRANSFER_AMOUNT_PER_TICK =
            100;

    /**
     * 1回の消費処理で減るお湯の量です。
     */
    public static final int CONSUME_AMOUNT =
            100;

    /**
     * お湯を消費する間隔です。
     *
     * 20tickで約1秒です。
     */
    public static final int CONSUME_INTERVAL =
            20;

    /**
     * 湯気を発生させる間隔です。
     *
     * 5tickで約0.25秒です。
     */
    private static final int STEAM_INTERVAL =
            5;

    /**
     * 入浴者を検索する間隔です。
     *
     * 10tickで約0.5秒です。
     */
    private static final int BATHING_SCAN_INTERVAL =
            10;

    /**
     * 入浴中の温度上昇を実行する間隔です。
     *
     * 20tickで約1秒です。
     */
    private static final int WARMING_INTERVAL =
            20;

    /**
     * 入浴中の回復を実行する間隔です。
     *
     * 40tickで約2秒です。
     */
    private static final int HEAL_INTERVAL =
            40;

    /**
     * 1回の回復量です。
     *
     * Minecraftでは1.0Fがハート半分です。
     */
    private static final float HEAL_AMOUNT =
            0.5F;

    /**
     * 入浴中に1秒あたり加算する
     * Cold Sweatの深部体温です。
     */
    private static final double CORE_TEMPERATURE_INCREASE =
            5.0D;

    /**
     * お湯を受け取れる方向です。
     *
     * 上下方向は含めず、東西南北だけを対象にします。
     */
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    /**
     * 浴槽内部に保存されているお湯の量です。
     */
    private int hotWaterAmount =
            0;

    /**
     * お湯の消費間隔を管理するタイマーです。
     */
    private int consumeTimer =
            0;

    public BathBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                ModBlockEntities.BATH.get(),
                pos,
                state
        );
    }

    /**
     * 浴槽の毎tick処理です。
     */
    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            BathBlockEntity bath
    ) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed =
                false;

        /*
         * 浴槽に空き容量がある場合、
         * 横方向に隣接するタンクからお湯を受け取ります。
         */
        if (bath.hotWaterAmount < MAX_HOT_WATER) {
            changed |= bath.transferFromAdjacentTank(
                    level,
                    pos
            );
        }

        /*
         * 浴槽にお湯がある間は、
         * 消費、湯気、入浴効果を処理します。
         */
        if (bath.hotWaterAmount > 0) {
            bath.consumeTimer++;

            if (bath.consumeTimer >= CONSUME_INTERVAL) {
                bath.consumeTimer =
                        0;

                int consumedAmount =
                        Math.min(
                                CONSUME_AMOUNT,
                                bath.hotWaterAmount
                        );

                bath.hotWaterAmount -=
                        consumedAmount;

                changed =
                        true;
            }

            /*
             * お湯が入っている浴槽の周辺へ、
             * Cold Sweatの環境加温を適用します。
             */
            BathWarmthManager.applyEnvironmentHeat(
                    level,
                    pos
            );

            bath.spawnSteamParticle(
                    level,
                    pos
            );

            bath.applyBathingEffects(
                    level,
                    pos
            );
        } else if (bath.consumeTimer != 0) {
            bath.consumeTimer =
                    0;

            changed =
                    true;
        }

        if (changed) {
            bath.setChanged();
        }
    }

    /**
     * 東西南北に隣接するタンクからお湯を受け取ります。
     *
     * 1tickあたりの合計移送量は
     * TRANSFER_AMOUNT_PER_TICKまでです。
     */
    private boolean transferFromAdjacentTank(
            Level level,
            BlockPos bathPos
    ) {
        int remainingTransfer =
                Math.min(
                        TRANSFER_AMOUNT_PER_TICK,
                        getRemainingCapacity()
                );

        if (remainingTransfer <= 0) {
            return false;
        }

        boolean transferred =
                false;

        for (Direction direction
                : HORIZONTAL_DIRECTIONS) {
            BlockPos adjacentPos =
                    bathPos.relative(direction);

            BlockEntity adjacentBlockEntity =
                    level.getBlockEntity(
                            adjacentPos
                    );

            if (!(adjacentBlockEntity
                    instanceof TankBlockEntity tank)) {
                continue;
            }

            int transferAmount =
                    Math.min(
                            remainingTransfer,
                            tank.getHotWaterAmount()
                    );

            if (transferAmount <= 0) {
                continue;
            }

            /*
             * transferAmountは浴槽の空き容量以下、
             * かつタンクのお湯量以下に制限済みです。
             *
             * TankBlockEntity.removeHotWater()はvoid型なので、
             * 戻り値は受け取りません。
             */
            tank.removeHotWater(
                    transferAmount
            );

            int acceptedAmount =
                    addHotWater(
                            transferAmount
                    );

            remainingTransfer -=
                    acceptedAmount;

            if (acceptedAmount > 0) {
                transferred =
                        true;
            }

            if (remainingTransfer <= 0) {
                break;
            }
        }

        return transferred;
    }

    /**
     * 浴槽内にいるプレイヤーへ入浴効果を適用します。
     */
    private void applyBathingEffects(
            Level level,
            BlockPos pos
    ) {
        long gameTime =
                level.getGameTime();

        /*
         * 入浴判定は0.5秒ごとに行います。
         */
        if (gameTime % BATHING_SCAN_INTERVAL != 0) {
            return;
        }

        boolean shouldHeal =
                gameTime % HEAL_INTERVAL == 0;

        boolean shouldWarm =
                gameTime % WARMING_INTERVAL == 0;

        AABB bathingArea =
                createBathingArea(
                        pos
                );

        List<Player> bathingPlayers =
                level.getEntitiesOfClass(
                        Player.class,
                        bathingArea,
                        player ->
                                player.isAlive()
                                        && !player.isSpectator()
                );

        for (Player player
                : bathingPlayers) {

            /*
             * 現在入浴中であることを、
             * 入浴後の余熱管理へ記録します。
             */
            if (player
                    instanceof ServerPlayer serverPlayer) {
                BathWarmthManager.markBathing(
                        serverPlayer
                );
            }

            /*
             * 回復効果は浴槽内にいる間だけ適用します。
             */
            if (shouldHeal
                    && player.getHealth()
                    < player.getMaxHealth()) {
                player.heal(
                        HEAL_AMOUNT
                );
            }

            /*
             * 入浴中は1秒ごとに深部体温を上昇させます。
             *
             * 周囲の環境温度はBathWarmthManager側で処理するため、
             * ここでは深部体温だけを変更します。
             */
            if (shouldWarm) {
                ColdSweatCompat.warmPlayer(
                        player,
                        CORE_TEMPERATURE_INCREASE
                );
            }
        }
    }

    /**
     * 入浴判定に使用する浴槽内部の範囲です。
     *
     * 水平方向は浴槽の縁より内側だけを対象にします。
     */
    private static AABB createBathingArea(
            BlockPos pos
    ) {
        return new AABB(
                pos.getX() + 0.125D,
                pos.getY() + 0.125D,
                pos.getZ() + 0.125D,
                pos.getX() + 0.875D,
                pos.getY() + 1.50D,
                pos.getZ() + 0.875D
        );
    }

    /**
     * 浴槽上部から専用の湯気を発生させます。
     */
    private void spawnSteamParticle(
            Level level,
            BlockPos pos
    ) {
        if (!(level
                instanceof ServerLevel serverLevel)) {
            return;
        }

        if (level.getGameTime() % STEAM_INTERVAL != 0) {
            return;
        }

        double particleX =
                pos.getX()
                        + 0.20D
                        + level.random.nextDouble()
                        * 0.60D;

        double particleY =
                pos.getY()
                        + 1.08D;

        double particleZ =
                pos.getZ()
                        + 0.20D
                        + level.random.nextDouble()
                        * 0.60D;

        double velocityX =
                (level.random.nextDouble()
                        - 0.5D)
                        * 0.006D;

        double velocityY =
                0.014D
                        + level.random.nextDouble()
                        * 0.006D;

        double velocityZ =
                (level.random.nextDouble()
                        - 0.5D)
                        * 0.006D;

        serverLevel.sendParticles(
                ModParticles.STEAM,
                particleX,
                particleY,
                particleZ,
                0,
                velocityX,
                velocityY,
                velocityZ,
                1.0D
        );
    }

    public int getHotWaterAmount() {
        return hotWaterAmount;
    }

    public int getRemainingCapacity() {
        return MAX_HOT_WATER
                - hotWaterAmount;
    }

    public boolean isEmpty() {
        return hotWaterAmount <= 0;
    }

    public boolean isFull() {
        return hotWaterAmount
                >= MAX_HOT_WATER;
    }

    public void setHotWaterAmount(
            int amount
    ) {
        int clampedAmount =
                clampHotWaterAmount(
                        amount
                );

        if (hotWaterAmount == clampedAmount) {
            return;
        }

        hotWaterAmount =
                clampedAmount;

        setChanged();
    }

    /**
     * 浴槽へお湯を追加します。
     *
     * @param amount 追加しようとする量
     * @return 実際に追加できた量
     */
    public int addHotWater(
            int amount
    ) {
        if (amount <= 0) {
            return 0;
        }

        int acceptedAmount =
                Math.min(
                        amount,
                        getRemainingCapacity()
                );

        if (acceptedAmount <= 0) {
            return 0;
        }

        hotWaterAmount +=
                acceptedAmount;

        setChanged();

        return acceptedAmount;
    }

    /**
     * 浴槽からお湯を取り除きます。
     *
     * @param amount 取り除こうとする量
     * @return 実際に取り除いた量
     */
    public int removeHotWater(
            int amount
    ) {
        if (amount <= 0) {
            return 0;
        }

        int removedAmount =
                Math.min(
                        amount,
                        hotWaterAmount
                );

        if (removedAmount <= 0) {
            return 0;
        }

        hotWaterAmount -=
                removedAmount;

        setChanged();

        return removedAmount;
    }

    /**
     * お湯量を0から最大容量の範囲に制限します。
     */
    private static int clampHotWaterAmount(
            int amount
    ) {
        return Math.max(
                0,
                Math.min(
                        MAX_HOT_WATER,
                        amount
                )
        );
    }

    /**
     * Block Entityの情報をNBTへ保存します。
     */
    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(
                tag,
                registries
        );

        tag.putInt(
                "HotWaterAmount",
                hotWaterAmount
        );

        tag.putInt(
                "ConsumeTimer",
                consumeTimer
        );
    }

    /**
     * NBTからBlock Entityの情報を読み込みます。
     */
    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(
                tag,
                registries
        );

        hotWaterAmount =
                clampHotWaterAmount(
                        tag.getInt(
                                "HotWaterAmount"
                        )
                );

        consumeTimer =
                Math.max(
                        0,
                        Math.min(
                                CONSUME_INTERVAL - 1,
                                tag.getInt(
                                        "ConsumeTimer"
                                )
                        )
                );
    }
}