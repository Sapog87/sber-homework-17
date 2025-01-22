package org.sber;

import lombok.RequiredArgsConstructor;
import org.sber.services.FilesDownloadService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

@RequiredArgsConstructor
@Component
public class FilesDownloadRunner implements ApplicationRunner {
    private final FilesDownloadService filesDownloadService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            File fileWithUrls = ResourceUtils.getFile("classpath:urls.txt");
            List<URL> urls = Files.readAllLines(fileWithUrls.toPath())
                    .stream().map(url -> {
                        try {
                            return new URL(url);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();

            filesDownloadService.download("./files", urls);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}