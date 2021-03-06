package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.goodRepository;
import com.example.demo.repository.recplanRepository;
import com.example.demo.repository.recrecordRepository;
import com.example.demo.repository.userRepository;
import com.example.demo.result.Result;
import com.example.demo.result.ResultFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RecommendService implements IDGenenrator{
    @Autowired
    recrecordRepository recrecordRepo;
    @Autowired
    recplanRepository recplanRepo;
    @Autowired
    goodRepository goodRepo;
    @Autowired
    userRepository userRepo;

    public Result getAllRecPlans(){
        return ResultFactory.buildSuccessResult(recplanRepo.findAll());
    }

    public Result getMyCurrentRecs(String user_id){
        List<Good> goods=goodRepo.getMyRecGoods(user_id);
        List<Map<String,Object>> result=new ArrayList<>();
        for(Good good:goods){
            Recrecord record = recrecordRepo.getCurrentByGoodId(good.getId());
            if(record==null){continue;}
            Map<String,Object> map=new HashMap<>();
            map.put("good_id",good.getId());
            map.put("good_name",good.getName());
            map.put("url",good.getUrl());
            map.put("record_id",record.getId());
            map.put("plan_id",record.getRecId());
            map.put("rec_state",record.getState());
            map.put("start_time",record.getStartTime());
            Recplan plan=recplanRepo.findById(record.getRecId()).get();
            map.put("plan_name",plan.getName());
            map.put("plan_cost",plan.getCost());
            map.put("duration",plan.getDuration());
            result.add(map);
        }
        return ResultFactory.buildSuccessResult(result);
    }

    public Result submitRecommend(String good_id,String plan_id){
        if(!goodRepo.existsById(good_id)||!recplanRepo.existsById(plan_id)){
            return ResultFactory.buildFailResult("??????????????????");
        }
        if(recrecordRepo.getUnpaidRec(good_id)!=null){
            return ResultFactory.buildFailResult("????????????????????????");
        }
        Good good= goodRepo.findById(good_id).get();
        if(recrecordRepo.getCurrentByGoodId(good_id)!=null){
            Recrecord current=recrecordRepo.getCurrentByGoodId(good_id);
            Recplan recplan=recplanRepo.findById(current.getRecId()).get();
            if(current.getStartTime().plusMillis(TimeUnit.DAYS.toMillis(recplan.getDuration())).isAfter(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)))) {
                return ResultFactory.buildFailResult("?????????????????????");
            }else{
                current.setState("?????????");
                good.setIsRec("0");
                goodRepo.save(good);
                recrecordRepo.save(current);
            }
        }
        if(!good.getGoodState().equals("?????????")){
            return ResultFactory.buildFailResult("???????????????");
        }
        Recrecord record=new Recrecord();
        record.setId(generateID(24));
        record.setState("?????????");
        record.setRecId(plan_id);
        record.setGoodId(good_id);
        recrecordRepo.save(record);
        return ResultFactory.buildResult(200,"?????????????????????",record);
    }

    public Result payRecommend(String record_id){
        if(!recrecordRepo.existsById(record_id)){
            return ResultFactory.buildFailResult("??????????????????");
        }
        Recrecord record=recrecordRepo.findById(record_id).get();
        Good good=goodRepo.findById(record.getGoodId()).get();
        if(!good.getGoodState().equals("?????????")){
            return ResultFactory.buildFailResult("???????????????");
        }
        if(!record.getState().equals("?????????")){
            return ResultFactory.buildFailResult("???????????????");
        }
        User user=userRepo.findById(good.getSellerId()).get();
        if(user.getBalance()>=recplanRepo.findById(record.getRecId()).get().getCost()){
            user.setBalance(user.getBalance()-recplanRepo.findById(record.getRecId()).get().getCost());
            record.setStartTime(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)));
            record.setState("?????????");
            good.setIsRec("1");
            goodRepo.save(good);
            return ResultFactory.buildResult(200,"????????????",null);
        }else{
            return ResultFactory.buildFailResult("????????????");
        }
    }

    public Result getHistoryRecsByGoodID(String good_id){
        return ResultFactory.buildSuccessResult(recrecordRepo.getAllRecordsByGoodID(good_id));
    }

    @Override
    public StringBuilder tryGetID(int length) {
        StringBuilder id=new StringBuilder();
        Random rd = new SecureRandom();
        for(int i=0;i<length;i++){
            int bit = rd.nextInt(10);
            id.append(bit);
        }
        return id;
    }


    @Override
    public String generateID(int length) {
        while(true)
        {
            StringBuilder id=tryGetID(length);
            if(!recrecordRepo.existsById(id.toString())) return id.toString();
        }
    }
}
