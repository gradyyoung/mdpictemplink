package top.ygang.mdpictemplink_server.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @描述
 * @创建人 yhgh
 * @创建时间 2023/6/1 17:01
 */
@Component
public class ClearTask {

    @Value("${spring.resources.static-locations}")
    private String tempDirStr;

    @Scheduled(cron = "${clearCron}")
    public void clear(){
        System.out.println(1);
        String tempDirPath = tempDirStr.replace("file:", "");
        File file = new File(tempDirPath);
        if (file.exists()){
            File[] files = file.listFiles();
            for (int i = 0;i < files.length;i ++){
                files[i].delete();
            }
        }
    }
}
