# Java解析 Electron asar 格式文件

## 用法：
```java
File f = new File("demo.asar");

AsarArchive archive = new AsarArchive(f);
System.out.println(archive.getHeader().getJson());

// 迭代asar中的所有文件元数据
for (VirtualFile vf : archive) {
    System.out.println(vf);
}

// 解压asar文件到当前asar-unpack目录下
File dir = new File("asar-unpack");
if (!dir.exists()) {
    dir.mkdirs();
}
// 解压全部文件
AsarExtractor.extractAll(archive, dir.getAbsolutePath());

// 获取某个文件的内容
byte[] iconPng = archive.read("img/chrome.png");
Files.write(new File(dir, "chrome22.png").toPath(), iconPng);

byte[] readme = archive.read("readme.md");
String readmeContent = new String(readme, StandardCharsets.UTF_8);
System.out.println("readme.md 文件内容："+ readmeContent);
if (!"hello asar".equals(readmeContent)) {
    throw new RuntimeException("读取 readme.md 文件失败，内容不匹配");
}
```