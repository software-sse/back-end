package com.example.demo.service;

import com.example.demo.model.Good;
import com.example.demo.model.Refund;
import com.example.demo.model.TradeOrder;
import com.example.demo.model.User;
import com.example.demo.repository.goodRepository;
import com.example.demo.repository.orderRepository;
import com.example.demo.repository.refundRepository;
import com.example.demo.repository.userRepository;
import com.example.demo.result.Result;
import com.example.demo.result.ResultFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService implements IDGenenrator{

    @Autowired
    orderRepository orderRepo;
    @Autowired
    goodRepository goodRepo;
    @Autowired
    userRepository userRepo;
    @Autowired
    refundRepository refundRepo;

    public Result getOrderInfo(String order_id){
        if(orderRepo.existsById(order_id)){
            TradeOrder order = orderRepo.findById(order_id).get();
            Good good =goodRepo.findById(order.getGoodId()).get();
            Map<String,Object> map =new HashMap<>();
            map.put("order_id",order.getId());
            map.put("seller_id",good.getSellerId());
            map.put("seller_name",userRepo.findById(good.getSellerId()).get().getName());
            map.put("good_id",good.getId());
            map.put("good_name",good.getName());
            map.put("good_url",good.getUrl());
            map.put("order_state",order.getOrderState());
            map.put("isRefunding",order.getIsRefunding());
            map.put("price",order.getPrice());
            map.put("num",order.getNum());
            map.put("start_date",order.getStartDate());
            map.put("buyer_address",order.getBuyerAddress());
            map.put("seller_address",order.getSellerAddress());
            map.put("buyer_id",order.getBuyerId());
            map.put("buyer_name",userRepo.findById(order.getBuyerId()).get().getName());

            return ResultFactory.buildSuccessResult(map);
        }
        return ResultFactory.buildFailResult("??????????????????");
    }

    public Result getRefundState(String order_id){
        if(!orderRepo.existsById(order_id)){
            return ResultFactory.buildFailResult("????????????");
        }
        TradeOrder order=orderRepo.findById(order_id).get();
        if(!refundRepo.existsById(order_id)){
            return ResultFactory.buildResult(200,"????????????",null);
        }
        else if(refundRepo.findById(order.getId()).get().getRefundState().equals("?????????")||
                refundRepo.findById(order.getId()).get().getRefundState().equals("????????????")||
                refundRepo.findById(order.getId()).get().getRefundState().equals("????????????")){
            return ResultFactory.buildResult(201,"???????????????",null);
        }
        else if(refundRepo.findById(order.getId()).get().getRefundState().equals("????????????")||
                refundRepo.findById(order.getId()).get().getRefundState().equals("????????????")){
            return ResultFactory.buildResult(202,"????????????",null);
        }
        return ResultFactory.buildFailResult("????????????");

    }

    public Result generateOrder(String u_id, String g_id, String buy_address, Integer num) {
        if(userRepo.existsById(u_id)&&goodRepo.existsById(g_id)) {
            //???????????????????????????????????????
            if(goodRepo.findById(g_id).get().getSellerId().equals(u_id)){
                return ResultFactory.buildFailResult("???????????????????????????????????????");
            }
            if(!goodRepo.findById(g_id).get().getGoodState().equals("?????????")){
                return ResultFactory.buildFailResult("?????????????????????");
            }
            TradeOrder order = new TradeOrder();
            String id = generateID(16);
            order.setId(id);
            order.setBuyerId(u_id);
            order.setGoodId(g_id);
            //????????????
            if(goodRepo.isEnough(g_id,num)>0){
                order.setNum(num);
            }else{
                return ResultFactory.buildFailResult("?????????????????????????????????");
            }
            order.setPrice(goodRepo.calculateSum(g_id,num));
            order.setStartDate(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)));
            order.setBuyerAddress(buy_address);
            order.setSellerAddress(goodRepo.findById(g_id).get().getShip_address());
            order.setOrderState("?????????");
            order.setIsRefunding("n");
            orderRepo.save(order);
            return ResultFactory.buildResult(200,"????????????",order.getId());
        }
        return ResultFactory.buildFailResult("????????????");
    }

    public Result payOrder(String o_id){
        if(!orderRepo.existsById(o_id)){return ResultFactory.buildFailResult("???????????????");}
        TradeOrder order= orderRepo.getOne(o_id);
        if(!order.getOrderState().equals("?????????")){return ResultFactory.buildFailResult("?????????????????????");}
        if(!goodRepo.getGoodState(order.getGoodId()).equals("?????????")){return ResultFactory.buildFailResult("??????????????????");}
        if(goodRepo.isEnough(order.getGoodId(),order.getNum())>0){
            if(userRepo.getBalance(order.getBuyerId())>=order.getPrice()){
                order.setOrderState("?????????");
                User user = userRepo.getOne(order.getBuyerId());
                user.setBalance(user.getBalance()-order.getPrice());
                userRepo.save(user);
                Good good = goodRepo.getOne(order.getGoodId());
                good.setInventory(good.getInventory()-order.getNum());
                goodRepo.save(good);
                orderRepo.save(order);
                return ResultFactory.buildResult(200,"????????????",null);
            }else{
                return ResultFactory.buildFailResult("????????????");
            }
        }
        else{
            return ResultFactory.buildFailResult("????????????");
        }
    }

    public Result ackOrder(String order_id){
        if(orderRepo.existsById(order_id)){
            if(orderRepo.findById(order_id).get().getOrderState().equals("?????????")){
                TradeOrder order = orderRepo.findById(order_id).get();
                order.setOrderState("?????????");
                orderRepo.save(order);
                return ResultFactory.buildResult(200,"??????????????????",null);
            }
        }
        return ResultFactory.buildFailResult("????????????");
    }

    public Result sendPackage(String order_id){
        if(orderRepo.existsById(order_id)){
            if(orderRepo.findById(order_id).get().getOrderState().equals("?????????")){
                TradeOrder order = orderRepo.findById(order_id).get();
                order.setOrderState("?????????");
                orderRepo.save(order);
                return ResultFactory.buildResult(200,"????????????",null);
            }
        }
        return ResultFactory.buildFailResult("????????????");
    }

    public Result getRefundingOrder(String user_id){
        TradeOrder order = new TradeOrder();
        order.setIsRefunding("y");
        order.setBuyerId(user_id);
        Example<TradeOrder> orderExample=Example.of(order);
        Sort sort = Sort.sort(TradeOrder.class).descending();
        sort.getOrderFor("startDate");
        List<TradeOrder> orderList = orderRepo.findAll(orderExample,sort);
        List<Map<String,Object>> result= new ArrayList<>();
        for(TradeOrder order1:orderList){
            Map<String,Object> map=new HashMap<>();
            map.put("order_id",order1.getId());
            map.put("name",goodRepo.findById(order1.getGoodId()).get().getName());
            map.put("start_date",order1.getStartDate());
            map.put("order_state",order1.getOrderState());
            map.put("price",order1.getPrice());
            result.add(map);
        }
        return ResultFactory.buildSuccessResult(result);
    }

    public void setOrderState(String order_id,String newstate){
        orderRepo.setOrderState(order_id,newstate);
    }

    public Result getAllByBuyer(String buyerId){
        if(userRepo.existsById(buyerId)){
            List<TradeOrder> orderList = orderRepo.getAllByBuyer(buyerId);
            List<Map<String,Object>> result= new ArrayList<>();
            for(TradeOrder order1:orderList){
                Map<String,Object> map=new HashMap<>();
                map.put("order_id",order1.getId());
                map.put("name",goodRepo.findById(order1.getGoodId()).get().getName());
                map.put("start_date",order1.getStartDate());
                map.put("order_state",order1.getOrderState());
                map.put("good_url",goodRepo.findById(order1.getGoodId()).get().getUrl());
                map.put("isRefunding",order1.getIsRefunding());
                map.put("price",order1.getPrice());
                result.add(map);
            }
            return ResultFactory.buildSuccessResult(result);
        }else{
            return ResultFactory.buildFailResult("??????????????????");
        }
    }

    public Result getAllBySeller(String sellerId){
        if(userRepo.existsById(sellerId)){
            List<TradeOrder> orderList = orderRepo.getAllBySeller(sellerId);
            List<Map<String,Object>> result= new ArrayList<>();
            for(TradeOrder order1:orderList){
                Map<String,Object> map=new HashMap<>();
                map.put("order_id",order1.getId());
                map.put("name",goodRepo.findById(order1.getGoodId()).get().getName());
                map.put("start_date",order1.getStartDate());
                map.put("order_state",order1.getOrderState());
                map.put("good_url",goodRepo.findById(order1.getGoodId()).get().getUrl());
                map.put("isRefunding",order1.getIsRefunding());
                map.put("price",order1.getPrice());
                result.add(map);
            }
            return ResultFactory.buildSuccessResult(result);
        }else{
            return ResultFactory.buildFailResult("??????????????????");
        }
    }

    @Override
    public StringBuilder tryGetID(int length) {
        StringBuilder id=new StringBuilder();
        Random rd = new SecureRandom();
        for(int i=0;i<length;i++){
            int bit = rd.nextInt(10);
            id.append(String.valueOf(bit));
        }
        return id;
    }

    @Override
    public String generateID(int length) {
        while(true)
        {
            StringBuilder id=tryGetID(length);
            if(!orderRepo.existsById(id.toString())) return id.toString();
        }
    }
}
