package dev.bluerotor.sentocraft.client.screen;

import dev.bluerotor.sentocraft.menu.TankMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TankScreen extends AbstractContainerScreen<TankMenu> {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 118;

    private static final int GAUGE_WIDTH = 40;
    private static final int GAUGE_HEIGHT = 60;

    private static final int WATER_GAUGE_RELATIVE_X = 35;
    private static final int HOT_WATER_GAUGE_RELATIVE_X = 101;
    private static final int GAUGE_RELATIVE_Y = 30;

    public TankScreen(
            TankMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);

        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;

        titleLabelX = 8;
        titleLabelY = 6;

        // プレイヤーインベントリ欄は表示しない
        inventoryLabelY = 1000;
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int left = leftPos;
        int top = topPos;

        // GUI背景
        guiGraphics.fill(
                left,
                top,
                left + imageWidth,
                top + imageHeight,
                0xFFC6C6C6
        );

        // 上端の明るい縁
        guiGraphics.fill(
                left,
                top,
                left + imageWidth,
                top + 2,
                0xFFFFFFFF
        );

        // 左端の明るい縁
        guiGraphics.fill(
                left,
                top,
                left + 2,
                top + imageHeight,
                0xFFFFFFFF
        );

        // 下端の暗い縁
        guiGraphics.fill(
                left,
                top + imageHeight - 2,
                left + imageWidth,
                top + imageHeight,
                0xFF555555
        );

        // 右端の暗い縁
        guiGraphics.fill(
                left + imageWidth - 2,
                top,
                left + imageWidth,
                top + imageHeight,
                0xFF555555
        );

        int waterGaugeX =
                left + WATER_GAUGE_RELATIVE_X;

        int hotWaterGaugeX =
                left + HOT_WATER_GAUGE_RELATIVE_X;

        int gaugeY =
                top + GAUGE_RELATIVE_Y;

        drawGaugeBackground(
                guiGraphics,
                waterGaugeX,
                gaugeY
        );

        drawGaugeBackground(
                guiGraphics,
                hotWaterGaugeX,
                gaugeY
        );

        drawFluidLevel(
                guiGraphics,
                waterGaugeX,
                gaugeY,
                menu.getWaterAmount(),
                0xFF3F76E4
        );

        drawFluidLevel(
                guiGraphics,
                hotWaterGaugeX,
                gaugeY,
                menu.getHotWaterAmount(),
                0xFFF07F35
        );
    }

    private void drawGaugeBackground(
            GuiGraphics guiGraphics,
            int x,
            int y
    ) {
        // ゲージ外枠
        guiGraphics.fill(
                x - 2,
                y - 2,
                x + GAUGE_WIDTH + 2,
                y + GAUGE_HEIGHT + 2,
                0xFF555555
        );

        // ゲージ内部
        guiGraphics.fill(
                x,
                y,
                x + GAUGE_WIDTH,
                y + GAUGE_HEIGHT,
                0xFF202020
        );
    }

    private void drawFluidLevel(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int amount,
            int fluidColor
    ) {
        int maximum =
                menu.getMaximumAmount();

        int clampedAmount =
                Math.max(
                        0,
                        Math.min(maximum, amount)
                );

        int filledHeight =
                clampedAmount
                        * GAUGE_HEIGHT
                        / maximum;

        int fluidTop =
                y + GAUGE_HEIGHT - filledHeight;

        guiGraphics.fill(
                x,
                fluidTop,
                x + GAUGE_WIDTH,
                y + GAUGE_HEIGHT,
                fluidColor
        );
    }

    @Override
    protected void renderLabels(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.drawString(
                font,
                title,
                titleLabelX,
                titleLabelY,
                0x404040,
                false
        );

        drawCenteredText(
                guiGraphics,
                Component.translatable(
                        "gui.sentocraft.water"
                ),
                55,
                18
        );

        drawCenteredText(
                guiGraphics,
                Component.translatable(
                        "gui.sentocraft.hot_water"
                ),
                121,
                18
        );
    }

    private void drawCenteredText(
            GuiGraphics guiGraphics,
            Component text,
            int centerX,
            int y
    ) {
        int textX =
                centerX - font.width(text) / 2;

        guiGraphics.drawString(
                font,
                text,
                textX,
                y,
                0x404040,
                false
        );
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderGaugeTooltip(
                guiGraphics,
                mouseX,
                mouseY
        );
    }

    private void renderGaugeTooltip(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        int waterGaugeX =
                leftPos + WATER_GAUGE_RELATIVE_X;

        int hotWaterGaugeX =
                leftPos + HOT_WATER_GAUGE_RELATIVE_X;

        int gaugeY =
                topPos + GAUGE_RELATIVE_Y;

        if (isMouseOverGauge(
                mouseX,
                mouseY,
                waterGaugeX,
                gaugeY
        )) {
            guiGraphics.renderTooltip(
                    font,
                    Component.literal(
                            menu.getWaterAmount()
                                    + " / "
                                    + menu.getMaximumAmount()
                                    + " mB"
                    ),
                    mouseX,
                    mouseY
            );

            return;
        }

        if (isMouseOverGauge(
                mouseX,
                mouseY,
                hotWaterGaugeX,
                gaugeY
        )) {
            guiGraphics.renderTooltip(
                    font,
                    Component.literal(
                            menu.getHotWaterAmount()
                                    + " / "
                                    + menu.getMaximumAmount()
                                    + " mB"
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    private boolean isMouseOverGauge(
            int mouseX,
            int mouseY,
            int gaugeX,
            int gaugeY
    ) {
        return mouseX >= gaugeX
                && mouseX < gaugeX + GAUGE_WIDTH
                && mouseY >= gaugeY
                && mouseY < gaugeY + GAUGE_HEIGHT;
    }
}