package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.*;
import com.deriska.psydtrader.repository.TradeJournalRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CalculationService {

    @Autowired
    private SyntheticCalculatorService syntheticCalculatorService;
    @Autowired
    private CurrencyCalculatorService currencyCalculatorService;
    @Autowired
    private TradeJournalRepo journalRepo;

    public ResponseEntity<StandardResponse> calculateTradeRisk(TradeRequest trade) {
        try {
            String category = trade.getAssetCategory();
            new RiskAnalysisResponse();
            RiskAnalysisResponse response = switch (category) {
                case "Synthetics" -> syntheticCalculatorService.calculateAll(trade);
                case "Currencies" -> currencyCalculatorService.calculateAll(trade);
                default -> new RiskAnalysisResponse();
            };
            return StandardResponse.sendHttpResponse(true, "Successful", response, "200");
        } catch (Exception e) {
            return StandardResponse.sendHttpResponse(false, "Operation failed");
        }
    }

    public RiskAnalysisResponse calculateRiskAnalysis(TradeRequest trade){

        RiskAnalysisResponse response = new RiskAnalysisResponse();
        try {
            String category = trade.getAssetCategory();
            switch (category) {
                case "Synthetics" -> response = syntheticCalculatorService.calculateAll(trade);
                case "Currencies" -> response = currencyCalculatorService.calculateAll(trade);
            }
            return response;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return response;
        }
    }
}
