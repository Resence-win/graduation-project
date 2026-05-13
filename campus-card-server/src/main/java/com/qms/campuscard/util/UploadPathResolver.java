package com.qms.campuscard.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadPathResolver {

    @Value("${file.upload.path:upload}")
    private String uploadPath;

    public Path resolveRootPath() {
        Path configuredPath = Paths.get(uploadPath);
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize();
        }

        Path userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path projectRoot = resolveProjectRoot(userDir);
        if (projectRoot != null && isDefaultUploadPath(configuredPath)) {
            return projectRoot.resolve("upload").normalize();
        }

        Path directPath = userDir.resolve(configuredPath).normalize();
        if (Files.exists(directPath)) {
            return directPath;
        }

        if (projectRoot != null) {
            return projectRoot.resolve(configuredPath).normalize();
        }

        return directPath;
    }

    public Path resolvePath(String relativeDir) {
        return resolveRootPath().resolve(relativeDir).normalize();
    }

    private Path resolveProjectRoot(Path startPath) {
        Path current = startPath;
        for (int i = 0; i < 6 && current != null; i++) {
            if (Files.isDirectory(current.resolve("campus-card-server"))) {
                return current;
            }
            if (current.getFileName() != null
                    && "campus-card-server".equals(current.getFileName().toString())
                    && current.getParent() != null) {
                return current.getParent();
            }
            current = current.getParent();
        }
        return null;
    }

    private boolean isDefaultUploadPath(Path configuredPath) {
        String normalizedPath = configuredPath.normalize().toString().replace('\\', '/');
        return "upload".equals(normalizedPath)
                || "./upload".equals(normalizedPath)
                || "../upload".equals(normalizedPath);
    }
}
