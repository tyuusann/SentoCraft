package dev.bluerotor.sentocraft.block;

import com.mojang.serialization.MapCodec;
import dev.bluerotor.sentocraft.blockentity.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
}