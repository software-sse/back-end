package com.example.demo.controller;

import com.example.demo.config.ApiGroup;
import com.example.demo.model.History;
import com.example.demo.service.HistoryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins="*")
@RestController("history")
@RequestMapping("/history")
public class historyController {
    @Autowired
    HistoryService tmp;

    @GetMapping("/getHistory/{user_id}")
    @ApiGroup(group = {"history"})
    @ApiOperation(value="获取用户浏览历史",notes = "用户id")
    public List<History> getHistory(@PathVariable String user_id){
        return tmp.getHistory(user_id);
    }

    @PostMapping("/removeOneHistory")
    @ApiGroup(group = "history")
    @ApiOperation(value = "删除用户某条浏览记录",notes="用户id，商品id")
    public void removeOneHistory(@RequestParam("user_id") String user_id,
                                 @RequestParam("good_id") String good_id){
        tmp.removeOneHistory(user_id,good_id);
    }

    @PostMapping("/removeAllHistory")
    @ApiGroup(group = "history")
    @ApiOperation(value = "清空用户浏览记录",notes="用户id")
    public void removeAllHistory(@RequestParam("user_id") String user_id){
        tmp.removeAllHistory(user_id);
    }
}