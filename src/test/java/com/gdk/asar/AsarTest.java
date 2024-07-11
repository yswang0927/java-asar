package com.gdk.asar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AsarTest {
    public static void main(String[] args) throws IOException {
        File f = new File("app.asar");

        AsarArchive archive = new AsarArchive(f);
        System.out.println(archive.getHeader().getJson());

        // 迭代asar中的所有文件元数据
        for (VirtualFile vf : archive) {
            System.out.println(vf);
        }
        
        // 获取某个文件的内容
        /*byte[] iconPng = archive.content("icon.png");
        Files.write(Paths.get("/data/Downloads", "icon11111111.png"), iconPng);*/

        // 解压asar文件到当前asar目录下
        File dir = new File("asar-unpack");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        AsarExtractor.extractAll(archive, dir.getAbsolutePath());

    }
}
