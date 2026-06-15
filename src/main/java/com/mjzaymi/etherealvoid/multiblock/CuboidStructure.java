package com.mjzaymi.etherealvoid.multiblock;

import com.mjzaymi.etherealvoid.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CuboidStructure {
    public static final int MIN_WIDTH = 3;
    public static final int MIN_HEIGHT = 2;
    public static final int MIN_DEPTH = 3;
    public static final int MAX_DIMENSION = 64;

    private final BlockPos min;
    private final BlockPos max;

    private final Set<BlockPos> members;

    public CuboidStructure(
            BlockPos min,
            BlockPos max,
            Set<BlockPos> members) {

        this.min=min;
        this.max=max;
        this.members=members;
    }

    public static Optional<CuboidStructure> findFromInterior(Level level, BlockPos interiorPos) {
        if (!isInterior(level, interiorPos)) {
            return Optional.empty();
        }

        Integer minX = findWall(level, interiorPos, Direction.WEST);
        Integer maxX = findWall(level, interiorPos, Direction.EAST);
        Integer minZ = findWall(level, interiorPos, Direction.NORTH);
        Integer maxZ = findWall(level, interiorPos, Direction.SOUTH);
        Integer bottomY = findBottom(level, interiorPos);

        if (minX == null || maxX == null || minZ == null || maxZ == null || bottomY == null) {
            return Optional.empty();
        }

        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;
        if (width < MIN_WIDTH || depth < MIN_DEPTH) {
            return Optional.empty();
        }

        int maxY = findWallTop(level, minX, maxX, bottomY, minZ, maxZ);
        if (maxY - bottomY + 1 < MIN_HEIGHT || interiorPos.getY() > maxY) {
            return Optional.empty();
        }

        BlockPos min = new BlockPos(minX, bottomY, minZ);
        BlockPos max = new BlockPos(maxX, maxY, maxZ);
        Set<BlockPos> members = collectMembers(level, min, max);
        CuboidStructure structure = new CuboidStructure(min, max, members);
        return structure.isValid(level) ? Optional.of(structure) : Optional.empty();
    }

    public int width() {
        return max.getX()-min.getX()+1;
    }

    public int height() {
        return max.getY()-min.getY()+1;
    }

    public int depth() {
        return max.getZ()-min.getZ()+1;
    }

    public BlockPos min() {
        return min;
    }

    public BlockPos max() {
        return max;
    }

    public Set<BlockPos> members() {
        return Set.copyOf(members);
    }

    public boolean containsInterior(BlockPos pos) {
        return pos.getX() > min.getX()
                && pos.getX() < max.getX()
                && pos.getY() > min.getY()
                && pos.getY() <= max.getY()
                && pos.getZ() > min.getZ()
                && pos.getZ() < max.getZ();
    }

    public boolean isValid(Level level) {
        return width() >= MIN_WIDTH
                && height() >= MIN_HEIGHT
                && depth() >= MIN_DEPTH
                && isValidShell(level);
    }

    private boolean isValidShell(Level level) {
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            boolean bottom = pos.getY() == min.getY();
            boolean edgeX = pos.getX() == min.getX() || pos.getX() == max.getX();
            boolean edgeZ = pos.getZ() == min.getZ() || pos.getZ() == max.getZ();
            boolean wall = edgeX || edgeZ;

            if (bottom) {
                if (!isSteelCasing(level.getBlockState(pos))) {
                    return false;
                }
            } else if (wall) {
                if (edgeX && edgeZ) {
                    if (!isSteelCasing(level.getBlockState(pos))) {
                        return false;
                    }
                } else if (!isWallPanel(level.getBlockState(pos))) {
                    return false;
                }
            } else if (!isInterior(level, pos)) {
                return false;
            }
        }
        return true;
    }

    private static Integer findWall(Level level, BlockPos start, Direction direction) {
        BlockPos.MutableBlockPos cursor = start.mutable();

        for (int distance = 1; distance <= MAX_DIMENSION; distance++) {
            cursor.move(direction);
            BlockState state = level.getBlockState(cursor);

            if (isWallPanel(state)) {
                return direction.getAxis() == Direction.Axis.X ? cursor.getX() : cursor.getZ();
            }

            if (!isInterior(level, cursor)) {
                return null;
            }
        }

        return null;
    }

    private static Integer findBottom(Level level, BlockPos start) {
        BlockPos.MutableBlockPos cursor = start.mutable();

        for (int distance = 1; distance <= MAX_DIMENSION; distance++) {
            cursor.move(Direction.DOWN);
            BlockState state = level.getBlockState(cursor);

            if (isSteelCasing(state)) {
                return cursor.getY();
            }

            if (!isInterior(level, cursor)) {
                return null;
            }
        }

        return null;
    }

    private static int findWallTop(Level level, int minX, int maxX, int bottomY, int minZ, int maxZ) {
        int maxY = bottomY;

        for (int y = bottomY + 1; y <= bottomY + MAX_DIMENSION - 1; y++) {
            if (!isWallLayer(level, minX, maxX, y, minZ, maxZ)) {
                break;
            }
            maxY = y;
        }

        return maxY;
    }

    private static boolean isWallLayer(Level level, int minX, int maxX, int y, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean edgeX = x == minX || x == maxX;
                boolean edgeZ = z == minZ || z == maxZ;

                if (!edgeX && !edgeZ) {
                    continue;
                }

                BlockState state = level.getBlockState(new BlockPos(x, y, z));
                if (edgeX && edgeZ) {
                    if (!isSteelCasing(state)) {
                        return false;
                    }
                } else if (!isWallPanel(state)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Set<BlockPos> collectMembers(Level level, BlockPos min, BlockPos max) {
        Set<BlockPos> result = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (isSteelCasing(state) || isWallPanel(state)) {
                result.add(pos.immutable());
            }
        }
        return result;
    }

    private static boolean isInterior(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }

    private static boolean isWallPanel(BlockState state) {
        return isSteelCasing(state) || state.is(ModBlocks.ANTI_CORROSION_GLASS.get())
                || state.is(ModBlocks.POOL_MONITOR.get());
    }

    private static boolean isSteelCasing(BlockState state) {
        return state.is(ModBlocks.STEEL_CASING.get());
    }
}
