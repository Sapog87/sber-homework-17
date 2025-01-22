package org.sber.services;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ThrottledInputStream;
import org.sber.properties.DownloadProperties;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class FilesDownloadServiceImpl implements FilesDownloadService {
    private final DownloadProperties downloadProperties;
    private final ExecutorService executorService;

    public FilesDownloadServiceImpl(DownloadProperties downloadProperties) {
        this.downloadProperties = downloadProperties;
        this.executorService = Executors.newFixedThreadPool(downloadProperties.getThreads());
    }

    /**
     * Скачивает файлы с указанных URL в заданную директорию
     *
     * @param dirPath путь к директории, в которую сохранять файлы
     * @param urls    список URL
     */
    @Override
    public void download(String dirPath, List<URL> urls) {
        File outputDir = getDir(dirPath);
        for (URL url : urls) {
            executorService.execute(() -> {
                String fileName = generateFileName(url);
                File outputFile = new File(outputDir, fileName);

                try (InputStream inputStream = getThrottledInputStream(url.openStream());
                     OutputStream outputStream = new FileOutputStream(outputFile)
                ) {
                    IOUtils.copy(inputStream, outputStream);
                    log.info("Файл сохранен: {} ({})", outputFile, url);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    /**
     * @param inputStream поток, скорость чтение из которого необходимо ограничить
     * @return поток, с ограниченной скоростью чтения в секунду
     * @throws IOException
     */
    private InputStream getThrottledInputStream(InputStream inputStream) throws IOException {
        ThrottledInputStream.Builder builder = ThrottledInputStream.builder();
        builder.setInputStream(inputStream);
        builder.setMaxBytesPerSecond(downloadProperties.getBytesPerSecond());
        return builder.get();
    }

    /**
     * Добавляет UUID суффикс к имени файла
     *
     * @param url
     * @return имя файла
     */
    private String generateFileName(URL url) {
        StringBuilder sb = new StringBuilder();
        sb.append(FilenameUtils.getBaseName(url.getPath()));
        sb.append("_");
        sb.append(UUID.randomUUID());
        String extension = FilenameUtils.getExtension(url.getPath());
        if (extension != null && !extension.isBlank()) {
            sb.append(".");
            sb.append(FilenameUtils.getExtension(url.getPath()));
        }
        return sb.toString();
    }

    /**
     * Возвращает директоию по заданному пути
     *
     * @param dirPath
     * @return директорию
     */
    private File getDir(String dirPath) {
        File outputDir = new File(dirPath);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IllegalArgumentException("Невозможно создать директорию " + dirPath);
            }
        } else if (!outputDir.isDirectory()) {
            throw new IllegalArgumentException("Путь не является директорией " + dirPath);
        }
        return outputDir;
    }

    @PreDestroy
    private void shutdown() {
        executorService.shutdown();
    }
}
