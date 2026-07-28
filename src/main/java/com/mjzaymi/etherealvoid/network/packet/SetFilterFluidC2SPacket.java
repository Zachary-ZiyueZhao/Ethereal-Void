package com.mjzaymi.etherealvoid.network.packet;

import com.mjzaymi.etherealvoid.registration.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetFilterFluidC2SPacket {
    private final String fluidName;
    private final boolean isCarried;

    public SetFilterFluidC2SPacket(String fluidName, boolean isCarried) {
        this.fluidName = fluidName;
        this.isCarried = isCarried;
    }

    public SetFilterFluidC2SPacket(FriendlyByteBuf buf) {
        this.fluidName = buf.readUtf();
        this.isCarried = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(fluidName);
        buf.writeBoolean(isCarried);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 判断要修改的过滤器是在鼠标上抓着，还是在主手拿着
                ItemStack targetStack = isCarried ? player.containerMenu.getCarried() : player.getMainHandItem();

                if (targetStack.getItem() == ModItems.FLUID_PIPE_FILTER.get()) {
                    // 给物品写入流体注册名 tag
                    targetStack.getOrCreateTag().putString("FilterFluid", fluidName);
                }
            }
        });
        return true;
    }
}