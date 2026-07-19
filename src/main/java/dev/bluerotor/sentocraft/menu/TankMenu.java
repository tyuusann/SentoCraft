package dev.bluerotor.sentocraft.menu;

import dev.bluerotor.sentocraft.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class TankMenu extends AbstractContainerMenu {

    public TankMenu(int containerId, Inventory inventory) {
        super(ModMenus.TANK.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}