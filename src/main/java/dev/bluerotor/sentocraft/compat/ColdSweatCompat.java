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
     * Cold Sweat 1.21系のTemperature APIです。
     */
    private static final String TEMPERATURE_CLASS_NAME =
            "com.momosoftworks.coldsweat.api.util.Temperature";

    /**
     * Temperature内部のTrait列挙型です。
     */
    private static final String TRAIT_CLASS_NAME =
            "com.momosoftworks.coldsweat.api.util.Temperature$Trait";

    /**
     * 入浴によって上昇できる深部体温の上限です。
     */
    private static final double MAX_BATH_CORE_TEMPERATURE =
            50.0D;

    private static boolean initializationAttempted = false;
    private static boolean apiAvailable = false;

    private static Method getTemperatureMethod;
    private static Method addTemperatureMethod;
    private static Object coreTrait;

    private ColdSweatCompat() {
    }

    /**
     * プレイヤーのCold Sweat深部体温を上昇させます。
     *
     * @param player 対象プレイヤー
     * @param amount 上昇量
     */
    public static void warmPlayer(
            Player player,
            double amount
    ) {
        if (player == null
                || amount <= 0.0D
                || player.level().isClientSide()) {
            return;
        }

        if (!ModList.get().isLoaded(COLD_SWEAT_MOD_ID)) {
            return;
        }

        initializeApi();

        if (!apiAvailable) {
            return;
        }

        try {
            double currentTemperature =
                    ((Number) getTemperatureMethod.invoke(
                            null,
                            player,
                            coreTrait
                    )).doubleValue();

            if (currentTemperature
                    >= MAX_BATH_CORE_TEMPERATURE) {
                return;
            }

            double actualIncrease =
                    Math.min(
                            amount,
                            MAX_BATH_CORE_TEMPERATURE
                                    - currentTemperature
                    );

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
                    currentTemperature + actualIncrease
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

        initializationAttempted = true;

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
                    traitClass.asSubclass(Enum.class);

            coreTrait =
                    Enum.valueOf(
                            enumTraitClass,
                            "CORE"
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

            apiAvailable = true;

            LOGGER.info(
                    "Cold Sweat compatibility initialized successfully"
            );
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | IllegalArgumentException exception) {
            apiAvailable = false;

            LOGGER.error(
                    "Cold Sweat API initialization failed",
                    exception
            );
        }
    }
}