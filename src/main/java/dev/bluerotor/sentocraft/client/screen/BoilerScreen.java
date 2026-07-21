package dev.bluerotor.sentocraft.client.screen;

import dev.bluerotor.sentocraft.menu.BoilerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BoilerScreen extends AbstractContainerScreen<BoilerMenu> {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    public BoilerScreen(
            BoilerMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);

        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {

        int x = leftPos;
        int y = topPos;

        // 背景
        graphics.fill(
                x,
                y,
                x + imageWidth,
                y + imageHeight,
                0xFF555555
        );

        // 燃焼ゲージ枠
        graphics.fill(
                x + 20,
                y + 20,
                x + 36,
                y + 80,
                0xFF222222
        );

        // ゲージ
        if (menu.isBurning()) {

            int burnHeight = 56;

            if (menu.getMaxBurnTime() > 0) {
                burnHeight = menu.getBurnTime() * 56 / menu.getMaxBurnTime();
            }

            graphics.fill(
                    x + 22,
                    y + 78 - burnHeight,
                    x + 34,
                    y + 78,
                    0xFFFF6600
            );
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        renderBackground(graphics, mouseX, mouseY, partialTick);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.drawString(
                font,
                menu.isBurning()
                        ? "燃焼中"
                        : "停止中",
                leftPos + 50,
                topPos + 35,
                0xFFFFFF,
                false
        );

        renderTooltip(
                graphics,
                mouseX,
                mouseY
        );
    }
}
