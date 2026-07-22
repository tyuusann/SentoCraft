package dev.bluerotor.sentocraft.block;

import com.mojang.serialization.MapCodec;
import dev.bluerotor.sentocraft.blockentity.BathBlockEntity;
import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BathBlock extends BaseEntityBlock {

    public static final MapCodec<BathBlock> CODEC =
            simpleCodec(BathBlock::new);

    /**
     * 浴槽の底面。
     *
     * 高さは2/16ブロックです。
     */
    private static final VoxelShape BOTTOM_SHAPE =
            box(
                    0.0D,
                    0.0D,
                    0.0D,
                    16.0D,
                    2.0D,
                    16.0D
            );

    /**
     * 北側の壁。
     *
     * 厚さは2/16ブロックです。
     */
    private static final VoxelShape NORTH_WALL_SHAPE =
            box(
                    0.0D,
                    2.0D,
                    0.0D,
                    16.0D,
                    12.0D,
                    2.0D
            );

    /**
     * 南側の壁。
     */
    private static final VoxelShape SOUTH_WALL_SHAPE =
            box(
                    0.0D,
                    2.0D,
                    14.0D,
                    16.0D,
                    12.0D,
                    16.0D
            );

    /**
     * 西側の壁。
     */
    private static final VoxelShape WEST_WALL_SHAPE =
            box(
                    0.0D,
                    2.0D,
                    2.0D,
                    2.0D,
                    12.0D,
                    14.0D
            );

    /**
     * 東側の壁。
     */
    private static final VoxelShape EAST_WALL_SHAPE =
            box(
                    14.0D,
                    2.0D,
                    2.0D,
                    16.0D,
                    12.0D,
                    14.0D
            );

    /**
     * 底面と四方の壁を結合した浴槽全体の形状です。
     *
     * 中央には12×12/16ブロックの空間があるため、
     * プレイヤーが浴槽内へ入れます。
     */
    private static final VoxelShape BATH_SHAPE =
            Shapes.or(
                    BOTTOM_SHAPE,
                    NORTH_WALL_SHAPE,
                    SOUTH_WALL_SHAPE,
                    WEST_WALL_SHAPE,
                    EAST_WALL_SHAPE
            );

    public BathBlock(Properties properties) {
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
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return BATH_SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new BathBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.BATH.get(),
                BathBlockEntity::tick
        );
    }
}