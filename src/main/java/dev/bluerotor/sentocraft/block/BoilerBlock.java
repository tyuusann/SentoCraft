package dev.bluerotor.sentocraft.block;

import com.mojang.serialization.MapCodec;
import dev.bluerotor.sentocraft.blockentity.BoilerBlockEntity;
import dev.bluerotor.sentocraft.menu.BoilerMenu;
import dev.bluerotor.sentocraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BoilerBlock extends BaseEntityBlock {

    public static final MapCodec<BoilerBlock> CODEC =
            simpleCodec(BoilerBlock::new);

    public static final DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;

    public BoilerBlock(BlockBehaviour.Properties properties) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
        );
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
        return new BoilerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type,
                ModBlockEntities.BOILER.get(),
                BoilerBlockEntity::tick
        );
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof BoilerBlockEntity boilerBlockEntity)) {
            return null;
        }

        return new SimpleMenuProvider(
                (containerId, playerInventory, player) ->
                        new BoilerMenu(
                                containerId,
                                playerInventory,
                                boilerBlockEntity.getContainerData(),
                                ContainerLevelAccess.create(level, pos)
                        ),
                Component.translatable("menu.sentocraft.boiler")
        );
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer) {

            MenuProvider menuProvider =
                    state.getMenuProvider(level, pos);

            if (menuProvider != null) {
                serverPlayer.openMenu(menuProvider);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
    }
}