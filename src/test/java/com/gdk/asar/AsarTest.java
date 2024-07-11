package com.gdk.asar;

import java.io.File;
import java.io.IOException;

public class AsarTest {
    public static void main(String[] args) throws IOException {
        File f = new File("obsidian.asar");

        AsarArchive archive = new AsarArchive(f);
        System.out.println(archive.getHeader().getJson());

        // 迭代asar中的所有文件元数据
        /*for (VirtualFile vf : archive) {
            System.out.println(vf);
        }*/

        // 解压asar文件到当前asar目录下
        File dir = new File("asar");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        AsarExtractor.extractAll(archive, dir.getAbsolutePath());

    }
}
