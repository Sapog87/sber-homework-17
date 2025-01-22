package org.sber.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "files.download")
public class DownloadProperties {
    private int threads;
    private int bytesPerSecond;
}
