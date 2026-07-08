package com.mjzaymi.etherealvoid.screen;

import com.mjzaymi.etherealvoid.blockentity.VirtualMinerBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModBlocks;
import com.mjzaymi.etherealvoid.registration.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class VirtualMinerMenu extends AbstractContainerMenu {
    public final VirtualMinerBlockEntity blockEntity;
    private final Level level;

    // 1. 客户端构造函数：从网络里读取 BlockPos，然后抓取真正的 BlockEntity 🌟
    public VirtualMinerMenu(int windowId, Inventory inv, FriendlyByteBuf extraData) {
        this(windowId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // 2. 主构造函数
    public VirtualMinerMenu(int windowId, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.VIRTUAL_MINER_MENU.get(), windowId);
        this.blockEntity = (VirtualMinerBlockEntity) entity;
        this.level = inv.player.level();

        // 🌟 严格对齐示例：先加玩家背包和快捷栏（占用槽位索引 0 - 35）
        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // 🌟 再加采矿机的 27 个格子（占用槽位索引 36 - 62）
        if (this.blockEntity != null) {
            this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 9; col++) {
                        // 这里的坐标 (8 + col * 18, 18 + row * 18) 完美对应原版 9x3 箱子贴图的格子
                        this.addSlot(new SlotItemHandler(handler, col + row * 9, 8 + col * 18, 18 + row * 18));
                    }
                }
            });
        }
    }

    // 槽位索引常量定义（完全对应上面的摆放顺序）
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT; // 36
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT; // 36

    private static final int TE_INVENTORY_SLOT_COUNT = 27; // 采矿机拥有 27 个格子 🌟

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // 如果点击的是玩家背包/快捷栏 (0-35) -> 往采矿机里塞 (36-62)
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }
        // 如果点击的是采矿机里的格子 (36-62) -> 往玩家背包里塞 (0-35)
        else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
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
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.VIRTUAL_MINER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}