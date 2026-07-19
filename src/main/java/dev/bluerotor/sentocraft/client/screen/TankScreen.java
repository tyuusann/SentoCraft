package dev.bluerotor.sentocraft.client.screen;

import dev.bluerotor.sentocraft.SentoCraft;
import dev.bluerotor.sentocraft.menu.TankMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class TankScreen extends AbstractContainerScreen<TankMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    SentoCraft.MOD_ID,
                    "textures/gui/tank.png"
            );

    public TankScreen(
            TankMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);

        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                0,
                0,
                imageWidth,
                imageHeight,
                176,
                166
        );
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
