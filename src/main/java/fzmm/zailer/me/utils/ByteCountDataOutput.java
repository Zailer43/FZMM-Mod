package fzmm.zailer.me.utils;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;

public class ByteCountDataOutput implements DataOutput {
    private static ByteCountDataOutput instance = null;
    private long count;

    private ByteCountDataOutput() {
        this.count = 0;
    }

    public static ByteCountDataOutput getInstance() {
        if (instance == null)
            instance = new ByteCountDataOutput();

        return instance;
    }

    public long getCount() {
        return this.count;
    }

    public void reset() {
        this.count = 0;
    }

    @Override
    public void write(int b) {
        this.count++;
    }

    @Override
    public void write(byte[] b) {
        this.count += b.length;
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) {
        this.count += len;
    }

    @Override
    public void writeBoolean(boolean v) {
        this.count++;
    }

    @Override
    public void writeByte(int v) {
        this.count++;
    }

    @Override
    public void writeShort(int v) {
        this.count += 2;
    }

    @Override
    public void writeChar(int v) {
        this.count += 2;
    }

    @Override
    public void writeInt(int v) {
        this.count += 4;
    }

    @Override
    public void writeLong(long v) {
        this.count += 8;
    }

    @Override
    public void writeFloat(float v) {
        this.count += 4;
    }

    @Override
    public void writeDouble(double v) {
        this.count += 8;
    }

    @Override
    public void writeBytes(String s) {
        this.count += s.length();
    }

    @Override
    public void writeChars(String s) {
        this.count += s.length() * 2L;
    }

    @Override
    public void writeUTF(@NotNull String s) {
        this.count += 2 + getUTFLength(s);
    }

    public int getUTFLength(String s) {
        int count = 0;
        for (int i = 0, len = s.length(); i < len; i++) {
            char ch = s.charAt(i);
            if (ch <= 0x7F)
                count++;
            else if (ch <= 0x7FF)
                count += 2;
            else if (Character.isHighSurrogate(ch)) {
                count += 4;
                ++i;
            } else
                count += 3;
        }
        return count;
    }
}