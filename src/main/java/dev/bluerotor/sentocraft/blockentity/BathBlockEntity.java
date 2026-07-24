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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 浴槽内部のお湯と入浴効果を管理します。
 *
 * 東西南北に接続された浴槽は、
 * 1つの大型浴槽ネットワークとして扱われます。
 */
public class BathBlockEntity extends BlockEntity {

    /**
     * 浴槽1ブロックあたりに保存できる
     * お湯の最大量です。
     */
    public static final int MAX_HOT_WATER =
            2000;

    /**
     * 浴槽ネットワークが隣接タンクから
     * 1tickに受け取る最大量です。
     */
    public static final int TRANSFER_AMOUNT_PER_TICK =
            100;

    /**
     * 1回の消費処理で減るお湯の量です。
     *
     * 接続された大型浴槽全体で
     * この量だけ消費します。
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
     * 浴槽およびタンクを検索する方向です。
     *
     * 上下方向は接続せず、
     * 同じ高さの東西南北だけを対象にします。
     */
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    /**
     * 浴槽ネットワークの代表座標を決める順序です。
     *
     * 接続された浴槽の中で、
     * X、Y、Zの順に最も小さい座標を
     * ネットワークの管理役とします。
     */
    private static final Comparator<BathBlockEntity>
            BATH_POSITION_COMPARATOR =
            Comparator
                    .<BathBlockEntity>comparingInt(
                            bath ->
                                    bath.getBlockPos().getX()
                    )
                    .thenComparingInt(
                            bath ->
                                    bath.getBlockPos().getY()
                    )
                    .thenComparingInt(
                            bath ->
                                    bath.getBlockPos().getZ()
                    );

    /**
     * この浴槽ブロック内部に保存されている
     * お湯の量です。
     *
     * 大型浴槽では、ネットワーク全体のお湯を
     * 接続された各浴槽へ均等に分配して保存します。
     */
    private int hotWaterAmount =
            0;

    /**
     * ネットワーク全体のお湯の
     * 消費間隔を管理するタイマーです。
     *
     * ネットワークの代表浴槽だけが使用します。
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
     *
     * 接続ネットワークの代表浴槽だけが、
     * 給湯、消費、お湯の分配、入浴効果を処理します。
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

        List<BathBlockEntity> connectedBaths =
                findConnectedBaths(
                        level,
                        pos
                );

        if (connectedBaths.isEmpty()) {
            return;
        }

        /*
         * findConnectedBaths()では座標順に並べているため、
         * 最初の浴槽がネットワークの代表浴槽です。
         */
        BathBlockEntity controllerBath =
                connectedBaths.getFirst();

        /*
         * 代表以外の浴槽は処理しません。
         *
         * これにより、大型浴槽の消費や給湯が
         * 浴槽の個数分だけ重複することを防ぎます。
         */
        if (bath != controllerBath) {
            return;
        }

        controllerBath.processBathNetwork(
                level,
                connectedBaths
        );
    }

    /**
     * 接続された浴槽ネットワーク全体を処理します。
     *
     * @param level          浴槽が存在するLevel
     * @param connectedBaths 接続されている全浴槽
     */
    private void processBathNetwork(
            Level level,
            List<BathBlockEntity> connectedBaths
    ) {
        int totalHotWater =
                getTotalHotWater(
                        connectedBaths
                );

        int totalCapacity =
                connectedBaths.size()
                        * MAX_HOT_WATER;

        /*
         * ネットワーク内のいずれかの浴槽に
         * タンクが隣接していれば給湯します。
         *
         * 同じタンクが複数の浴槽に接していても、
         * 1tickに重複して吸い出さないようにしています。
         */
        if (totalHotWater < totalCapacity) {
            int transferredAmount =
                    transferFromAdjacentTanks(
                            level,
                            connectedBaths,
                            totalCapacity
                                    - totalHotWater
                    );

            totalHotWater +=
                    transferredAmount;
        }

        /*
         * お湯がある場合だけ、
         * ネットワーク全体で消費タイマーを進めます。
         */
        if (totalHotWater > 0) {
            consumeTimer++;

            if (consumeTimer >= CONSUME_INTERVAL) {
                consumeTimer =
                        0;

                int consumedAmount =
                        Math.min(
                                CONSUME_AMOUNT,
                                totalHotWater
                        );

                totalHotWater -=
                        consumedAmount;

                setChanged();
            }
        } else if (consumeTimer != 0) {
            consumeTimer =
                    0;

            setChanged();
        }

        /*
         * ネットワーク全体のお湯を、
         * 接続されている各浴槽へ均等に分配します。
         *
         * 各浴槽が実際にお湯を保存するため、
         * 浴槽を破壊してネットワークが分割された場合も、
         * 残った区画に保存されていたお湯が維持されます。
         */
        distributeHotWater(
                connectedBaths,
                totalHotWater
        );

        if (totalHotWater <= 0) {
            return;
        }

        /*
         * ネットワークにお湯があれば、
         * 接続されたすべての浴槽区画で
         * 周辺加温、湯気、入浴効果を処理します。
         */
        for (BathBlockEntity connectedBath
                : connectedBaths) {
            BlockPos bathPos =
                    connectedBath.getBlockPos();

            BathWarmthManager.applyEnvironmentHeat(
                    level,
                    bathPos
            );

            connectedBath.spawnSteamParticle(
                    level,
                    bathPos
            );

            connectedBath.applyBathingEffects(
                    level,
                    bathPos
            );
        }
    }

    /**
     * 指定座標から東西南北に接続されている
     * すべての浴槽を検索します。
     */
    private static List<BathBlockEntity> findConnectedBaths(
            Level level,
            BlockPos startPos
    ) {
        List<BathBlockEntity> connectedBaths =
                new ArrayList<>();

        Set<BlockPos> visitedPositions =
                new HashSet<>();

        ArrayDeque<BlockPos> searchQueue =
                new ArrayDeque<>();

        searchQueue.add(
                startPos.immutable()
        );

        while (!searchQueue.isEmpty()) {
            BlockPos currentPos =
                    searchQueue.removeFirst();

            if (!visitedPositions.add(
                    currentPos
            )) {
                continue;
            }

            BlockEntity blockEntity =
                    level.getBlockEntity(
                            currentPos
                    );

            if (!(blockEntity
                    instanceof BathBlockEntity currentBath)) {
                continue;
            }

            connectedBaths.add(
                    currentBath
            );

            for (Direction direction
                    : HORIZONTAL_DIRECTIONS) {
                BlockPos adjacentPos =
                        currentPos
                                .relative(direction)
                                .immutable();

                if (visitedPositions.contains(
                        adjacentPos
                )) {
                    continue;
                }

                BlockEntity adjacentBlockEntity =
                        level.getBlockEntity(
                                adjacentPos
                        );

                if (adjacentBlockEntity
                        instanceof BathBlockEntity) {
                    searchQueue.addLast(
                            adjacentPos
                    );
                }
            }
        }

        connectedBaths.sort(
                BATH_POSITION_COMPARATOR
        );

        return connectedBaths;
    }

    /**
     * 接続された浴槽が保持している
     * お湯の合計量を取得します。
     */
    private static int getTotalHotWater(
            List<BathBlockEntity> connectedBaths
    ) {
        int totalHotWater =
                0;

        for (BathBlockEntity bath
                : connectedBaths) {
            totalHotWater +=
                    bath.hotWaterAmount;
        }

        return totalHotWater;
    }

    /**
     * ネットワーク内の浴槽に隣接するタンクから
     * お湯を受け取ります。
     *
     * 同じタンクが複数の浴槽に接している場合でも、
     * 1回の処理で1度だけ対象にします。
     *
     * @param level             浴槽が存在するLevel
     * @param connectedBaths    接続された浴槽
     * @param remainingCapacity ネットワークの空き容量
     * @return 実際に受け取ったお湯の量
     */
    private static int transferFromAdjacentTanks(
            Level level,
            List<BathBlockEntity> connectedBaths,
            int remainingCapacity
    ) {
        int remainingTransfer =
                Math.min(
                        TRANSFER_AMOUNT_PER_TICK,
                        remainingCapacity
                );

        if (remainingTransfer <= 0) {
            return 0;
        }

        int transferredAmount =
                0;

        Set<BlockPos> checkedTankPositions =
                new HashSet<>();

        for (BathBlockEntity bath
                : connectedBaths) {
            BlockPos bathPos =
                    bath.getBlockPos();

            for (Direction direction
                    : HORIZONTAL_DIRECTIONS) {
                BlockPos adjacentPos =
                        bathPos
                                .relative(direction)
                                .immutable();

                /*
                 * 同じタンクを複数回処理しません。
                 */
                if (!checkedTankPositions.add(
                        adjacentPos
                )) {
                    continue;
                }

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

                tank.removeHotWater(
                        transferAmount
                );

                transferredAmount +=
                        transferAmount;

                remainingTransfer -=
                        transferAmount;

                if (remainingTransfer <= 0) {
                    return transferredAmount;
                }
            }
        }

        return transferredAmount;
    }

    /**
     * ネットワーク全体のお湯を、
     * 接続された各浴槽へ均等に分配します。
     */
    private static void distributeHotWater(
            List<BathBlockEntity> connectedBaths,
            int totalHotWater
    ) {
        if (connectedBaths.isEmpty()) {
            return;
        }

        int bathCount =
                connectedBaths.size();

        int baseAmount =
                totalHotWater
                        / bathCount;

        int remainder =
                totalHotWater
                        % bathCount;

        for (int index = 0;
             index < bathCount;
             index++) {
            BathBlockEntity bath =
                    connectedBaths.get(index);

            int targetAmount =
                    baseAmount;

            /*
             * 割り切れない端数は、
             * 座標順の先頭から1mBずつ配ります。
             */
            if (index < remainder) {
                targetAmount++;
            }

            bath.setHotWaterAmount(
                    targetAmount
            );
        }
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

    /**
     * この浴槽ブロックへ実際に保存されている
     * お湯の量を取得します。
     */
    public int getHotWaterAmount() {
        return hotWaterAmount;
    }

    /**
     * この浴槽ブロック単体の空き容量を取得します。
     */
    public int getRemainingCapacity() {
        return MAX_HOT_WATER
                - hotWaterAmount;
    }

    /**
     * この浴槽ブロック単体が空か確認します。
     */
    public boolean isEmpty() {
        return hotWaterAmount <= 0;
    }

    /**
     * この浴槽ブロック単体が満杯か確認します。
     */
    public boolean isFull() {
        return hotWaterAmount
                >= MAX_HOT_WATER;
    }

    /**
     * この浴槽ブロックへ保存する
     * お湯の量を設定します。
     */
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
     * この浴槽ブロックへお湯を追加します。
     *
     * 通常の自動給湯ではネットワーク処理を使用しますが、
     * 外部連携との互換性のため、このメソッドも維持します。
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
     * この浴槽ブロックからお湯を取り除きます。
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