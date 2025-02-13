package com.gdk.asar;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a .asar file.
 */
public class AsarArchive implements Closeable, Iterable<VirtualFile> {
    private final String path;
    private final RandomAccessFile file;
    private final int baseOffset;
    private final Header header;
    //private final Set<VirtualFile> set;
    private final ConcurrentHashMap<String, VirtualFile> files = new ConcurrentHashMap<>(256);

    public AsarArchive(File file, boolean threadSafe) throws IOException {
        this.path = file.getAbsolutePath();
        this.file = new RandomAccessFile(file, "r");
        FileChannel fc = this.file.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, 8);
        int headerSize = (int) (mbb.order(ByteOrder.LITTLE_ENDIAN).getInt(4) & 0xFFFFFFFFL);
        this.baseOffset = headerSize + 8;
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, 16, headerSize);
        try {
            byte[] headerBytes = new byte[headerSize];
            mbb.get(headerBytes);
            this.header = new Header(headerSize, new JSONObject(new String(headerBytes, StandardCharsets.UTF_8)));
        } catch (JSONException e) {
            close();
            throw new AsarException("Invalid header", e);
        }

        files(this, "", header.getJson().getJSONObject("files"), files);

        //this.set = threadSafe ? new CopyOnWriteArraySet<>() : new HashSet<>();
        //files(this, "", header.getJson().getJSONObject("files"), set);
    }

    public AsarArchive(File file) throws IOException {
        this(file, false);
    }

    public AsarArchive(String path, boolean threadSafe) throws IOException {
        this(new File(path), threadSafe);
    }

    public AsarArchive(String path) throws IOException {
        this(new File(path), false);
    }

    private static void files(AsarArchive asar, String path, JSONObject obj, ConcurrentHashMap<String, VirtualFile> files) throws JSONException {
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject o = obj.getJSONObject(key);
            JSONObject filesObj = o.optJSONObject("files");
            String resPath = path.isEmpty() ? key : (path + '/' + key);
            if (filesObj != null) {
                files(asar, resPath, filesObj, files);
            } else {
                files.putIfAbsent(resPath, new VirtualFile(asar,
                        resPath,
                        (o.has("offset") ? o.getInt("offset") : 0) + asar.baseOffset,
                        o.has("size") ? o.getInt("size") : 0
                ));
            }
        }
    }

    /*private static void files(AsarArchive asar, String path, JSONObject obj, Set<VirtualFile> files) throws JSONException {
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject o = obj.getJSONObject(key);
            JSONObject filesObj = o.optJSONObject("files");
            if (filesObj != null) {
                files(asar, path + "/" + key, filesObj, files);
            } else {
                files.add(new VirtualFile(asar, path + "/" + key, Integer.parseInt(o.getString("offset")) + asar.baseOffset, o.getInt("size")));
            }
        }
    }*/

    public boolean contains(String filepath) {
        return this.files.containsKey(filepath);
    }

    public byte[] read(String filepath) {
        VirtualFile vFile = this.files.get(filepath);
        if (vFile == null) {
            return new byte[0];
        }
        return vFile.read();
    }

    MappedByteBuffer read(long offset, long size) throws IOException {
        return file.getChannel().map(FileChannel.MapMode.READ_ONLY, offset, size);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    @Override
    public Iterator<VirtualFile> iterator() {
        return this.files.values().iterator();
        //return set.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AsarArchive) {
            AsarArchive o = (AsarArchive) other;
            return o.baseOffset == baseOffset && Objects.equals(path, o.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, baseOffset);
    }

    /**
     * Returns the path of the loaded asar file
     *
     * @return The path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the {@link Header} of this file
     *
     * @return The header
     */
    public Header getHeader() {
        return header;
    }

    /**
     * Returns the base offset for the files inside this file, in bytes
     *
     * @return The offset
     */
    public int getBaseOffset() {
        return baseOffset;
    }
}
