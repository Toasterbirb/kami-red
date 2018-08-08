package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.render.ChunkFinder;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author 086
 */
@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handleChunkData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;read(Lnet/minecraft/network/PacketBuffer;IZ)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void read(SPacketChunkData data, CallbackInfo info, Chunk chunk) {
        if (!data.isFullChunk())
            ChunkFinder.newChunk(chunk);
    }

}
