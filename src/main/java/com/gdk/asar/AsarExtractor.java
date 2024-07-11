package com.gdk.asar;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Extracts files from an {@link AsarArchive}
 */
public final class AsarExtractor {
    private AsarExtractor() {
    }

    /**
     * Extracts a single file from an {@link AsarArchive}
     *
     * @see AsarExtractor#extract(AsarArchive, String, File)
     */
    public static void extract(AsarArchive asar, String filePath, String destination) throws IOException {
        extract(asar, filePath, new File(destination));
    }

    /**
     * Extracts a single file from an {@link AsarArchive}
     *
     * @param asar        The source
     * @param filePath    The path inside the asar file of the wanted file
     * @param destination The {@link File} to save the extracted file
     * @throws IOException              If there's an error writing the file
     * @throws IllegalArgumentException If the path to extract doesn't exist
     */
    public static void extract(AsarArchive asar, String filePath, File destination) throws IOException {
        if (asar == null || filePath == null || destination == null) throw new NullPointerException();
        VirtualFile vf = null;
        for (VirtualFile v : asar) {
            if (v.getPath().equals(filePath)) {
                vf = v;
                break;
            }
        }
        if (vf == null) throw new IllegalArgumentException("No file " + filePath + " in the asar archive");
        extract(vf, destination);
    }

    /**
     * Extracts all the contents of an {@link AsarArchive} to a given folder
     *
     * @see AsarExtractor#extractAll(AsarArchive, File)
     */
    public static void extractAll(AsarArchive asar, String destination) throws IOException {
        extractAll(asar, new File(destination));
    }

    /**
     * Extracts all the contents of an {@link AsarArchive} to a given folder
     *
     * @param asar        The source asar
     * @param destination The destination folder
     * @throws IOException              If there's an error writing the files
     * @throws IllegalArgumentException If the destination is invalid
     */
    public static void extractAll(AsarArchive asar, File destination) throws IOException {
        if (asar == null || destination == null) {
            throw new NullPointerException();
        }
        if (destination.exists() && !destination.isDirectory()) {
            throw new IllegalArgumentException("destination must be a directory or not exist: "+ destination);
        }

        for (VirtualFile f : asar) {
            File d = new File(destination, f.getPath());
            File parentDir = d.getParentFile();
            if (!parentDir.exists() || !parentDir.isDirectory()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Error creating parent directories: "+ parentDir);
                }
            }
            extract(f, d);
        }
    }

    private static void extract(VirtualFile vf, File to) throws IOException {
        File toDir = to.getParentFile();
        if (!toDir.exists() || !toDir.isDirectory()) {
            if (!toDir.mkdirs()) {
                throw new IOException("Error creating parent directories: "+ toDir);
            }
        }

        try (RandomAccessFile raf = new RandomAccessFile(to, "rw");
             FileChannel fc = raf.getChannel()) {
            vf.read(fc.map(FileChannel.MapMode.READ_WRITE, 0, vf.getSize()));
        }
    }
}

