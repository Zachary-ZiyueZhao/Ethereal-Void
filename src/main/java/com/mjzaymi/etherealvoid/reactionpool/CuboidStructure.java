package com.mjzaymi.etherealvoid.reactionpool;

import com.mjzaymi.etherealvoid.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

    private final Set<BlockPos> interiors;

    public CuboidStructure(
            BlockPos min,
            BlockPos max,
            Set<BlockPos> members) {

        this.min=min;
        this.max=max;
        this.members=members;
        Set<BlockPos> blockPosSet = new HashSet<>();
        for (int x = min.getX()+1; x < max.getX(); x++) {
            for (int y = min.getY()+1; y <= max.getY(); y++) {
                for (int z = min.getZ()+1; z < max.getZ(); z++) {
                    blockPosSet.add(new BlockPos(x, y, z));
                }
            }
        }
        this.interiors=blockPosSet;
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

    public static Optional<CuboidStructure> findFromWallAndCorner(Level level, BlockPos pos) {
        var result = findFromWall(level, pos);
        if (result.isPresent()) return result;
        return findFromCorner(level, pos);
    }

    public static Optional<CuboidStructure> findFromWall(Level level, BlockPos wallPos) {

        BlockState state = level.getBlockState(wallPos);

        if (!isWallPanel(state) && !isSteelCasing(state)) {
            return Optional.empty();
        }

        for (Direction dir : Direction.values()) {

            BlockPos adjacent = wallPos.relative(dir);

            if (!isInterior(level, adjacent)) {
                continue;
            }

            Optional<CuboidStructure> result =
                    findFromInterior(level, adjacent);

            if (result.isPresent()
                    && result.get().members().contains(wallPos)) {

                return result;
            }
        }

        return Optional.empty();
    }

    public static Optional<CuboidStructure> findFromCorner(Level level, BlockPos pos) {

        BlockState state = level.getBlockState(pos);

        if (!isWallPanel(state) && !isSteelCasing(state)) {
            return Optional.empty();
        }

        for (int i=-1;i<1;i+=2)
            for (int j=-1;j<1;j+=2)
                for (int k=-1;k<1;k+=2) {
                    BlockPos adjacent = pos.offset(-i, -j, -k);
                    if (!isInterior(level, adjacent)) continue;

                    Optional<CuboidStructure> result =
                            findFromInterior(level, adjacent);

                    if (result.isPresent()
                            && result.get().members().contains(pos)) {

                        return result;
                    }
                }

        return Optional.empty();
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

    public Set<BlockPos> interiors() {
        return Set.copyOf(interiors);
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
        return true;//level.getBlockState(pos).isAir();
    }

    private static boolean isWallPanel(BlockState state) {
        return isSteelCasing(state) || state.is(ModBlocks.ANTI_CORROSION_GLASS.get())
                || state.is(ModBlocks.POOL_MONITOR.get());
    }

    private static boolean isSteelCasing(BlockState state) {
        return state.is(ModBlocks.STEEL_CASING.get());
    }

    public void dropInterior(Level level) {
        for (BlockPos pos : interiors) {
            BlockState blockState = level.getBlockState(pos);
            if (blockState.isAir()) continue;
            Block.dropResources(blockState, level, pos);
            level.removeBlock(pos, false);
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        CompoundTag minTag = new CompoundTag();
        minTag.putInt("x", min.getX());
        minTag.putInt("y", min.getY());
        minTag.putInt("z", min.getZ());
        root.put("min", minTag);
        CompoundTag maxTag = new CompoundTag();
        maxTag.putInt("x", max.getX());
        maxTag.putInt("y", max.getY());
        maxTag.putInt("z", max.getZ());
        root.put("max", maxTag);
        ListTag list = new ListTag();
        for (BlockPos pos : members) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            list.add(tag);
        }
        root.put("members", list);
        return root;
    }

    public static CuboidStructure deserializeNBT(CompoundTag root) {
        if (root == null || root.isEmpty())
            return null;
        Set<BlockPos> members = new HashSet<>();
        ListTag list = root.getList("members", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag tag = (CompoundTag) t;

            members.add(new BlockPos(
                    tag.getInt("x"),
                    tag.getInt("y"),
                    tag.getInt("z")
            ));
        }
        return new CuboidStructure(
                new BlockPos(root.getCompound("min").getInt("x"),
                        root.getCompound("min").getInt("y"),
                        root.getCompound("min").getInt("z")),
                new BlockPos(root.getCompound("max").getInt("x"),
                        root.getCompound("max").getInt("y"),
                        root.getCompound("max").getInt("z")),
                members);
    }

    public boolean isEqual(CuboidStructure cuboidStructure) {
        return cuboidStructure != null &&
                cuboidStructure.max.compareTo(max) == 0 &&
                cuboidStructure.min.compareTo(min) == 0;
    }
}
