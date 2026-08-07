package com.ammobox.client.screen;

import com.ammobox.container.AmmoBoxContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class AmmoBoxScreen extends ContainerScreen<AmmoBoxContainer> {
    private static final ResourceLocation BG = new ResourceLocation("minecraft", "textures/gui/container/shulker_box.png");

    public AmmoBoxScreen(AmmoBoxContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partial, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(BG);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(matrix, x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override public void render(MatrixStack matrix, int mouseX, int mouseY, float partial) {
        this.renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, partial);
        this.renderHoveredTooltip(matrix, mouseX, mouseY);
    }

    @Override protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY) {
        this.font.drawString(matrix, this.title.getString(), 8.0F, 6.0F, 4210752);
        this.font.drawString(matrix, this.playerInventory.getDisplayName().getString(), 8.0F, 72.0F, 4210752);
    }
}
