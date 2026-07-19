package dev.bluerotor.sentocraft.block;

import com.mojang.serialization.MapCodec;
import dev.bluerotor.sentocraft.blockentity.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TankBlock extends BaseEntityBlock {

    public static final MapCodec<TankBlock> CODEC =
            simpleCodec(TankBlock::new);

    public TankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new TankBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof TankBlockEntity tankBlockEntity) {
                tankBlockEntity.addWater(1000);

                player.displayClientMessage(
                        Component.literal(
                                "水量: "
                                        + tankBlockEntity.getWaterAmount()
                                        + " / "
                                        + TankBlockEntity.MAX_FLUID
                                        + " mB"
                        ),
                        true
                );
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}