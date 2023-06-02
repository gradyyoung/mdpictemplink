package top.ygang.mdpictemplink_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @描述
 * @创建人 yhgh
 * @创建时间 2023/6/1 14:15
 */
@SpringBootApplication
@EnableScheduling
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class,args);
    }
}
