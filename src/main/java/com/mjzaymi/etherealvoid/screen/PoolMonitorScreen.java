package com.mjzaymi.etherealvoid.screen;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.block.entity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.util.CalculateUtil;
import com.mjzaymi.etherealvoid.util.fluid.FluidSorter;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class PoolMonitorScreen extends AbstractContainerScreen<PoolMonitorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "textures/gui/pool_monitor_gui.png");

    public final ReactionPoolBlockEntity poolBlockEntity;
    public final float pressure = 1f;
    public final float temperature = 20f+273.15f;
    public final List<FluidStack> fluids;
    public final List<ItemStack> precipitates;

    public PoolMonitorScreen(PoolMonitorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 220;
        this.inventoryLabelY = this.imageHeight - 94;
        this.poolBlockEntity = pMenu.getPoolBlockEntity();
        if (poolBlockEntity ==null) {
            this.fluids = new ArrayList<>();
            this.precipitates = new ArrayList<>();
            return;
        }
        this.fluids = poolBlockEntity.getTank().getFluids();
        this.fluids.sort(FluidSorter.DENSITY_SORTER);
        this.precipitates = poolBlockEntity.getPrecipitates();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);


        //No structure
        if (poolBlockEntity==null) {
            //Indicator lines
            guiGraphics.blit(TEXTURE, leftPos+8, topPos+16, 176, 0, 80, 106);
            guiGraphics.blit(TEXTURE, leftPos+142, topPos+16, 176, 0, 11, 106);
            return;
        }

        //Thermometer bar
        final float temperatureTotalHeight = 97f;
        final float totalTemperature = CalculateUtil.getPreferredTotalTemperature(temperature);
        int temperatureHeight = Math.round((temperature / totalTemperature) * temperatureTotalHeight);
        guiGraphics.fill(leftPos+163, topPos+114-temperatureHeight+1, leftPos+164, topPos+115, 0xffeb3822);


        //Barometer bar
        final float pressureTotalHeight = 106f;
        final float totalPressure = CalculateUtil.getPreferredTotalPressure(pressure);
        int pressureHeight = Math.round((pressure / totalPressure) * pressureTotalHeight);

        FluidStack fluid = new FluidStack(Fluids.WATER, 1000);
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation textureRes = ext.getStillTexture(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureRes);
        int color = ext.getTintColor(fluid);
        RenderSystem.setShaderColor(
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                ((color >> 24) & 0xFF) / 255f
        );
        guiGraphics.blit(leftPos+142, topPos+121-pressureHeight+1, 0, 11, pressureHeight, sprite);

        //Precipitates
        int currentY = 121;
        if (!precipitates.isEmpty()) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            guiGraphics.fill(leftPos+8, topPos+currentY-6+1, leftPos+8+110, topPos+currentY+1, 0xffFAEBD7);
            currentY -= 6f;
        }

        //Fluids
        final float fluidsTotalHeight = currentY-15f;
        final float capacity = poolBlockEntity.getTank().getCapacity();
        for (FluidStack fluidStack : fluids) {
            fluid = fluidStack;
            ext = IClientFluidTypeExtensions.of(fluid.getFluid());
            textureRes = ext.getStillTexture(fluid);
            sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureRes);
            color = ext.getTintColor(fluid);
            RenderSystem.setShaderColor(
                    ((color >> 16) & 0xFF) / 255f,
                    ((color >> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    ((color >> 24) & 0xFF) / 255f
            );
            int fluidHeight = Math.round(((float)fluid.getAmount() / capacity) * fluidsTotalHeight);
            guiGraphics.blit(leftPos+8, topPos+currentY-fluidHeight+1, 0, 110, fluidHeight, sprite);
            currentY -= fluidHeight;
        }


        //Indicator lines
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(TEXTURE, leftPos+8, topPos+16, 176, 0, 80, 106);
        guiGraphics.blit(TEXTURE, leftPos+142, topPos+16, 176, 0, 11, 106);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);


        //Constants
        //Temperature
        final int temperatureBarX = leftPos + 161;
        final int temperatureBarY = topPos + 16;
        final int temperatureBarWidth = 5;
        final int temperatureBarHeight = 106;
        //Pressure
        final int pressureBarX = leftPos + 142;
        final int pressureBarY = topPos + 16;
        final int pressureBarWidth = 11;
        final int pressureBarHeight = 106;
        //Fluids and precipitates
        final int barX = leftPos + 8;
        final int barWidth = 110;
        final int fluidsTotalHeight = 106;
        //No structure
        final String noStructureKey = "tooltip."+EtherealVoid.MOD_ID+".pool_not_found";


        //Temperature
        final float totalTemperature = CalculateUtil.getPreferredTotalTemperature(temperature);
        if (mouseX >= temperatureBarX && mouseX < temperatureBarX + temperatureBarWidth &&
                mouseY >= temperatureBarY && mouseY < temperatureBarY + temperatureBarHeight) {
            guiGraphics.fill(temperatureBarX, temperatureBarY, temperatureBarX + temperatureBarWidth,
                    temperatureBarY + temperatureBarHeight, 0x40FFFFFF);
            List<Component> tooltip = new ArrayList<>();
            if (poolBlockEntity==null) {
                Component name = Component.translatable(noStructureKey);
                tooltip.add(name.copy().withStyle(ChatFormatting.GOLD));
            } else {
                Component name = Component.translatable("tooltip."+EtherealVoid.MOD_ID+".temperature");
                tooltip.add(name.copy().withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal(String.format("%,.2f / %,.2f K", temperature, totalTemperature))
                        .withStyle(ChatFormatting.GRAY));
            }
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            return;
        }


        //Pressure
        final float totalPressure = CalculateUtil.getPreferredTotalPressure(pressure);
        if (mouseX >= pressureBarX && mouseX < pressureBarX + pressureBarWidth &&
                mouseY >= pressureBarY && mouseY < pressureBarY + pressureBarHeight) {
            guiGraphics.fill(pressureBarX, pressureBarY, pressureBarX + pressureBarWidth,
                    pressureBarY + pressureBarHeight, 0x40FFFFFF);
            List<Component> tooltip = new ArrayList<>();
            if (poolBlockEntity==null) {
                Component name = Component.translatable(noStructureKey);
                tooltip.add(name.copy().withStyle(ChatFormatting.GOLD));
            } else {
                Component name = Component.translatable("tooltip." + EtherealVoid.MOD_ID + ".pressure");
                tooltip.add(name.copy().withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal(String.format("%,.2f / %,.2f atm", pressure, totalPressure))
                        .withStyle(ChatFormatting.GRAY));
            }
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            return;
        }


        //Precipitates
        int currentY = topPos + 121;
        if (poolBlockEntity==null) {
            if (mouseX >= barX && mouseX < barX + barWidth &&
                    mouseY >= currentY-fluidsTotalHeight+1 && mouseY < currentY) {
                guiGraphics.fill(barX, currentY-fluidsTotalHeight+1,
                        barX+barWidth, currentY, 0x40FFFFFF);
                List<Component> tooltip = new ArrayList<>();
                Component fluidName = Component.translatable(noStructureKey);
                tooltip.add(fluidName.copy().withStyle(ChatFormatting.GOLD));
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                return;
            }
            return;
        }
        if (!precipitates.isEmpty()) {
            if (mouseX >= barX && mouseX < barX + barWidth &&
                    mouseY >= currentY - 6 + 1 && mouseY < currentY) {
                guiGraphics.fill(barX, currentY - 6 + 1,
                        barX + barWidth, currentY, 0x40FFFFFF);
                List<Component> tooltip = new ArrayList<>();
                Component fluidName = Component.translatable("tooltip."+EtherealVoid.MOD_ID+".precipitates");
                tooltip.add(fluidName.copy().withStyle(ChatFormatting.GOLD));
                for (ItemStack item : precipitates) {
                    tooltip.add(Component.literal(String.format("%,d * ", item.getCount()))
                            .append(item.getDisplayName())
                            .withStyle(ChatFormatting.GRAY));
                }
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                return;
            }
            currentY -= 6;
        }


        //Fluids
        final int capacity = poolBlockEntity.getTank().getCapacity();
        for (FluidStack fluid : fluids) {
            int fluidHeight = Math.round(((float)fluid.getAmount() / (float)capacity) * (float)fluidsTotalHeight);
            if (mouseX >= barX && mouseX < barX + barWidth &&
                    mouseY >= currentY-fluidHeight+1 && mouseY < currentY) {
                guiGraphics.fill(barX, currentY-fluidHeight+1,
                        barX+barWidth, currentY, 0x40FFFFFF);

                List<Component> tooltip = new ArrayList<>();
                Component fluidName = Component.translatable(fluid.getTranslationKey());
                tooltip.add(fluidName.copy().withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal(String.format("%,d / %,d mb", fluid.getAmount(), capacity))
                        .withStyle(ChatFormatting.GRAY));
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                return;
            }
            currentY -= fluidHeight;
        }
    }
}