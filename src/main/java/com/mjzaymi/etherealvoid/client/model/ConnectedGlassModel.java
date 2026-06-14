package com.mjzaymi.etherealvoid.client.model;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.registrations.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.ArrayList;
import java.util.List;

public class ConnectedGlassModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<BlockAndTintGetter> LEVEL = new ModelProperty<>();
    private static final ModelProperty<BlockPos> POS = new ModelProperty<>();

    private static final ResourceLocation[] TILE_LOCATIONS = new ResourceLocation[47];

    private static final int UP = 1;
    private static final int RIGHT = 2;
    private static final int DOWN = 4;
    private static final int LEFT = 8;
    private static final int UP_LEFT = 16;
    private static final int UP_RIGHT = 32;
    private static final int DOWN_RIGHT = 64;
    private static final int DOWN_LEFT = 128;

    private static final int[] CTM_TILES = {
            0, 36, 1, 16, 12, 24, 4, 6, 3, 17, 2, 18, 5, 19, 7, 46,
            0, 36, 1, 16, 12, 24, 4, 6, 3, 39, 2, 42, 5, 41, 7, 20,
            0, 36, 1, 37, 12, 24, 4, 30, 3, 17, 2, 40, 5, 19, 7, 8,
            0, 36, 1, 37, 12, 24, 4, 30, 3, 39, 2, 38, 5, 41, 7, 11,
            0, 36, 1, 16, 12, 24, 13, 28, 3, 17, 2, 18, 5, 19, 31, 9,
            0, 36, 1, 16, 12, 24, 13, 28, 3, 39, 2, 42, 5, 41, 31, 35,
            0, 36, 1, 37, 12, 24, 13, 25, 3, 17, 2, 40, 5, 19, 31, 23,
            0, 36, 1, 37, 12, 24, 13, 25, 3, 39, 2, 38, 5, 41, 31, 33,
            0, 36, 1, 16, 12, 24, 4, 6, 3, 17, 2, 18, 15, 43, 29, 21,
            0, 36, 1, 16, 12, 24, 4, 6, 3, 39, 2, 42, 15, 27, 29, 10,
            0, 36, 1, 37, 12, 24, 4, 30, 3, 17, 2, 40, 15, 43, 29, 34,
            0, 36, 1, 37, 12, 24, 4, 30, 3, 39, 2, 38, 15, 27, 29, 32,
            0, 36, 1, 16, 12, 24, 13, 28, 3, 17, 2, 18, 15, 43, 14, 22,
            0, 36, 1, 16, 12, 24, 13, 28, 3, 39, 2, 42, 15, 27, 14, 44,
            0, 36, 1, 37, 12, 24, 13, 25, 3, 17, 2, 40, 15, 43, 14, 45,
            0, 36, 1, 37, 12, 24, 13, 25, 3, 39, 2, 38, 15, 27, 14, 26
    };

    static {
        for (int i = 0; i < TILE_LOCATIONS.length; i++) {
            TILE_LOCATIONS[i] = new ResourceLocation(EtherealVoid.MOD_ID, "block/anti_corrosion_glass/" + i);
        }
    }

    public ConnectedGlassModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return super.getModelData(level, pos, state, modelData)
                .derive()
                .with(LEVEL, level)
                .with(POS, pos)
                .build();
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random, ModelData modelData, RenderType renderType) {
        List<BakedQuad> quads = originalModel.getQuads(state, side, random, modelData, renderType);
        if (state == null || side == null || quads.isEmpty() || !modelData.has(LEVEL) || !modelData.has(POS)) {
            return quads;
        }

        BlockAndTintGetter level = modelData.get(LEVEL);
        BlockPos pos = modelData.get(POS);
        int tile = CTM_TILES[connectionMask(level, pos, side)];
        TextureAtlasSprite sprite = tileSprite(tile);

        List<BakedQuad> connectedQuads = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            connectedQuads.add(remapSprite(quad, sprite));
        }
        return connectedQuads;
    }

    private static int connectionMask(BlockAndTintGetter level, BlockPos pos, Direction side) {
        FaceAxes axes = FaceAxes.forSide(side);
        int mask = 0;

        if (connectsTo(level, pos.relative(axes.up))) {
            mask |= UP;
        }
        if (connectsTo(level, pos.relative(axes.right))) {
            mask |= RIGHT;
        }
        if (connectsTo(level, pos.relative(axes.down))) {
            mask |= DOWN;
        }
        if (connectsTo(level, pos.relative(axes.left))) {
            mask |= LEFT;
        }
        if ((mask & (UP | LEFT)) == (UP | LEFT) && connectsTo(level, pos.relative(axes.up).relative(axes.left))) {
            mask |= UP_LEFT;
        }
        if ((mask & (UP | RIGHT)) == (UP | RIGHT) && connectsTo(level, pos.relative(axes.up).relative(axes.right))) {
            mask |= UP_RIGHT;
        }
        if ((mask & (DOWN | RIGHT)) == (DOWN | RIGHT) && connectsTo(level, pos.relative(axes.down).relative(axes.right))) {
            mask |= DOWN_RIGHT;
        }
        if ((mask & (DOWN | LEFT)) == (DOWN | LEFT) && connectsTo(level, pos.relative(axes.down).relative(axes.left))) {
            mask |= DOWN_LEFT;
        }

        return mask;
    }

    private static boolean connectsTo(BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.ANTI_CORROSION_GLASS.get());
    }

    private static TextureAtlasSprite tileSprite(int tile) {
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(TILE_LOCATIONS[tile]);
    }

    private static BakedQuad remapSprite(BakedQuad quad, TextureAtlasSprite targetSprite) {
        TextureAtlasSprite sourceSprite = quad.getSprite();
        int[] vertices = quad.getVertices().clone();

        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 8;
            float sourceU = Float.intBitsToFloat(vertices[offset + 4]);
            float sourceV = Float.intBitsToFloat(vertices[offset + 5]);
            vertices[offset + 4] = Float.floatToRawIntBits(targetSprite.getU(sourceSprite.getUOffset(sourceU)));
            vertices[offset + 5] = Float.floatToRawIntBits(targetSprite.getV(sourceSprite.getVOffset(sourceV)));
        }

        return new BakedQuad(
                vertices,
                quad.getTintIndex(),
                quad.getDirection(),
                targetSprite,
                quad.isShade(),
                quad.hasAmbientOcclusion()
        );
    }

    private record FaceAxes(Direction up, Direction right, Direction down, Direction left) {
        private static FaceAxes forSide(Direction side) {
            return switch (side) {
                //这里东南西北在java代码里其实搞反了，但是实际上因为材质匹配搞反了，干脆就改这里，能用就行
                case DOWN -> new FaceAxes(Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST);
                case UP -> new FaceAxes(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
                case SOUTH -> new FaceAxes(Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST);
                case NORTH -> new FaceAxes(Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST);
                case EAST -> new FaceAxes(Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH);
                case WEST -> new FaceAxes(Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH);
            };
        }
    }
}
