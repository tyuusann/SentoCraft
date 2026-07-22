package dev.bluerotor.sentocraft.compat;

import dev.bluerotor.sentocraft.SentoCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 浴槽による温熱効果を管理します。
 *
 * ・浴槽を出た後に残る30秒間の余熱
 * ・お湯入り浴槽周辺の環境加温
 *
 * を処理します。
 */
@EventBusSubscriber(
        modid = SentoCraft.MOD_ID
)
public final class BathWarmthManager {

    /**
     * Cold SweatのMod IDです。
     */
    private static final String COLD_SWEAT_MOD_ID =
            "cold_sweat";

    /**
     * 浴槽を出た後に余熱が残る時間です。
     *
     * 20tick × 30秒 = 600tick
     */
    private static final int AFTERGLOW_DURATION_TICKS =
            20 * 30;

    /**
     * 余熱による加温を実行する間隔です。
     *
     * 20tickで約1秒です。
     */
    private static final int AFTERGLOW_WARMING_INTERVAL =
            20;

    /**
     * 余熱中に1秒あたり加算する深部体温です。
     */
    private static final double AFTERGLOW_TEMPERATURE_INCREASE =
            0.25D;

    /**
     * 最後の入浴判定から、この時間以内なら
     * まだ浴槽に入っているものとして扱います。
     *
     * 浴槽側の入浴判定は10tickごとなので、
     * 少し余裕を持たせて15tickにしています。
     */
    private static final int BATHING_GRACE_TICKS =
            15;

    /**
     * 浴槽周辺の環境加温を更新する間隔です。
     *
     * 20tickで約1秒です。
     */
    private static final int ENVIRONMENT_HEAT_INTERVAL =
            20;

    /**
     * 浴槽の熱が届く水平方向の距離です。
     */
    private static final double ENVIRONMENT_HEAT_RADIUS =
            3.0D;

    /**
     * 浴槽の熱が届く下方向の距離です。
     */
    private static final double ENVIRONMENT_HEAT_BELOW =
            1.0D;

    /**
     * 浴槽の熱が届く上方向の距離です。
     */
    private static final double ENVIRONMENT_HEAT_ABOVE =
            3.0D;

    /**
     * 浴槽周辺でCold SweatのWORLD温度へ
     * 加算する値です。
     */
    private static final double ENVIRONMENT_TEMPERATURE_INCREASE =
            0.5D;

    /**
     * 浴槽用環境加温Modifierが残る時間です。
     *
     * 浴槽側では1秒ごとに更新します。
     * 40tickにすることで、範囲を離れた後は
     * 約2秒以内に効果が終了します。
     */
    private static final int ENVIRONMENT_MODIFIER_DURATION_TICKS =
            40;

    /**
     * プレイヤーが最後に入浴中と判定されたゲーム時刻です。
     */
    private static final Map<UUID, Long> LAST_BATHING_TIME =
            new HashMap<>();

    /**
     * プレイヤーの余熱効果が終了するゲーム時刻です。
     */
    private static final Map<UUID, Long> AFTERGLOW_END_TIME =
            new HashMap<>();

    private BathWarmthManager() {
    }

    /**
     * プレイヤーが現在入浴中であることを記録します。
     *
     * 呼び出されるたびに、余熱終了時刻を
     * 現在時刻から30秒後へ更新します。
     *
     * @param player 入浴中のプレイヤー
     */
    public static void markBathing(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        long currentGameTime =
                player.serverLevel().getGameTime();

        UUID playerId =
                player.getUUID();

        LAST_BATHING_TIME.put(
                playerId,
                currentGameTime
        );

        AFTERGLOW_END_TIME.put(
                playerId,
                currentGameTime
                        + AFTERGLOW_DURATION_TICKS
        );
    }

    /**
     * お湯が入っている浴槽周辺のプレイヤーへ
     * Cold Sweatの環境加温を適用します。
     *
     * @param level   浴槽が存在するLevel
     * @param bathPos 浴槽の座標
     */
    public static void applyEnvironmentHeat(
            Level level,
            BlockPos bathPos
    ) {
        if (level == null
                || bathPos == null
                || level.isClientSide()) {
            return;
        }

        if (!ModList.get().isLoaded(
                COLD_SWEAT_MOD_ID
        )) {
            return;
        }

        /*
         * 各浴槽について1秒ごとに処理します。
         */
        if (level.getGameTime()
                % ENVIRONMENT_HEAT_INTERVAL != 0) {
            return;
        }

        AABB heatingArea =
                createEnvironmentHeatingArea(
                        bathPos
                );

        List<ServerPlayer> nearbyPlayers =
                level.getEntitiesOfClass(
                        ServerPlayer.class,
                        heatingArea,
                        player ->
                                player.isAlive()
                                        && !player.isSpectator()
                );

        for (ServerPlayer player
                : nearbyPlayers) {
            ColdSweatEnvironmentCompat
                    .applyBathEnvironmentHeat(
                            player,
                            ENVIRONMENT_TEMPERATURE_INCREASE,
                            ENVIRONMENT_MODIFIER_DURATION_TICKS
                    );
        }
    }

    /**
     * 浴槽周辺の加温範囲を作成します。
     */
    private static AABB createEnvironmentHeatingArea(
            BlockPos bathPos
    ) {
        return new AABB(
                bathPos.getX()
                        - ENVIRONMENT_HEAT_RADIUS,
                bathPos.getY()
                        - ENVIRONMENT_HEAT_BELOW,
                bathPos.getZ()
                        - ENVIRONMENT_HEAT_RADIUS,
                bathPos.getX()
                        + 1.0D
                        + ENVIRONMENT_HEAT_RADIUS,
                bathPos.getY()
                        + 1.0D
                        + ENVIRONMENT_HEAT_ABOVE,
                bathPos.getZ()
                        + 1.0D
                        + ENVIRONMENT_HEAT_RADIUS
        );
    }

    /**
     * サーバーtick終了時に余熱処理を進めます。
     *
     * @param event サーバーtickイベント
     */
    @SubscribeEvent
    public static void onServerTick(
            ServerTickEvent.Post event
    ) {
        MinecraftServer server =
                event.getServer();

        /*
         * 余熱処理は1秒ごとに実行します。
         */
        if (server.getTickCount()
                % AFTERGLOW_WARMING_INTERVAL != 0) {
            return;
        }

        Set<UUID> onlinePlayerIds =
                new HashSet<>();

        for (ServerPlayer player
                : server.getPlayerList().getPlayers()) {
            UUID playerId =
                    player.getUUID();

            onlinePlayerIds.add(playerId);

            applyAfterglow(
                    player,
                    playerId
            );
        }

        /*
         * ログアウトしたプレイヤーの一時データを削除します。
         */
        LAST_BATHING_TIME
                .keySet()
                .removeIf(playerId ->
                        !onlinePlayerIds.contains(playerId));

        AFTERGLOW_END_TIME
                .keySet()
                .removeIf(playerId ->
                        !onlinePlayerIds.contains(playerId));
    }

    /**
     * 対象プレイヤーへ余熱効果を適用します。
     *
     * @param player   対象プレイヤー
     * @param playerId 対象プレイヤーのUUID
     */
    private static void applyAfterglow(
            ServerPlayer player,
            UUID playerId
    ) {
        Long lastBathingTime =
                LAST_BATHING_TIME.get(playerId);

        Long afterglowEndTime =
                AFTERGLOW_END_TIME.get(playerId);

        if (lastBathingTime == null
                || afterglowEndTime == null) {
            return;
        }

        long currentGameTime =
                player.serverLevel().getGameTime();

        /*
         * 余熱時間が終了している場合は、
         * 管理データを削除します。
         */
        if (currentGameTime
                >= afterglowEndTime) {
            LAST_BATHING_TIME.remove(playerId);
            AFTERGLOW_END_TIME.remove(playerId);
            return;
        }

        /*
         * 最後の入浴判定から15tick以内なら、
         * まだ浴槽に入っているものとして扱います。
         *
         * 入浴中はBathBlockEntity側で強い加温を行うため、
         * 余熱効果は重ねません。
         */
        boolean isStillBathing =
                currentGameTime
                        - lastBathingTime
                        <= BATHING_GRACE_TICKS;

        if (isStillBathing) {
            return;
        }

        /*
         * 浴槽を出た後だけ、弱い余熱を適用します。
         */
        ColdSweatCompat.warmPlayer(
                player,
                AFTERGLOW_TEMPERATURE_INCREASE
        );
    }
}