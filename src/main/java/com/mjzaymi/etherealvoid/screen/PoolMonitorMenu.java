package com.mjzaymi.etherealvoid.screen;

import com.mjzaymi.etherealvoid.blockentity.PoolMonitorBlockEntity;
import com.mjzaymi.etherealvoid.blockentity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModBlocks;
import com.mjzaymi.etherealvoid.registration.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class PoolMonitorMenu extends AbstractContainerMenu {
    public final PoolMonitorBlockEntity blockEntity;
    private final Level level;

    // 💡 新增：专门负责双端同步温度和气压的数据插槽
    private final ContainerData containerData;

    public PoolMonitorMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public PoolMonitorMenu(int pContainerId, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.POOL_MONITOR_MENU.get(), pContainerId);
        checkContainerSize(inv, 2);
        blockEntity = ((PoolMonitorBlockEntity) entity);
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 122, 106));
        });

        this.containerData = new ContainerData() {
            // 客户端本地用于缓存 4 个通道数据的数组
            private final int[] clientBuffers = new int[4];

            @Override
            public int get(int index) {
                if (level.isClientSide) {
                    return clientBuffers[index];
                }
                ReactionPoolBlockEntity pool = getPoolBlockEntity();
                if (pool == null) return 0;

                // 根据索引获取对应的 32 位二进制位
                int bits = (index < 2)
                        ? Float.floatToIntBits(pool.getTemperature())
                        : Float.floatToIntBits(pool.getPressure());

                // 💡 核心修复：将 32 位 int 拆分为高 16 位和低 16 位传输，完美避开原版 short 网络截断！
                if (index == 0 || index == 2) {
                    return (bits >> 16) & 0xFFFF; // 获取高 16 位
                } else {
                    return bits & 0xFFFF;        // 获取低 16 位
                }
            }

            @Override
            public void set(int index, int value) {
                // 客户端接收服务端发来的 16 位片段并存入缓存
                clientBuffers[index] = value;
            }

            @Override
            public int getCount() {
                return 4; // 升级为 4 个数据插槽
            }
        };

        // 极其重要：向原版 Menu 注册这个升级后的 4 通道同步管线
        this.addDataSlots(this.containerData);
    }

    // 💡 完美复原：在客户端将两个 16 位 short 拼接回 32 位 int，再还原为标准的 float
    public float getSyncedTemperature() {
        int high = this.containerData.get(0) & 0xFFFF;
        int low = this.containerData.get(1) & 0xFFFF;
        int bits = (high << 16) | low;
        return Float.intBitsToFloat(bits);
    }

    public float getSyncedPressure() {
        int high = this.containerData.get(2) & 0xFFFF;
        int low = this.containerData.get(3) & 0xFFFF;
        int bits = (high << 16) | low;
        return Float.intBitsToFloat(bits);
    }

    public ReactionPoolBlockEntity getPoolBlockEntity() {
        return blockEntity.getPoolBlockEntity();
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 1;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.POOL_MONITOR.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 138 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 196));
        }
    }
}