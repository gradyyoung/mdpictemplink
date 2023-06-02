package top.ygang.mdpictemplink_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @描述
 * @创建人 yhgh
 * @创建时间 2023/6/1 14:22
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${spring.resources.static-locations}")
    private String tempDirStr;
    @Value("${picLink.host}")
    private String host;

    @PostMapping("/tempFile")
    public Map<String,Object> tempFile(@RequestParam("name") String name, @RequestParam("file")MultipartFile file){
        String subfix = name.substring(name.lastIndexOf("."));
        String tempDirPath = tempDirStr.replace("file:", "");
        Map<String,Object> result = new HashMap<>();

        File tempDir = new File(tempDirPath);
        if (!tempDir.exists()){
            tempDir.mkdirs();
        }

        long l = System.currentTimeMillis();
        String s = UUID.randomUUID().toString();
        String fileName = l + "-" + s + subfix;
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(tempDirPath + File.separator + fileName);
            outputStream.write(file.getBytes());
            result.put("code",200);
            result.put("link",host + fileName);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.put("code",500);
            return result;
        }finally {
            if (outputStream != null){
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
