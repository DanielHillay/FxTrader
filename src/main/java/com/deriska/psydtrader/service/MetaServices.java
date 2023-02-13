package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.Pojo.RunningTradeRequest;
import org.springframework.stereotype.Service;

@Service
public class MetaServices {

    private Double quote;

    public RunningTradeRequest getRunningTrade(){
         RunningTradeRequest request = new RunningTradeRequest();
         return request;
    }

    public void checkAccountStatus(Long accountId){

    }


}
