package com.mjzaymi.etherealvoid.screen;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.blockentity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.common.util.GameUtil;
import com.mjzaymi.etherealvoid.common.util.fluid.FluidSorter;
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
    // public final float pressure = 1f;
    // public final float temperature = 20f+273.15f;

    public float getTemperature() {
        // 如果没有多方块结构，返回基础室温 293.15K
        if (this.poolBlockEntity == null) return 20f + 273.15f;
        // 💡 替换为你 ReactionPoolBlockEntity 里面实际存储温度的方法名
        return this.poolBlockEntity.getTemperature();
    }

    public float getPressure() {
        if (this.poolBlockEntity == null) return 1.0f;
        // 💡 替换为你 ReactionPoolBlockEntity 里面实际存储气压的方法名
        return this.poolBlockEntity.getPressure();
    }
    public final List<FluidStack> fluids;
    public final List<ItemStack> precipitates;

    public PoolMonitorScreen(PoolMonitorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 220;
        this.inventoryLabelY = this.imageHeight - 94;
        this.poolBlockEntity = pMenu.getPoolBlockEntity();
        if (poolBlockEntity == null) {
            this.fluids = new ArrayList<>();
            this.precipitates = new ArrayList<>();
            return;
        }
        this.fluids = poolBlockEntity.getTankAll().getFluids();
        this.fluids.sort(FluidSorter.DENSITY_SORTER);
        this.precipitates = poolBlockEntity.getPrecipitatesAll();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        //No structure
        if (poolBlockEntity == null) {
            //Indicator lines
            guiGraphics.blit(TEXTURE, leftPos+8, topPos+16, 176, 0, 80, 106);
            guiGraphics.blit(TEXTURE, leftPos+142, topPos+16, 176, 0, 11, 106);
            return;
        }

        final float temperatureTotalHeight = 97f;
        float currentTemp = getTemperature();
        final float totalTemperature = GameUtil.getPreferredTotalTemperature(currentTemp);
        int temperatureHeight = Math.round((currentTemp / totalTemperature) * temperatureTotalHeight);
        guiGraphics.fill(leftPos+163, topPos+114-temperatureHeight+1, leftPos+164, topPos+115, 0xffeb3822);

        final float pressureTotalHeight = 106f;
        float currentPress = getPressure();
        final float totalPressure = GameUtil.getPreferredTotalPressure(currentPress);
        int pressureHeight = Math.round((currentPress / totalPressure) * pressureTotalHeight);

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

        // --- 开始分拣液体与气体 ---
        // TODO: 可替换为动态空气密度，例如 poolBlockEntity.getAtmosphereDensity()
        float currentAirDensity = 0.0012f;
        List<FluidStack> liquids = new ArrayList<>();
        List<FluidStack> gases = new ArrayList<>();

        for (FluidStack fs : fluids) {
            ResourceLocation rl = net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(fs.getFluid());
            float density = 1.0f;
            if (rl != null && FluidSorter.DENSITY_MAP.containsKey(rl.getPath())) {
                density = FluidSorter.DENSITY_MAP.get(rl.getPath());
            }
            if (density < currentAirDensity) {
                gases.add(fs);
            } else {
                liquids.add(fs);
            }
        }

        // --- 1. 计算与渲染沉淀物 ---
        int currentY = 121;
        if (!precipitates.isEmpty()) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            guiGraphics.fill(leftPos+8, topPos+currentY-6+1, leftPos+8+110, topPos+currentY+1, 0xffFAEBD7);
            currentY -= 6;
        }

        // 液体所占的显示槽最大上限为当前沉淀物上方到初始顶端（topPos+16）的距离
        final float fluidsTotalHeight = currentY - 16f;
        final float capacity = poolBlockEntity.getTankAll().getCapacity();

        // --- 2. 渲染液体（由沉淀物上方，从底往上累加） ---
        int liquidY = currentY;
        for (FluidStack fluidStack : liquids) {
            ext = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            textureRes = ext.getStillTexture(fluidStack);
            sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureRes);
            color = ext.getTintColor(fluidStack);
            RenderSystem.setShaderColor(
                    ((color >> 16) & 0xFF) / 255f,
                    ((color >> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    ((color >> 24) & 0xFF) / 255f
            );
            int fluidHeight = Math.round(((float) fluidStack.getAmount() / capacity) * fluidsTotalHeight);

            int drawX = leftPos + 8;
            int drawY = topPos + liquidY - fluidHeight + 1;

            drawTiledFluid(guiGraphics, sprite, drawX, drawY, 110, fluidHeight);
            liquidY -= fluidHeight;
        }

        // --- 3. 渲染气体（从显示槽的最顶端 topPos+16，从顶往下悬挂） ---
        int gasY = 16;
        for (FluidStack fluidStack : gases) {
            ext = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            textureRes = ext.getStillTexture(fluidStack);
            sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureRes);
            color = ext.getTintColor(fluidStack);
            RenderSystem.setShaderColor(
                    ((color >> 16) & 0xFF) / 255f,
                    ((color >> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    ((color >> 24) & 0xFF) / 255f
            );
            int fluidHeight = Math.round(((float) fluidStack.getAmount() / capacity) * fluidsTotalHeight);

            int drawX = leftPos + 8;
            int drawY = topPos + gasY; // 气体起始点在当前悬挂最高处

            drawTiledFluid(guiGraphics, sprite, drawX, drawY, 110, fluidHeight);
            gasY += fluidHeight; // 下一层气体悬挂在当前气体的下方
        }

        //Indicator lines
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(TEXTURE, leftPos+8, topPos+16, 176, 0, 80, 106);
        guiGraphics.blit(TEXTURE, leftPos+142, topPos+16, 176, 0, 11, 106);
    }

    private void drawTiledFluid(
            GuiGraphics guiGraphics,
            TextureAtlasSprite sprite,
            int x,
            int y,
            int width,
            int height
    ) {
        int tile = 32;
        for (int yy = 0; yy < height; yy += tile) {
            for (int xx = 0; xx < width; xx += tile) {
                int w = Math.min(tile, width - xx);
                int h = Math.min(tile, height - yy);

                if (w == tile && h == tile) {
                    guiGraphics.blit(x + xx, y + yy, 0, tile, tile, sprite);
                } else {
                    guiGraphics.enableScissor(x + xx, y + yy, x + xx + w, y + yy + h);
                    guiGraphics.blit(x + xx, y + yy, 0, tile, tile, sprite);
                    guiGraphics.disableScissor();
                }
            }
        }
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
        //No structure
        final String noStructureKey = "tooltip."+EtherealVoid.MOD_ID+".pool_not_found";

        //Temperature Tooltip
        float currentTemp = getTemperature();
        final float totalTemperature = GameUtil.getPreferredTotalTemperature(currentTemp);
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
                tooltip.add(Component.literal(String.format("%,.2f / %,.2f K", currentTemp, totalTemperature))
                        .withStyle(ChatFormatting.GRAY));
            }
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            return;
        }

        //Pressure Tooltip
        float currentPress = getPressure();
        final float totalPressure = GameUtil.getPreferredTotalPressure(currentPress);
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
                tooltip.add(Component.literal(String.format("%,.2f / %,.2f atm", currentPress, totalPressure))
                        .withStyle(ChatFormatting.GRAY));
            }
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            return;
        }

        // --- 处理流体/沉淀显示区域的 Tooltip 触发 ---
        int fluidsTotalHeight = 106;
        int currentY = topPos + 121;
        if (poolBlockEntity == null) {
            if (mouseX >= barX && mouseX < barX+barWidth &&
                    mouseY >= currentY-fluidsTotalHeight+1 && mouseY < currentY+1) {
                guiGraphics.fill(barX, currentY-fluidsTotalHeight+1, barX+barWidth, currentY+1, 0x40FFFFFF);
                List<Component> tooltip = new ArrayList<>();
                Component fluidName = Component.translatable(noStructureKey);
                tooltip.add(fluidName.copy().withStyle(ChatFormatting.GOLD));
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                return;
            }
            return;
        }

        // 判定沉淀物悬浮提示
        if (!precipitates.isEmpty()) {
            if (mouseX >= barX && mouseX < barX+barWidth &&
                    mouseY >= currentY-6+1 && mouseY < currentY+1) {
                guiGraphics.fill(barX, currentY-6+1, barX+barWidth, currentY+1, 0x40FFFFFF);
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
            fluidsTotalHeight -= 6;
            currentY -= 6;
        }

        // 将流体列表重新分流用于悬浮高亮碰撞盒计算
        float currentAirDensity = 0.0012f;
        List<FluidStack> liquids = new ArrayList<>();
        List<FluidStack> gases = new ArrayList<>();
        for (FluidStack fs : fluids) {
            ResourceLocation rl = net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(fs.getFluid());
            float density = 1.0f;
            if (rl != null && FluidSorter.DENSITY_MAP.containsKey(rl.getPath())) {
                density = FluidSorter.DENSITY_MAP.get(rl.getPath());
            }
            if (density < currentAirDensity) {
                gases.add(fs);
            } else {
                liquids.add(fs);
            }
        }

        final int capacity = poolBlockEntity.getTankAll().getCapacity();

        // 碰撞计算 A：检测液体（从沉淀物处自底向上）
        int liquidY = currentY;
        for (FluidStack fluid : liquids) {
            int fluidHeight = Math.round(((float)fluid.getAmount() / (float)capacity) * (float)fluidsTotalHeight);
            if (mouseX >= barX && mouseX < barX+barWidth &&
                    mouseY >= liquidY-fluidHeight+1 && mouseY < liquidY+1) {
                guiGraphics.fill(barX, liquidY-fluidHeight+1, barX+barWidth, liquidY+1, 0x40FFFFFF);

                List<Component> tooltip = new ArrayList<>();
                Component fluidName = Component.translatable(fluid.getTranslationKey());
                tooltip.add(fluidName.copy().withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal(String.format("%,d / %,d mb", fluid.getAmount(), capacity))
                        .withStyle(ChatFormatting.GRAY));
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                return;
            }
            liquidY -= fluidHeight;
        }

        // 碰撞计算 B：检测气体（从槽顶端 topPos+16 自顶向下）
        int gasY = topPos + 16;
        for (FluidStack fluid : gases) {
            int fluidHeight = Math.round(((float)fluid.getAmount() / (float)capacity) * (float)fluidsTotalHeight);
            if (mouseX >= barX && mouseX < barX+barWidth &&
                    mouseY >= gasY && mouseY < gasY+fluidHeight) {
                guiGraphics.fill(barX, gasY, barX+barWidth, gasY+fluidHeight, 0x40FFFFFF);

                List<Component> tooltip = new ArrayList<>();
                Component fluidName = Component.translatable(fluid.getTranslationKey());
                tooltip.add(fluidName.copy().withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal(String.format("%,d / %,d mb", fluid.getAmount(), capacity))
                        .withStyle(ChatFormatting.GRAY));
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                return;
            }
            gasY += fluidHeight;
        }
    }
}