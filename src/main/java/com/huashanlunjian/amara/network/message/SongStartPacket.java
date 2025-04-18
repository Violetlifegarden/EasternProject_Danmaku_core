package com.huashanlunjian.amara.network.message;

import com.huashanlunjian.amara.entity.songs.Boss;
import com.huashanlunjian.amara.music_game_core.AbstractChart;
import com.huashanlunjian.amara.utils.ChartUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.huashanlunjian.amara.utils.FileUtil.getResourceLocation;

public record SongStartPacket(String path,String chartPath)implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SongStartPacket> TYPE = new CustomPacketPayload.Type<>(getResourceLocation("songs_start"));
    public static final StreamCodec<ByteBuf,SongStartPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SongStartPacket :: path,
            ByteBufCodecs.STRING_UTF8,
            SongStartPacket :: chartPath,
            SongStartPacket::new
    );

    public static void handle(SongStartPacket msg, IPayloadContext context){

        if (context.flow().isServerbound()){
            context.enqueueWork(() -> {
                CompletableFuture.supplyAsync(() -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(3000);
                        Boss boss = new Boss(context.player(),msg.chartPath(), audioFile(msg.path));
                        boss.level().addFreshEntity(boss);
                    } catch (InterruptedException ignored) {
                    }

                    //boss.setCustomName(boss.getDisplayName());

                    return null;
                });
            });
        }
    }
//    public static AbstractChart chart(String chartPath){
//        return ChartUtil.loadChart(Path.of(chartPath));
//    }

    @Deprecated
    public static Path audioFile(String resource) {
        return Path.of(resource);//.resolve("audio.ogg");
    }

    @Deprecated
    public static Path resourcePath(String resource) {
        Path path = Path.of(resource);
        return path.resolve(resource);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
