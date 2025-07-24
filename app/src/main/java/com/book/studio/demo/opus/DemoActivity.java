package com.book.studio.demo.opus;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.book.studio.demo.R;
import com.book.studio.opus.OpusBridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class DemoActivity extends AppCompatActivity {

    // 需要识别的文件
    private static final String ASSETS_NAME = "test_audio.pcm";
    byte[] opusByteBuffer = new byte[4096];
    private long opusEncoder;
    private long opusDecoder;

    private final short[] pcmBuffer = new short[1920];

    private String outputOpusFile;
    private String outputPcmFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_layout);
        opusEncoder = OpusBridge.createEncoder(16000, 1, 10);
        opusDecoder = OpusBridge.createDecoder(16000, 1);
        outputOpusFile = getFilesDir().getAbsoluteFile() + "/test_audio.opus";
        outputPcmFile = getFilesDir().getAbsoluteFile() + "/test_audio.pcm";
    }

    public void onToPcmClick(View view) {
        if (TextUtils.isEmpty(outputOpusFile)) {
            Toast.makeText(this, "请先生成 opus 文件", Toast.LENGTH_SHORT);
            return;
        }
        File tempFile = new File(outputOpusFile);
        if (!tempFile.isFile()) {
            Toast.makeText(this, "请先生成 opus 文件", Toast.LENGTH_SHORT);
            return;
        }
        readOpusFile(outputOpusFile, outputPcmFile);
    }

    public void onToOpusClick(View view) {
        readPcmFile(outputOpusFile);
    }

    private void readOpusFile(String inputPath, String outputPath) {
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);
        int chunkSize = 80; // 比如 20ms 的 16bit 单声道 PCM 数据，320 samples = 640 bytes

        try (InputStream inputStream = new FileInputStream(inputFile);
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] actualBuffer;
                if (bytesRead < chunkSize) {
                    actualBuffer = new byte[bytesRead];
                    System.arraycopy(buffer, 0, actualBuffer, 0, bytesRead);
                } else {
                    actualBuffer = buffer;
                }
                short[] encoded = opusDecode(actualBuffer);
                if (encoded.length > 0) {
                    outputStream.write(OpusBridge.toByteArray(encoded));
                }
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void readPcmFile(String outputPath) {
        File outputFile = new File(outputPath);
        // 比如 20ms 的 16bit 单声道 PCM 数据，320 samples = 640 bytes
        int chunkSize = 320 * 2;
        try (InputStream inputStream = getAssets().open(ASSETS_NAME);
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] actualBuffer;
                if (bytesRead < chunkSize) {
                    actualBuffer = new byte[bytesRead];
                    System.arraycopy(buffer, 0, actualBuffer, 0, bytesRead);
                } else {
                    actualBuffer = buffer;
                }
                byte[] encoded = opusEncode(actualBuffer);
                if (encoded.length > 0) {
                    outputStream.write(encoded);
                }
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] opusEncode(byte[] buffer) {
        // 将 byte[] PCM 数据转换成 short[]
        short[] pcmShorts = OpusBridge.toShortArray(buffer);
        // OpusTools.encode 会把编码后的数据写入 opusByteBuffer
        int encodeSize = OpusBridge.encode(opusEncoder, pcmShorts, 0, opusByteBuffer);
        if (encodeSize <= 0) {
            return new byte[0];
        }
        return Arrays.copyOf(opusByteBuffer, encodeSize);
    }

    private short[] opusDecode(byte[] buffer) {
        if (buffer == null || buffer.length == 0) {
            return new short[0]; // 空输入
        }
        // 解码：将 Opus 压缩数据转为 PCM 数据
        int decodedSamples = OpusBridge.decode(opusDecoder, buffer, pcmBuffer);
        if (decodedSamples <= 0) {
            return new short[0]; // 解码失败
        }
        // 返回解码后的实际 PCM 数据（复制出精确长度）
        return Arrays.copyOf(pcmBuffer, decodedSamples);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (opusEncoder != 0) {
            OpusBridge.destroyEncoder(opusEncoder);
        }
        if (opusDecoder != 0) {
            OpusBridge.destroyDecoder(opusDecoder);
        }
    }
}
