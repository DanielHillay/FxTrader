package com.deriska.psydtrader.controller;

import com.deriska.psydtrader.entity.StandardResponse;
import com.deriska.psydtrader.entity.TradeRequest;
import com.deriska.psydtrader.service.AutoTrader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trade")
public class AutoTraderController {
    @Autowired
    private AutoTrader autoService;

    @PostMapping("/executestricttrade")
    public ResponseEntity<StandardResponse> placeStrictTrade(@RequestBody TradeRequest request){
        return autoService.executeStrictTrade(request);
    }

    @PostMapping("/executenonstricttrade")
    public ResponseEntity<StandardResponse> placeNonStrictTrade(@RequestBody TradeRequest request){
        return autoService.executeNonStrictTrade(request);
    }
}
