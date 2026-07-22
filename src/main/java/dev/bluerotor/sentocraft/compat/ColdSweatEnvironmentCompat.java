package dev.bluerotor.sentocraft.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.SimpleTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Cold Sweatの環境温度Modifierを管理します。
 *
 * お湯入り浴槽の近くにいるプレイヤーへ、
 * 一時的なWORLD温度補正を付与します。
 */
public final class ColdSweatEnvironmentCompat {

    /**
     * SentoCraftが追加した浴槽用Modifierを
     * 識別するためのNBTキーです。
     */
    private static final String BATH_HEAT_MARKER =
            "SentoCraftBathEnvironmentHeat";

    private ColdSweatEnvironmentCompat() {
    }

    /**
     * 浴槽周辺の環境加温を適用または更新します。
     *
     * 同じプレイヤーにSentoCraftのModifierが
     * 既に存在する場合は、新しく追加せず更新します。
     *
     * @param player        対象プレイヤー
     * @param temperature   WORLD温度への加算量
     * @param durationTicks Modifierが残る時間
     */
    public static void applyBathEnvironmentHeat(
            ServerPlayer player,
            double temperature,
            int durationTicks
    ) {
        if (player == null
                || player.level().isClientSide()
                || temperature <= 0.0D
                || durationTicks <= 0) {
            return;
        }

        Optional<TempModifier> existingModifier =
                Temperature.getModifier(
                        player,
                        Temperature.Trait.WORLD,
                        ColdSweatEnvironmentCompat
                                ::isBathEnvironmentModifier
                );

        if (existingModifier.isPresent()) {
            refreshModifier(
                    existingModifier.get(),
                    temperature,
                    durationTicks
            );
            return;
        }

        addModifier(
                player,
                temperature,
                durationTicks
        );
    }

    /**
     * SentoCraftが追加した浴槽用Modifierかを判定します。
     */
    private static boolean isBathEnvironmentModifier(
            TempModifier modifier
    ) {
        return modifier
                instanceof SimpleTempModifier
                && modifier.getNBT().getBoolean(
                BATH_HEAT_MARKER
        );
    }

    /**
     * 既存Modifierの温度と有効時間を更新します。
     */
    private static void refreshModifier(
            TempModifier modifier,
            double temperature,
            int durationTicks
    ) {
        if (!(modifier
                instanceof SimpleTempModifier simpleModifier)) {
            return;
        }

        simpleModifier.setTemperature(
                temperature
        );

        simpleModifier.setOperation(
                SimpleTempModifier.Operation.ADD
        );

        simpleModifier.expires(
                durationTicks
        );

        /*
         * 経過tickを0に戻すことで、浴槽の近くにいる間は
         * Modifierの有効時間を毎回延長します。
         */
        simpleModifier.setTicksExisted(
                0
        );

        simpleModifier.getNBT().putBoolean(
                BATH_HEAT_MARKER,
                true
        );

        simpleModifier.markDirty();
    }

    /**
     * 新しい浴槽用Modifierを追加します。
     */
    private static void addModifier(
            ServerPlayer player,
            double temperature,
            int durationTicks
    ) {
        SimpleTempModifier modifier =
                new SimpleTempModifier(
                        temperature,
                        SimpleTempModifier.Operation.ADD
                );

        modifier.expires(
                durationTicks
        );

        modifier.getNBT().putBoolean(
                BATH_HEAT_MARKER,
                true
        );

        Temperature.addModifier(
                player,
                modifier,
                Temperature.Trait.WORLD,
                Placement.LAST
        );
    }
}