package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.StandardResponse;
import com.deriska.psydtrader.entity.TradingPlan;
import com.deriska.psydtrader.repository.TradingPlanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TradingPlanService {
    @Autowired
    private TradingPlanRepo tradingPlanRepo;

    public ResponseEntity<StandardResponse> createTradingPlan(TradingPlan tradingPlan){
        try {
            return StandardResponse.sendHttpResponse(true, "Operation Successful", tradingPlanRepo.save(tradingPlan));
        } catch (Exception e) {
            return StandardResponse.sendHttpResponse(false, "Could not create trading plan");
        }
    }
}
