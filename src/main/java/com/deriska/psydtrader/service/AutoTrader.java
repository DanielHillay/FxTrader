package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.*;
import com.deriska.psydtrader.entity.Pojo.RunningTradeRequest;
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
    private SyntheticCalculatorService syntheticCalculatorService;
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


        try {
            // Call a backgroundRunner here.
            RiskAnalysisResponse response = calculationService.calculateRiskAnalysis(tradeRequest);
            if (response.getPsychEvalScore() < 50) {
                // Warn the trader of his error and suggest actions to take.
                response.setRemarks("You have ignored some steps in your trading plan, to score higher in your next trades, " +
                        "please stick to your trading plan as much as possible");
                Long tradeId = journalRepo.save(syntheticCalculatorService.updateTradeJournalWithResponse(response, tradeJournal)).getId();
                tradeHolder.setTradeId(tradeId);
                response.setTradeId(tradeId);
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
                if(!ObjectUtils.isEmpty(ex.getSLPlacementPercentAfterProfit()) && ex.getSLPlacementPercentAfterProfit() != 0) {
                    sLPrice = calculatePriceToSet(request, ex.getSLPlacementPercentAfterProfit());
                }
                double lotSize = calculateLotSizeChange(request.getLotSize(), ex.getLotSizePercentWhenTradeInProfit());
                SyntheticsAsset asset = new SyntheticsAsset();
                asset.setSymbol(request.getAsset());
                asset.setWatchPrice(price);
                asset.setActive(true);
                asset.setAlertMedium("Default Medium");
                asset.setRemark("You ought to set your SL at "+ sLPrice +". ");

            }
            if(!ObjectUtils.isEmpty(ex.getAllowedLossLevelPercentage())){
                double price = calculatePriceToSet(request, ex.getAllowedLossLevelPercentage());
                SyntheticsAsset asset = new SyntheticsAsset();
                asset.setSymbol(request.getAsset());
                asset.setWatchPrice(price);
                asset.setActive(true);
                asset.setAlertMedium("Default Medium");
            }
            if(!ObjectUtils.isEmpty(ex.getStopLossPercentAfterTradeInProfit())){
                double price = calculatePriceToSet(request, ex.getStopLossPercentAfterTradeInProfit());
                SyntheticsAsset asset = new SyntheticsAsset();
                asset.setSymbol(request.getAsset());
                asset.setWatchPrice(price);
                asset.setActive(true);
                asset.setAlertMedium("Default Medium");
                asset.setRemark("You should set your SL price at " +price+ ". ");
            }
        }
    }

    private double calculateLotSizeChange(double lotSize, double lotSizePercentWhenTradeInProfit) {
        return Math.abs(lotSize/(100/lotSizePercentWhenTradeInProfit));

    }

    private double calculatePriceToSet(TradeRequest request, double levelPercent) {
        //Round to the symbols decimal places.
        double price  = Math.abs(request.getEntryPrice()/Math.floor(100/levelPercent));
        return price;
    }

    public void closeTrade(RunningTradeRequest request) {
        System.out.print("Trade has been closed");
    }
}
