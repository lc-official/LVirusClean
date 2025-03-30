import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class LVirusClean {

    public static void main(String[] args) {
        if (args.length == 0) return;
        try {
            // 创建原始文件对象
            File originFile = new File(args[0]);
            // 创建临时文件对象
            File newFile = File.createTempFile("lvirus-cleaned", ".jar");
            // 创建ZipFile对象，读取原始文件
            ZipFile jarFile = new ZipFile(originFile);
            // 创建ZipOutputStream对象，写入临时文件
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(newFile));
            // 创建AtomicReference对象，用于存储修改后的类名
            AtomicReference<String> name = new AtomicReference<>("");
            // 创建HashMap对象，用于存储ZipEntry和对应的字节数组
            HashMap<ZipEntry, byte[]> map = new HashMap<>();
            // 遍历原始文件中的所有ZipEntry
            jarFile.stream().forEach(entry -> {
                try {
                    // 创建新的ZipEntry对象
                    ZipEntry e = new ZipEntry(entry.getName());
                    // 读取ZipEntry对应的字节数组
                    byte[] bytes = jarFile.getInputStream(entry).readAllBytes();
                    // 判断ZipEntry是否需要修改
                    if (
                        !entry.getName().startsWith("javassist") &&
                        !entry.getName().equals(".l1") &&
                        !entry.getName().equals(".l_ignore")
                    ) {
                        // 判断ZipEntry是否为class文件
                        if (entry.getName().endsWith(".class")) {
                            // 创建ClassReader对象，读取class文件
                            ClassReader reader = new ClassReader(bytes);
                            // 判断class文件的父类是否为JavaPlugin
                            if (reader.getSuperName().contains("JavaPlugin")) {
                                // 创建ClassNode对象，用于存储修改后的class文件
                                ClassNode cn = new ClassNode();
                                // 创建SimpleVisitor对象，用于修改class文件
                                SimpleVisitor v = new SimpleVisitor(cn, reader.getClassName() + "L10");
                                // 修改class文件
                                reader.accept(v, 0);
                                // 创建ClassWriter对象，用于将修改后的class文件写入字节数组
                                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                                cn.accept(writer);
                                // 设置修改后的类名
                                name.set(cn.name + "L10.class");
                                // 将修改后的class文件写入字节数组
                                bytes = writer.toByteArray();
                            }
                        }
                        // 将ZipEntry和对应的字节数组存入HashMap
                        map.put(e, bytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            // 遍历HashMap中的所有ZipEntry和字节数组
            map.forEach((entry, bytes) -> {
                try {
                    // 判断ZipEntry是否为修改后的类
                    if (!entry.getName().equals(name.get())) {
                        // 将ZipEntry和字节数组写入临时文件
                        zipOut.putNextEntry(entry);
                        zipOut.write(bytes);
                        zipOut.closeEntry();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            // 关闭ZipFile和ZipOutputStream
            jarFile.close();
            zipOut.close();
            // 将临时文件替换原始文件
            Files.copy(newFile.toPath(), originFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
