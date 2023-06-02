package top.ygang.mdpictemplink_client;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import top.ygang.mdpictemplink_client.viewcontroller.MainController;

/**
 * @描述
 * @创建人 yhgh
 * @创建时间 2023/6/1 14:45
 */
@SpringBootApplication
public class Start extends AbstractJavaFxApplicationSupport {

    // 这个方法是项目启动后的钩子函数，可以在里面写逻辑，例如系统托盘
    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    public static void main(String[] args) {
        // 参数分别是Application的主类，主界面的UI类，闪屏对象，args
        // 不想要自定义闪屏的可以调用另一个不带闪屏对象的launch方法
        launch(Start.class,MainController.class, args);
    }
}
