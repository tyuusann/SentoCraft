package dev.bluerotor.sentocraft.block;

import com.mojang.serialization.MapCodec;
import dev.bluerotor.sentocraft.blockentity.BathBlockEntity;
import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * プレイヤーが入ることのできる浴槽ブロックです。
 *
 * 東西南北に別の浴槽が隣接している場合は、
 * 隣接方向の壁を自動的に取り除きます。
 */
public class BathBlock extends BaseEntityBlock {

    public static final MapCodec<BathBlock> CODEC =
            simpleCodec(BathBlock::new);

    /**
     * 北側に浴槽が接続されているかどうかです。
     */
    public static final BooleanProperty CONNECT_NORTH =
            BooleanProperty.create(
                    "connect_north"
            );

    /**
     * 南側に浴槽が接続されているかどうかです。
     */
    public static final BooleanProperty CONNECT_SOUTH =
            BooleanProperty.create(
                    "connect_south"
            );

    /**
     * 西側に浴槽が接続されているかどうかです。
     */
    public static final BooleanProperty CONNECT_WEST =
            BooleanProperty.create(
                    "connect_west"
            );

    /**
     * 東側に浴槽が接続されているかどうかです。
     */
    public static final BooleanProperty CONNECT_EAST =
            BooleanProperty.create(
                    "connect_east"
            );

    /**
     * 浴槽の底面です。
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
     * 北側の壁です。
     *
     * 四隅は個別の角パーツとして扱うため、
     * X方向の両端を2/16ずつ空けています。
     */
    private static final VoxelShape NORTH_WALL_SHAPE =
            box(
                    2.0D,
                    2.0D,
                    0.0D,
                    14.0D,
                    12.0D,
                    2.0D
            );

    /**
     * 南側の壁です。
     */
    private static final VoxelShape SOUTH_WALL_SHAPE =
            box(
                    2.0D,
                    2.0D,
                    14.0D,
                    14.0D,
                    12.0D,
                    16.0D
            );

    /**
     * 西側の壁です。
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
     * 東側の壁です。
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
     * 北西角です。
     */
    private static final VoxelShape NORTH_WEST_CORNER_SHAPE =
            box(
                    0.0D,
                    2.0D,
                    0.0D,
                    2.0D,
                    12.0D,
                    2.0D
            );

    /**
     * 北東角です。
     */
    private static final VoxelShape NORTH_EAST_CORNER_SHAPE =
            box(
                    14.0D,
                    2.0D,
                    0.0D,
                    16.0D,
                    12.0D,
                    2.0D
            );

    /**
     * 南西角です。
     */
    private static final VoxelShape SOUTH_WEST_CORNER_SHAPE =
            box(
                    0.0D,
                    2.0D,
                    14.0D,
                    2.0D,
                    12.0D,
                    16.0D
            );

    /**
     * 南東角です。
     */
    private static final VoxelShape SOUTH_EAST_CORNER_SHAPE =
            box(
                    14.0D,
                    2.0D,
                    14.0D,
                    16.0D,
                    12.0D,
                    16.0D
            );

    public BathBlock(
            Properties properties
    ) {
        super(properties);

        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(
                                CONNECT_NORTH,
                                false
                        )
                        .setValue(
                                CONNECT_SOUTH,
                                false
                        )
                        .setValue(
                                CONNECT_WEST,
                                false
                        )
                        .setValue(
                                CONNECT_EAST,
                                false
                        )
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * 接続状態をBlockStateへ登録します。
     */
    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    Block,
                    BlockState
                    > builder
    ) {
        builder.add(
                CONNECT_NORTH,
                CONNECT_SOUTH,
                CONNECT_WEST,
                CONNECT_EAST
        );
    }

    /**
     * 設置時に周囲の浴槽との接続状態を判定します。
     */
    @Override
    public @Nullable BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        BlockGetter level =
                context.getLevel();

        BlockPos pos =
                context.getClickedPos();

        return defaultBlockState()
                .setValue(
                        CONNECT_NORTH,
                        isBath(
                                level,
                                pos.north()
                        )
                )
                .setValue(
                        CONNECT_SOUTH,
                        isBath(
                                level,
                                pos.south()
                        )
                )
                .setValue(
                        CONNECT_WEST,
                        isBath(
                                level,
                                pos.west()
                        )
                )
                .setValue(
                        CONNECT_EAST,
                        isBath(
                                level,
                                pos.east()
                        )
                );
    }

    /**
     * 隣接ブロックが変化した際に接続状態を更新します。
     */
    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos
    ) {
        return switch (direction) {
            case NORTH ->
                    state.setValue(
                            CONNECT_NORTH,
                            neighborState.is(this)
                    );

            case SOUTH ->
                    state.setValue(
                            CONNECT_SOUTH,
                            neighborState.is(this)
                    );

            case WEST ->
                    state.setValue(
                            CONNECT_WEST,
                            neighborState.is(this)
                    );

            case EAST ->
                    state.setValue(
                            CONNECT_EAST,
                            neighborState.is(this)
                    );

            default ->
                    super.updateShape(
                            state,
                            direction,
                            neighborState,
                            level,
                            currentPos,
                            neighborPos
                    );
        };
    }

    /**
     * 指定座標に同じ浴槽ブロックがあるか確認します。
     */
    private boolean isBath(
            BlockGetter level,
            BlockPos pos
    ) {
        return level.getBlockState(pos)
                .is(this);
    }

    @Override
    protected RenderShape getRenderShape(
            BlockState state
    ) {
        return RenderShape.MODEL;
    }

    /**
     * 接続状態に応じた浴槽の当たり判定を返します。
     */
    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        boolean connectedNorth =
                state.getValue(
                        CONNECT_NORTH
                );

        boolean connectedSouth =
                state.getValue(
                        CONNECT_SOUTH
                );

        boolean connectedWest =
                state.getValue(
                        CONNECT_WEST
                );

        boolean connectedEast =
                state.getValue(
                        CONNECT_EAST
                );

        VoxelShape shape =
                BOTTOM_SHAPE;

        /*
         * 接続されていない方向にだけ壁を残します。
         */
        if (!connectedNorth) {
            shape = Shapes.or(
                    shape,
                    NORTH_WALL_SHAPE
            );
        }

        if (!connectedSouth) {
            shape = Shapes.or(
                    shape,
                    SOUTH_WALL_SHAPE
            );
        }

        if (!connectedWest) {
            shape = Shapes.or(
                    shape,
                    WEST_WALL_SHAPE
            );
        }

        if (!connectedEast) {
            shape = Shapes.or(
                    shape,
                    EAST_WALL_SHAPE
            );
        }

        /*
         * 角に接する2方向が両方とも接続されている場合だけ、
         * その角を取り除きます。
         *
         * どちらか一方でも外壁なら、角を残します。
         */
        if (!connectedNorth
                || !connectedWest) {
            shape = Shapes.or(
                    shape,
                    NORTH_WEST_CORNER_SHAPE
            );
        }

        if (!connectedNorth
                || !connectedEast) {
            shape = Shapes.or(
                    shape,
                    NORTH_EAST_CORNER_SHAPE
            );
        }

        if (!connectedSouth
                || !connectedWest) {
            shape = Shapes.or(
                    shape,
                    SOUTH_WEST_CORNER_SHAPE
            );
        }

        if (!connectedSouth
                || !connectedEast) {
            shape = Shapes.or(
                    shape,
                    SOUTH_EAST_CORNER_SHAPE
            );
        }

        return shape;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new BathBlockEntity(
                pos,
                state
        );
    }

    @Override
    public <T extends BlockEntity>
    @Nullable BlockEntityTicker<T> getTicker(
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