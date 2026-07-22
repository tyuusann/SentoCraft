package dev.bluerotor.sentocraft.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ColdSweatCompat {

    private static final Logger LOGGER =
            LogUtils.getLogger();

    /**
     * Cold SweatのMod IDです。
     */
    private static final String COLD_SWEAT_MOD_ID =
            "cold_sweat";

    /**
     * Cold SweatのTemperature APIクラスです。
     */
    private static final String TEMPERATURE_CLASS_NAME =
            "com.momosoftworks.coldsweat.api.util.Temperature";

    /**
     * Temperature内部のTrait列挙型です。
     */
    private static final String TRAIT_CLASS_NAME =
            "com.momosoftworks.coldsweat.api.util.Temperature$Trait";

    /**
     * 入浴効果で上昇できる深部体温の上限です。
     */
    private static final double MAX_BATH_CORE_TEMPERATURE =
            50.0D;

    /**
     * 入浴効果で上昇できる環境温度の上限です。
     *
     * WORLDの値はCOREとは単位感覚が異なるため、
     * 小さめの値に制限しています。
     */
    private static final double MAX_BATH_WORLD_TEMPERATURE =
            1.0D;

    private static boolean initializationAttempted =
            false;

    private static boolean apiAvailable =
            false;

    private static Method getTemperatureMethod;

    private static Method addTemperatureMethod;

    /**
     * Cold Sweatの深部体温を表すTraitです。
     */
    private static Object coreTrait;

    /**
     * Cold Sweatの環境温度を表すTraitです。
     */
    private static Object worldTrait;

    private ColdSweatCompat() {
    }

    /**
     * プレイヤーのCold Sweat深部体温へ、
     * 指定量を加算します。
     *
     * @param player 対象プレイヤー
     * @param amount 加算量
     */
    public static void warmPlayer(
            Player player,
            double amount
    ) {
        if (!canChangeTemperature(
                player,
                amount
        )) {
            return;
        }

        initializeApi();

        if (!apiAvailable) {
            return;
        }

        try {
            double currentTemperature =
                    getTemperature(
                            player,
                            coreTrait
                    );

            addTemperatureUpToMaximum(
                    player,
                    coreTrait,
                    currentTemperature,
                    amount,
                    MAX_BATH_CORE_TEMPERATURE
            );
        } catch (IllegalAccessException
                 | InvocationTargetException
                 | ClassCastException exception) {
            LOGGER.error(
                    "Failed to change Cold Sweat core temperature",
                    exception
            );
        }
    }

    /**
     * プレイヤーが感じるCold Sweatの環境温度へ、
     * 指定量を加算します。
     *
     * 実際のバイオーム温度や天候を変更するのではなく、
     * 対象プレイヤーのWORLD温度へ加温を適用します。
     *
     * @param player 対象プレイヤー
     * @param amount 加算量
     */
    public static void warmEnvironment(
            Player player,
            double amount
    ) {
        if (!canChangeTemperature(
                player,
                amount
        )) {
            return;
        }

        initializeApi();

        if (!apiAvailable) {
            return;
        }

        try {
            double currentTemperature =
                    getTemperature(
                            player,
                            worldTrait
                    );

            addTemperatureUpToMaximum(
                    player,
                    worldTrait,
                    currentTemperature,
                    amount,
                    MAX_BATH_WORLD_TEMPERATURE
            );
        } catch (IllegalAccessException
                 | InvocationTargetException
                 | ClassCastException exception) {
            LOGGER.error(
                    "Failed to change Cold Sweat world temperature",
                    exception
            );
        }
    }

    /**
     * プレイヤーを通常量だけ加温しつつ、
     * 深部体温が指定した最低値より低い場合は、
     * 最低値まで回復させます。
     *
     * 現在の浴槽処理では使用していませんが、
     * 既存コードとの互換性を保つため残しています。
     *
     * @param player             対象プレイヤー
     * @param normalIncrease     通常の加温量
     * @param minimumTemperature 保証する最低深部体温
     */
    public static void warmPlayerToMinimum(
            Player player,
            double normalIncrease,
            double minimumTemperature
    ) {
        if (!canChangeTemperature(
                player,
                normalIncrease
        )) {
            return;
        }

        initializeApi();

        if (!apiAvailable) {
            return;
        }

        try {
            double currentTemperature =
                    getTemperature(
                            player,
                            coreTrait
                    );

            double normallyWarmedTemperature =
                    currentTemperature
                            + normalIncrease;

            double targetTemperature =
                    Math.max(
                            normallyWarmedTemperature,
                            minimumTemperature
                    );

            targetTemperature =
                    Math.min(
                            targetTemperature,
                            MAX_BATH_CORE_TEMPERATURE
                    );

            double actualIncrease =
                    targetTemperature
                            - currentTemperature;

            if (actualIncrease <= 0.0D) {
                return;
            }

            addTemperatureMethod.invoke(
                    null,
                    player,
                    coreTrait,
                    actualIncrease
            );

            LOGGER.debug(
                    "Bath warming applied to {}: core {} -> {}",
                    player.getName().getString(),
                    currentTemperature,
                    targetTemperature
            );
        } catch (IllegalAccessException
                 | InvocationTargetException
                 | ClassCastException exception) {
            LOGGER.error(
                    "Failed to apply minimum Cold Sweat bath temperature",
                    exception
            );
        }
    }

    /**
     * 体温変更処理を実行できる状態か確認します。
     */
    private static boolean canChangeTemperature(
            Player player,
            double amount
    ) {
        if (player == null
                || amount <= 0.0D
                || player.level().isClientSide()) {
            return false;
        }

        return ModList.get().isLoaded(
                COLD_SWEAT_MOD_ID
        );
    }

    /**
     * 指定したCold Sweat温度を取得します。
     *
     * @param player 対象プレイヤー
     * @param trait  COREまたはWORLD
     * @return 現在の温度
     */
    private static double getTemperature(
            Player player,
            Object trait
    ) throws IllegalAccessException,
            InvocationTargetException {
        return ((Number) getTemperatureMethod.invoke(
                null,
                player,
                trait
        )).doubleValue();
    }

    /**
     * 上限を超えない範囲で、
     * 指定したTraitの温度を加算します。
     */
    private static void addTemperatureUpToMaximum(
            Player player,
            Object trait,
            double currentTemperature,
            double amount,
            double maximumTemperature
    ) throws IllegalAccessException,
            InvocationTargetException {
        if (currentTemperature
                >= maximumTemperature) {
            return;
        }

        double actualIncrease =
                Math.min(
                        amount,
                        maximumTemperature
                                - currentTemperature
                );

        if (actualIncrease <= 0.0D) {
            return;
        }

        addTemperatureMethod.invoke(
                null,
                player,
                trait,
                actualIncrease
        );

        LOGGER.debug(
                "Cold Sweat temperature applied to {}: {} -> {}",
                player.getName().getString(),
                currentTemperature,
                currentTemperature + actualIncrease
        );
    }

    /**
     * Cold Sweat APIを初回使用時に取得します。
     */
    @SuppressWarnings({
            "rawtypes",
            "unchecked"
    })
    private static synchronized void initializeApi() {
        if (initializationAttempted) {
            return;
        }

        initializationAttempted =
                true;

        try {
            Class<?> temperatureClass =
                    Class.forName(
                            TEMPERATURE_CLASS_NAME
                    );

            Class<?> traitClass =
                    Class.forName(
                            TRAIT_CLASS_NAME
                    );

            Class<? extends Enum> enumTraitClass =
                    traitClass.asSubclass(
                            Enum.class
                    );

            coreTrait =
                    Enum.valueOf(
                            enumTraitClass,
                            "CORE"
                    );

            worldTrait =
                    Enum.valueOf(
                            enumTraitClass,
                            "WORLD"
                    );

            getTemperatureMethod =
                    temperatureClass.getMethod(
                            "get",
                            LivingEntity.class,
                            traitClass
                    );

            addTemperatureMethod =
                    temperatureClass.getMethod(
                            "add",
                            LivingEntity.class,
                            traitClass,
                            double.class
                    );

            apiAvailable =
                    true;

            LOGGER.info(
                    "Cold Sweat compatibility initialized successfully"
            );
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | IllegalArgumentException exception) {
            apiAvailable =
                    false;

            LOGGER.error(
                    "Cold Sweat API initialization failed",
                    exception
            );
        }
    }
}