package ru.job4j.site;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
@Slf4j
public class SiteSrv {

    @Value("${server.site.url}")
    private static String site;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SiteSrv.class);
        application.addListeners(new ApplicationPidFileWriter("./site.pid"));
        application.run();
        log.info("Go to -> :{}", site);
    }
}
