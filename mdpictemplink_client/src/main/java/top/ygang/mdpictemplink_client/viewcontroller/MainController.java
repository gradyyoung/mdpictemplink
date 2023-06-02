package top.ygang.mdpictemplink_client.viewcontroller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import de.felixroske.jfxsupport.AbstractFxmlView;
import de.felixroske.jfxsupport.FXMLView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @描述
 * @创建人 yhgh
 * @创建时间 2023/6/1 14:46
 */
@FXMLView("/fxml/main.fxml")
public class MainController  extends AbstractFxmlView implements Initializable {

    @FXML
    private Button chooseFileBtn;

    @FXML
    private TextArea textArea;

    @FXML
    private Label pathText;

    @FXML
    private TextField host;

    @FXML
    private Label msg;

    private File cacheFile;

    private Properties loadCache(){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(cacheFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private void setCache(String key,String value){
        Properties properties = loadCache();
        properties.setProperty(key,value);
        try {
            properties.store(new FileOutputStream(cacheFile),"MdPicTempLink Cache");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cacheFile = new File(System.getProperty("user.dir") + File.separator + "cache.properties");
        if (!cacheFile.exists()){
            try {
                cacheFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Properties properties = loadCache();
            String cacheHost = properties.getProperty("host");
            if (StringUtils.hasText(cacheHost)){
                host.setText(cacheHost);
            }
        }
    }

    @FXML
    public void chooseFile(){
        if (!StringUtils.hasText(host.getText())){
            msg.setText("未正确配置服务地址！！！");
            return;
        }
        setCache("host",host.getText());
        FileChooser chooser = new FileChooser();
        Properties properties = loadCache();
        String lastChoosePath = properties.getProperty("lastChoosePath");
        if (StringUtils.hasText(lastChoosePath)){
            File base = new File(lastChoosePath);
            if (base.exists()){
                chooser.setInitialDirectory(base);
            }
        }
        File file = chooser.showOpenDialog(new Stage());
        try {
            pathText.setText(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String parent = file.getParent();

        setCache("lastChoosePath",parent);

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String text;
                    while ((text = bufferedReader.readLine()) != null){
                        stringBuilder.append(text);
                        stringBuilder.append("\n");
                    }
                    bufferedReader.close();

                    String s = stringBuilder.toString();
                    Pattern pattern = Pattern.compile("!\\[.*\\]\\((.*)\\)");
                    Matcher matcher = pattern.matcher(s);
                    int total = 0;
                    int success = 0;
                    int fail = 0;
                    while (matcher.find()){
                        String imgTag = matcher.group();
                        String imagePath = matcher.group(1);
                        updateMessage("正在上传：" + imagePath);
                        total++;
                        String link = uploadFile(parent + File.separator + imagePath);
                        if (link == null){
                            fail++;
                            continue;
                        }
                        success++;
                        String replace = imgTag.replace(imagePath, link);
                        s = s.replace(imgTag,replace);
                    }
                    updateMessage("解析完成，图片共：" + total + "个，成功：" + success + "个，失败：" + fail + "个");
                    textArea.setText(s);
                    return "success";
                }catch (Exception e){
                    e.printStackTrace();
                    return "failed";
                }finally {
                    if (fileReader != null){
                        try {
                            fileReader.close();
                        }catch (Exception e){}
                    }
                }
            }
        };

        task.messageProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                msg.setText(newValue);
            }
        });

        new Thread(task).start();
    }

    private String uploadFile(String path){
        File file = new File(path);
        if (!file.exists()){
            return null;
        }
        CloseableHttpClient client = HttpClients.createDefault();
        String text = host.getText();
        if (!text.endsWith("/")){
            text += "/";
        }
        String fileName = file.getName();
        String url = text + "upload/tempFile";
        HttpPost httpPost = new HttpPost(url);
        FileInputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(file);
            outputStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) > 0){
                outputStream.write(bytes,0,len);
            }
            byte[] reBytes = outputStream.toByteArray();
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addBinaryBody(
                    "file",
                    reBytes,
                    ContentType.APPLICATION_OCTET_STREAM,
                    fileName);
            multipartEntityBuilder.addTextBody("name",fileName,ContentType.TEXT_PLAIN);
            HttpEntity build = multipartEntityBuilder.build();
            httpPost.setEntity(build);
            CloseableHttpResponse execute = client.execute(httpPost);
            String s = EntityUtils.toString(execute.getEntity());
            JSONObject jsonObject = JSON.parseObject(s);
            int code = (int) jsonObject.get("code");
            if (code == 200){
                String link = jsonObject.get("link").toString();
                return link;
            }else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                }catch (Exception e){
                }
            }
            if (outputStream != null){
                try {
                    outputStream.flush();
                    outputStream.close();
                }catch (Exception e){}
            }
        }
    }
}
