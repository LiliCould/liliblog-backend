package cn.lilicould.liliblog.common.util;

import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class Ip2RegionUtil {
    private static final Ip2Region ip2Region;
    private static final Path tempV4File;
    private static final Path tempV6File;

    static {
        try {
            // 1. 将 classpath 下的 xdb 文件复制到临时目录
            tempV4File = copyToTempFile("/ip2region/ip2region_v4.xdb");
            tempV6File = copyToTempFile("/ip2region/ip2region_v6.xdb");

            // 2. 使用 VIndexCache 配置，通过文件路径加载
            Config v4Config = Config.custom()
                    .setCachePolicy(Config.VIndexCache)   // 方案二
                    .setSearchers(15)
                    .setXdbPath(tempV4File.toString())    // 临时文件路径
                    .asV4();

            Config v6Config = Config.custom()
                    .setCachePolicy(Config.VIndexCache)
                    .setSearchers(15)
                    .setXdbPath(tempV6File.toString())
                    .asV6();

            ip2Region = Ip2Region.create(v4Config, v6Config);
        } catch (Exception e) {
            throw new RuntimeException("初始化 Ip2Region 失败", e);
        }
    }

    /**
     * 将 classpath 资源复制到系统临时目录，返回临时文件路径
     */
    private static Path copyToTempFile(String classpathResource) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathResource);
        if (!resource.exists()) {
            throw new RuntimeException("资源不存在: " + classpathResource);
        }
        // 获取原始文件名
        String fileName = resource.getFilename();
        // 创建临时文件
        Path tempFile = Files.createTempFile("ip2region_", "_" + fileName);
        // 复制内容
        try (InputStream is = resource.getInputStream()) {
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        // 标记 JVM 退出时删除临时文件
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }

    /**
     * 根据 IP 地址获取地理位置
     * @param ip IP 地址 (IPv4 或 IPv6)
     * @return 地理位置字符串
     */
    public static String getLocation(String ip) {
        try {
            // search 方法并发安全，支持 IPv4 和 IPv6
            return ip2Region.search(ip);
        } catch (Exception e) {
            return "未知";
        }
    }

    /**
     * 根据 IP 地址获取格式化的地理位置
     * @param ip IP 地址 (IPv4 或 IPv6)
     * @return 格式化的地理位置字符串，如：中国江西省上饶市移动
     */
    public static String getFormattedLocation(String ip) {
        String rawLocation = getLocation(ip);
        return formatLocation(rawLocation);
    }

    /**
     * 格式化地理位置字符串
     * @param rawLocation 原始地理位置字符串，格式：国家|省份|城市|运营商|国家代码
     * @return 格式化后的字符串
     */
    public static String formatLocation(String rawLocation) {
        if (rawLocation == null || "未知".equals(rawLocation) || rawLocation.trim().isEmpty()) {
            return "未知";
        }

        String[] parts = rawLocation.split("\\|");
        if (parts.length == 0) {
            return "未知";
        }

        StringBuilder result = new StringBuilder();

        // 添加国家（如果不是"0"或"内网IP"）
        if (!isIgnoredValue(parts[0])) {
            if (!"中国".equals(parts[0])) { // 忽略中国
                result.append(parts[0]);
            }

        }

        // 添加省份
        if (parts.length > 1 && !isIgnoredValue(parts[1])) {
            result.append(parts[1]);
        }

        // 添加城市（如果与省份不同且不是"0"）
        if (parts.length > 2 && !isIgnoredValue(parts[2]) && !parts[2].equals(parts[1])) {
            result.append(parts[2]);
        }

        return !result.isEmpty() ? result.toString() : "未知";
    }

    /**
     * 判断是否为需要忽略的值
     */
    private static boolean isIgnoredValue(String value) {
        return "0".equals(value) ||
                "null".equalsIgnoreCase(value) ||
                value.trim().isEmpty() ||
                "内网IP".equals(value) ||
                "局域网".equals(value);
    }
}
