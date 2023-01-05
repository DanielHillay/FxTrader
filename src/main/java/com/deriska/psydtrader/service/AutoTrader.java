package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.*;
import com.deriska.psydtrader.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class AutoTrader {

    @Autowired
    private RiskManagementRepo riskManagementRepo;
    @Autowired
    private TradeJournalRepo journalRepo;
    @Autowired
    private TradeHolderRepo holderRepo;
    @Autowired
    private CalculationService calculationService;
    @Autowired
    private WatchListService watchListService;
    @Autowired
    private ExitStrategyRepo exitStrategyRepo;
    @Autowired
    private TradingPlanRepo tradingPlanRepo;
    @Autowired
    private TradeRequestRepo requestRepo;


    public ResponseEntity<StandardResponse> executeStrictTrade(TradeRequest trade){

        Trades tradeJournal = new Trades(trade);

        TradeHolder tradeHolder = new TradeHolder();
        TradeChanges tradeChanges = new TradeChanges();
        tradeChanges.setTradeRequestId(requestRepo.save(trade).getRequestId());
        tradeHolder.setTradeId(journalRepo.save(tradeJournal).getId());
        try {
            RiskAnalysisResponse response = calculationService.calculateRiskAnalysis(trade);
            if (response.getPsychEvalScore() < 50) {
                // Tell the user that we can't open the trade until he meets all his set criteria
                return StandardResponse.sendHttpResponse(true, "Could not open trade because You have betrayed your trading plan");
            } else {
                // call the metatrader API to open the trade
                journalRepo.save(tradeJournal);
                holderRepo.save(tradeHolder);
                return StandardResponse.sendHttpResponse(true, "Successfully entered trade");
            }
        } catch (Exception e) {
            return StandardResponse.sendHttpResponse(false, "Could not place trade");
        }
    }

    public ResponseEntity<StandardResponse> executeNonStrictTrade(TradeRequest tradeRequest){
        Trades tradeJournal = new Trades(tradeRequest);

        TradeHolder tradeHolder = new TradeHolder();
        tradeHolder.setTradeId(journalRepo.save(tradeJournal).getId());

        try {
            // Call a backgroundRunner here.
            RiskAnalysisResponse response = calculationService.calculateRiskAnalysis(tradeRequest);
            if (response.getPsychEvalScore() < 50) {
                // Warn the trader of his error and suggest actions to take.
                return StandardResponse.sendHttpResponse(true, "Operation successful", response);
            }

            return StandardResponse.sendHttpResponse(true, "Operation successful", response);
        } catch (Exception e) {
            return StandardResponse.sendHttpResponse(false, "Could not execute trade");
        }
    }

    public void setPriceAlertBasedOnTradeRequest(TradeRequest request){
        TradingPlan tradingPlan = tradingPlanRepo.findByPlanName(request.getTradingPlanName()).get();
        List<ExitStrategy> exitStrategies = exitStrategyRepo.findByTradingPlanId(tradingPlan.getPlanId());
        for(ExitStrategy ex : exitStrategies){
            if(!ObjectUtils.isEmpty(ex.getInTradeProfitLevel())){
                double price = calculatePriceToSet(request, ex.getInTradeProfitLevel());
                double sLPrice = 0;
                if(!ObjectUtils.isEmpty(ex.getSLPlacementPercentAfterProfit()) && ex.getSLPlacementPercentAfterProfit() == 0) {
                    sLPrice = calculatePriceToSet(request, ex.getSLPlacementPercentAfterProfit());
                }
                SyntheticsAsset asset = new SyntheticsAsset();
                asset.setWatchPrice(price);
                asset.setActive(true);
                asset.setAlertMedium("Default Medium");
                asset.setRemark("You ought to set your SL at "+ sLPrice +". ");
            }
            if(!ObjectUtils.isEmpty(ex.getAllowedLossLevelPercentage())){
                double price = calculatePriceToSet(request, ex.getAllowedLossLevelPercentage());
                SyntheticsAsset asset = new SyntheticsAsset();
                asset.setWatchPrice(price);
                asset.setActive(true);
                asset.setAlertMedium("Default Medium");
            }
            if(!ObjectUtils.isEmpty(ex.getStopLossPercentAfterTradeInProfit())){
                double price = calculatePriceToSet(request, ex.getStopLossPercentAfterTradeInProfit());
                SyntheticsAsset asset = new SyntheticsAsset();
                asset.setWatchPrice(price);
                asset.setActive(true);
                asset.setAlertMedium("Default Medium");
            }
        }
    }

    private double calculatePriceToSet(TradeRequest request, double levelPercent) {
        //Round to the symbols decimal places.
        double price  = Math.abs(request.getEntryPrice()/Math.floor(100/levelPercent));
        return price;
    }
}
