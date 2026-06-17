package com.mjzaymi.etherealvoid.screen;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.block.entity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModFluids;
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

    public final ReactionPoolBlockEntity blockEntity;
    public final float pressure = 1f;
    public final float temperature = 20f+273.15f;
    public final List<FluidStack> fluids;
    public final List<ItemStack> precipitates;

    public PoolMonitorScreen(PoolMonitorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 220;
        this.inventoryLabelY = this.imageHeight - 94;
        this.blockEntity = pMenu.getBlockEntity();
        this.fluids = blockEntity.getTank().getFluids();
        fluids.sort(FluidSorter.DENSITY_SORTER);
        precipitates = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

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


        //Fluids
        int currentY = 121;
        final float fluidsTotalHeight = 106f;
        final float capacity = blockEntity.getTank().getCapacity();
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


        //Others(Indicator lines)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(TEXTURE, leftPos+8, topPos+16, 176, 0, 80, 106);
        guiGraphics.blit(TEXTURE, leftPos+142, topPos+16, 176, 0, 11, 106);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);


        //Temperature
        final float totalTemperature = CalculateUtil.getPreferredTotalTemperature(temperature);
        int temperatureBarX = leftPos + 161;
        int temperatureBarY = topPos + 16;
        int temperatureBarWidth = 5;
        int temperatureBarHeight = 106;
        if (mouseX >= temperatureBarX && mouseX < temperatureBarX + temperatureBarWidth &&
                mouseY >= temperatureBarY && mouseY < temperatureBarY + temperatureBarHeight) {
            guiGraphics.fill(temperatureBarX, temperatureBarY, temperatureBarX + temperatureBarWidth,
                    temperatureBarY + temperatureBarHeight, 0x40FFFFFF);
            List<Component> tooltip = new ArrayList<>();
            Component name = Component.translatable("tooltip."+EtherealVoid.MOD_ID+".temperature");
            tooltip.add(name.copy().withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal(String.format("%,.2f / %,.2f K", temperature, totalTemperature))
                    .withStyle(ChatFormatting.GRAY));
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }


        //Pressure
        final float totalPressure = CalculateUtil.getPreferredTotalPressure(pressure);
        int pressureBarX = leftPos + 142;
        int pressureBarY = topPos + 16;
        int pressureBarWidth = 11;
        int pressureBarHeight = 106;
        if (mouseX >= pressureBarX && mouseX < pressureBarX + pressureBarWidth &&
                mouseY >= pressureBarY && mouseY < pressureBarY + pressureBarHeight) {
            guiGraphics.fill(pressureBarX, pressureBarY, pressureBarX + pressureBarWidth,
                    pressureBarY + pressureBarHeight, 0x40FFFFFF);
            List<Component> tooltip = new ArrayList<>();
            Component name = Component.translatable("tooltip."+EtherealVoid.MOD_ID+".pressure");
            tooltip.add(name.copy().withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal(String.format("%,.2f / %,.2f atm", pressure, totalPressure))
                    .withStyle(ChatFormatting.GRAY));
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }


        //Fluids
        int currentY = topPos + 121;
        int barX = leftPos + 8;
        int barWidth = 110;
        final float fluidsTotalHeight = 106f;
        final int capacity = blockEntity.getTank().getCapacity();
        for (FluidStack fluid : fluids) {
            int fluidHeight = Math.round(((float)fluid.getAmount() / (float)capacity) * fluidsTotalHeight);
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
                break;
            }
            currentY -= fluidHeight;
        }


        /*// 2. 同样定义流体柱在 GUI 里的基础几何范围
        int barX = leftPos + 20;
        int barY = topPos + 15;
        int barWidth = 16;
        int barHeight = 50;

        // 3. 获取流体数据和总容量

        // 核心算法：和绘制时一样，从最底部开始往上逆向推算每一层的位置
        int currentY = barY + barHeight;


        float pressure = 3;
        float totalPressure = CalculateUtil.getPreferredTotal(pressure);

        for (FluidStack fluid : fluidLayers) {
            if (fluid.isEmpty()) continue;

            // 计算这层流体的像素高度
            float fillRatio = (float) fluid.getAmount() / totalCapacity;
            int renderHeight = Math.round(barHeight * fillRatio);
            if (renderHeight <= 0) continue;

            currentY -= renderHeight; // 得到这层流体的物理顶部 Y 坐标

            // 4. 检查鼠标是否**正好停在这层流体**的矩形范围内
            if (mouseX >= barX && mouseX < barX + barWidth && mouseY >= currentY && mouseY < currentY + renderHeight) {

                // 🎯 【高亮核心】画一层半透明的白色遮罩盖在流体上面
                // 颜色格式为 ARGB：0x40FFFFFF 代表 25% 不透明度的纯白色（原版物品栏质感）
                // 如果觉得不够亮，可以改成 0x60FFFFFF
                guiGraphics.fill(barX, currentY, barX + barWidth, currentY + renderHeight, 0x40FFFFFF);

                // 5. 构建这一层流体的专属 Tooltip 文本
                List<Component> tooltip = new ArrayList<>();

                // 获取流体注册名并转换为本地化翻译文本（比如：水、岩浆）
                Component fluidName = Component.translatable(fluid.getTranslationKey());
                tooltip.add(fluidName.copy().withStyle(ChatFormatting.GOLD));

                // 显示这一层单独的容量： "1,500 mB"
                tooltip.add(Component.literal(String.format("%,d mB", fluid.getAmount())).withStyle(ChatFormatting.GRAY));

                // 顺便加上你的沉淀物种类
                //if (this.menu.hasPrecipitate()) {
                //    tooltip.add(Component.literal("沉淀物: " + this.menu.getPrecipitateName()).withStyle(ChatFormatting.YELLOW));
                //}

                // 6. 渲染文本框
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);

                // 既然已经找到了鼠标悬浮的那一层，直接跳出循环，防止叠在底下的流体重复触发
                break;
            }
        }*/
    }
}