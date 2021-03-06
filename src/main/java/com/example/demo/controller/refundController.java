package com.example.demo.controller;

import com.example.demo.config.ApiGroup;
import com.example.demo.model.Refund;
import com.example.demo.model.TradeOrder;
import com.example.demo.result.Result;
import com.example.demo.service.RefundService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins="*")
@RestController("refund")
@RequestMapping("/refund")
public class refundController {
    @Autowired
    RefundService tmp;

    @Transactional
    @GetMapping("/getAllArbitritions")
    @ApiGroup(group = {"refund","admin"})
    @ApiOperation(value = "管理员获取所有待仲裁的退款，按照退款发起时间升序列出")
    public Result getAllArbitration() {
        return tmp.getAllArbitration();
    }

    @Transactional
    @GetMapping("/getAllByBuyer/{user_id}")
    @ApiGroup(group = {"refund","user"})
    @ApiOperation(value = "获取该用户提交的所有退款申请",notes = "用户id")
    public Result getAllByBuyer(@PathVariable String user_id) {
        return tmp.getAllByBuyer(user_id);
    }

    @Transactional
    @GetMapping("/getAllBySeller/{user_id}")
    @ApiGroup(group = {"refund","user"})
    @ApiOperation(value = "获取该用户收到的所有退款申请",notes = "用户id")
    public Result getAllBySeller(@PathVariable String user_id) {
        return tmp.getAllBySeller(user_id);
    }

    @Transactional
    @PostMapping("/submitRefund")
    @ApiGroup(group = {"refund"})
    @ApiOperation(value = "申请退款",notes = "订单id，退款原因描述")
    public Result refund(@RequestParam("order_id") String order_id, @RequestParam("text") String text){
        return tmp.submitRefund(order_id,text);
    }

    @Transactional
    @PostMapping("/permitRefund")
    @ApiGroup(group = {"refund"})
    @ApiOperation(value = "卖家批准退款",notes = "订单id")
    public Result permitRefund(@RequestParam("order_id") String order_id){
        return tmp.permitRefund(order_id);
    }

    @Transactional
    @PostMapping("/refuseRefund")
    @ApiGroup(group = {"refund"})
    @ApiOperation(value = "卖家拒绝退款",notes = "订单id")
    public Result refuseRefund(@RequestParam("order_id") String order_id){
        return tmp.refuseRefund(order_id);
    }

    @Transactional
    @PostMapping("/cancelRefund")
    @ApiGroup(group = {"refund"})
    @ApiOperation(value = "买家撤回退款",notes = "订单id")
    public Result cancelRefund(@RequestParam("order_id") String order_id){
        return tmp.cancelRefund(order_id);
    }

    @Transactional
    @PostMapping("/refuseArbitition")
    @ApiGroup(group = {"refund","admin"})
    @ApiOperation(value = "仲裁驳回",notes = "订单id")
    public Result refuseArbitration(@RequestParam("order_id") String order_id){
        return tmp.refuseArbitration(order_id);
    }

    @Transactional
    @PostMapping("/permitArbitition")
    @ApiGroup(group = {"refund","admin"})
    @ApiOperation(value = "仲裁批准",notes = "订单id")
    public Result permitArbitration(@RequestParam("order_id") String order_id){
        return tmp.permitArbitration(order_id);
    }

    @Transactional
    @PostMapping(value = "/submitArbitition")
    //@RequestMapping(value = "/submitArbitation", method = RequestMethod.POST,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiGroup(group = {"refund"})
    @ApiOperation(value = "提交仲裁申请",notes = "订单id，仲裁理由，图片")
    public Result submitArbitration(@RequestParam("order_id") String order_id,
                                    @RequestParam("text") String text,
                                    @RequestParam("file") MultipartFile file){
        System.out.println("尝试提交仲裁");
        return tmp.submitArbitration(order_id,text,file);
    }



    @GetMapping("/getRefundInfo/{order_id}")
    @ApiGroup(group = {"refund"})
    @ApiOperation(value = "查看某笔订单退款情况",notes = "订单id")
    public Result getRefundInfo(@PathVariable("order_id") String order_id){
        return tmp.getRefundInfo(order_id);
    }
}
