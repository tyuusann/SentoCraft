package dev.bluerotor.sentocraft.client.screen;

import dev.bluerotor.sentocraft.menu.BoilerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BoilerScreen
        extends AbstractContainerScreen<BoilerMenu> {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private static final int SLOT_SIZE = 18;

    private static final int FUEL_SLOT_X = 79;
    private static final int FUEL_SLOT_Y = 34;

    private static final int PLAYER_INVENTORY_X = 7;
    private static final int PLAYER_INVENTORY_Y = 83;

    private static final int HOTBAR_Y = 141;

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

        titleLabelX = 8;
        titleLabelY = 6;

        inventoryLabelX = 8;
        inventoryLabelY = 72;
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

        // GUI全体の背景
        graphics.fill(
                x,
                y,
                x + imageWidth,
                y + imageHeight,
                0xFF555555
        );

        // 上部の機械部分
        graphics.fill(
                x + 7,
                y + 17,
                x + 169,
                y + 70,
                0xFF6A6A6A
        );

        // 燃焼ゲージ枠
        graphics.fill(
                x + 20,
                y + 20,
                x + 36,
                y + 65,
                0xFF222222
        );

        // 燃焼ゲージ内部
        graphics.fill(
                x + 22,
                y + 22,
                x + 34,
                y + 63,
                0xFF111111
        );

        // 燃焼ゲージ
        if (menu.isBurning()) {

            int maximumHeight = 39;
            int burnHeight = maximumHeight;

            if (menu.getMaxBurnTime() > 0) {
                burnHeight =
                        menu.getBurnTime()
                                * maximumHeight
                                / menu.getMaxBurnTime();
            }

            graphics.fill(
                    x + 22,
                    y + 63 - burnHeight,
                    x + 34,
                    y + 63,
                    0xFFFF6600
            );
        }

        // 燃料スロット背景
        drawSlotBackground(
                graphics,
                x + FUEL_SLOT_X,
                y + FUEL_SLOT_Y
        );

        // プレイヤーインベントリ背景
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {

                drawSlotBackground(
                        graphics,
                        x + PLAYER_INVENTORY_X
                                + column * SLOT_SIZE,
                        y + PLAYER_INVENTORY_Y
                                + row * SLOT_SIZE
                );
            }
        }

        // ホットバー背景
        for (int column = 0; column < 9; column++) {

            drawSlotBackground(
                    graphics,
                    x + PLAYER_INVENTORY_X
                            + column * SLOT_SIZE,
                    y + HOTBAR_Y
            );
        }
    }

    private void drawSlotBackground(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        graphics.fill(
                x,
                y,
                x + 18,
                y + 18,
                0xFF222222
        );

        graphics.fill(
                x + 1,
                y + 1,
                x + 17,
                y + 17,
                0xFF8B8B8B
        );
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawString(
                font,
                title,
                titleLabelX,
                titleLabelY,
                0xFFFFFF,
                false
        );

        graphics.drawString(
                font,
                playerInventoryTitle,
                inventoryLabelX,
                inventoryLabelY,
                0xFFFFFF,
                false
        );

        graphics.drawString(
                font,
                menu.isBurning()
                        ? "燃焼中"
                        : "停止中",
                48,
                39,
                0xFFFFFF,
                false
        );

        graphics.drawString(
                font,
                "燃料",
                77,
                24,
                0xFFFFFF,
                false
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                graphics,
                mouseX,
                mouseY
        );
    }
}