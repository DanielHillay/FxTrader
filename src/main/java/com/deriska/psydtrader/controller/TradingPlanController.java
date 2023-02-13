package com.deriska.psydtrader.controller;

import com.deriska.psydtrader.entity.StandardResponse;
import com.deriska.psydtrader.entity.TradingPlan;
import com.deriska.psydtrader.service.TradingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tradingjournal")
public class TradingPlanController {
    @Autowired
    private TradingPlanService planService;

    @PostMapping("/createtradingplan")
    public ResponseEntity<StandardResponse> createPlan(@RequestBody TradingPlan tradingPlan){
        return planService.createTradingPlan(tradingPlan);
    }

}
