package top.ygang.mdpictemplink_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @描述
 * @创建人 yhgh
 * @创建时间 2023/6/1 18:00
 */
@RestController
public class CheckController {

    @GetMapping("/check")
    public String check(){
        return "Success!!!";
    }
}
