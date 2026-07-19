package dev.bluerotor.sentocraft.block;

import com.mojang.serialization.MapCodec;
import dev.bluerotor.sentocraft.blockentity.TankBlockEntity;
import dev.bluerotor.sentocraft.menu.TankMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
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
    public @Nullable MenuProvider getMenuProvider(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof TankBlockEntity tankBlockEntity)) {
            return null;
        }

        return new SimpleMenuProvider(
                (containerId, playerInventory, player) ->
                        new TankMenu(
                                containerId,
                                playerInventory,
                                tankBlockEntity.getContainerData(),
                                ContainerLevelAccess.create(level, pos)
                        ),
                Component.translatable("menu.sentocraft.tank")
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
}