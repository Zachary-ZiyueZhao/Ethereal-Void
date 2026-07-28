package com.mjzaymi.etherealvoid.network;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.network.packet.SetFilterFluidC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // 💡 注册我们的过滤器流体设置数据包
        net.messageBuilder(SetFilterFluidC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SetFilterFluidC2SPacket::new)
                .encoder(SetFilterFluidC2SPacket::toBytes)
                .consumerMainThread(SetFilterFluidC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}