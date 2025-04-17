package com.huashanlunjian.amara.utils.sounds;

import io.github.jaredmdobson.concentus.OpusDecoder;
import io.github.jaredmdobson.concentus.OpusException;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 暂时用不到
 * Opus解码器工具类
 */
@OnlyIn(Dist.CLIENT)
public final class OpusDecoderUtil {

    private static final int MAX_FRAME_SIZE = 5;

    public static Pair<AudioFormat, byte[]> decode(byte[] data) throws IOException, OpusException {
        try (OpusFile opusFile = new OpusFile(new OggFile(new ByteArrayInputStream(data)));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int sampleRate = opusFile.getInfo().getSampleRate();
            int channels = opusFile.getInfo().getNumChannels();
            int frameSize = sampleRate * MAX_FRAME_SIZE / 1000;

            OpusDecoder decoder = new OpusDecoder(sampleRate, channels);
            byte[] pcmBytes = new byte[sampleRate * channels * 2];
            OpusAudioData packet;

            while ((packet = opusFile.getNextAudioPacket()) != null) {
                byte[] packetBytes = packet.getData();
                int packetLength = packetBytes.length;
                int samplesDecoded = decoder.decode(packetBytes, 0, packetLength, pcmBytes, 0, frameSize, false);
                outputStream.write(pcmBytes, 0, samplesDecoded * channels * 2);
            }
            byte[] outputData = outputStream.toByteArray();
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16,
                    channels, channels * 2, sampleRate, false);
            return Pair.of(audioFormat, outputData);
        }
    }
}
