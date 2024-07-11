# Java解析 Electron asar 格式文件

## 用法：
```java
File f = new File("/home/test.asar");

AsarArchive archive = new AsarArchive(f);

// 获取头部元数据以JSON格式输出
System.out.println(archive.getHeader().getJson());

// 遍历asar中的所有文件基本信息(文件路径、大小、偏移量)
for (VirtualFile vf : archive) {
    System.out.println(vf);
}

// 解压asar文件到指定目录下
File dir = new File("/home/asar-unpack");
if (!dir.exists()) {
    dir.mkdirs();
}
AsarExtractor.extractAll(archive, dir.getAbsolutePath());
```