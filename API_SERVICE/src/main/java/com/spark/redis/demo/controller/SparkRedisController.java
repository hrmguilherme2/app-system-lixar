package com.spark.redis.demo.controller;


import com.spark.redis.demo.model.Transaction;
import com.spark.redis.demo.service.GroupRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

@RequestMapping("api")
@RestController
public class SparkRedisController {

    @Autowired
    private GroupRedisService transactionServiceRedis;

    @RequestMapping(method = RequestMethod.POST, path = "/process")
    public DeferredResult<ResponseEntity<?>> save(@RequestBody List<Transaction> transactions) throws InterruptedException {
        System.out.println("Received async-deferredresult request");
        DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();
        System.out.println("Received: " + transactions.size() + " transactions");
        transactionServiceRedis.processTransctions(transactions);
        // Return the result to the client
        output.setResult(ResponseEntity.ok().body("Request received"));
        return output;
    }


    @RequestMapping("getInformation")
    public ResponseEntity<Object> getInformation(@RequestParam(required = true) List<Object> filter) {

        return new ResponseEntity<Object>(transactionServiceRedis.readGroup( filter), HttpStatus.OK);
    }




}
