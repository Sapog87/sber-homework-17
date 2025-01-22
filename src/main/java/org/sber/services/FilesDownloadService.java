package org.sber.services;

import java.net.URL;
import java.util.List;

public interface FilesDownloadService {
    /**
     * Скачивает файлы с указанных URL в заданную директорию
     *
     * @param dirPath путь к директории, в которую сохранять файлы
     * @param urls    список URL
     */
    void download(String dirPath, List<URL> urls);
}
