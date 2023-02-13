package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.*;
import com.deriska.psydtrader.entity.Pojo.RunningTradeRequest;
import com.deriska.psydtrader.repository.*;
import io.jsonwebtoken.lang.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PsychEvalService {

    @Autowired
    private TradeJournalRepo journalRepo;
    @Autowired
    private TradingPlanRepo tradingPlanRepo;
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private RiskManagementRepo riskManagementRepo;
    @Autowired
    private TradeChangesRepo tradeChangesRepo;
    @Autowired
    private TradeHolderRepo holderRepo;
    @Autowired
    private DerivService derivService;
    @Autowired
    private PsychEvalService psychEvalService;
    @Autowired
    private TradeRequestRepo tradeRequestRepo;
    @Autowired
    private TradeJournalService tradingJournalService;
    @Autowired
    private RiskAnalysisResponseRepo responseRepo;
    @Autowired
    private TradingSystemRepo tradingSystemRepo;
    @Autowired
    private ExitStrategyRepo exitStrategyRepo;


    public double initialTradeEvaluation(RiskAnalysisResponse response, TradeRequest trade) {
        double score = 0;
        double check = 0;

        if(trade.getTradingPlanName().isEmpty()){
            if ((check != response.getStopLossPrice())) {
                score = score + 10;
            }
            if ((check != response.getTakeProfitPrice())) {
                score = score + 10;
            }
            if (response.getRiskRewardRatio() >= 2) {
                score = score + 10;
            }
            if (response.getPercentageLoss() <= 2) {
                score = score + 10;
            }
//            if (Objects.nonNull(trade.getRequestAnalysis())) {
//                score = score + 5;
//            }

            return score;
        }else {
            TradingPlan tradePlan = tradingPlanRepo.findByPlanName(trade.getTradingPlanName()).get();
            Long tradeId = tradePlan.getPlanId();
            TradingSystem system = tradingSystemRepo.findByTradingPlanId(tradeId).get();
            List<ExitStrategy> exitStrategies = exitStrategyRepo.findByTradingSystemId(system.getSystemId());
            RiskManagement riskManagement = riskManagementRepo.findByTradingPlanId(tradeId).get();


            if ((check != trade.getStopLossPrice())) {
                score = score + 15;
            }
            if ((check != trade.getTakeProfitPrice())) {
                score = score + 15;
            }
            if (!(ObjectUtils.isEmpty(riskManagement)) && response.getRiskRewardRatio() == riskManagement.getRiskRewardRatio()) {
                score = score + 25;
            }
            if (!(ObjectUtils.isArray(riskManagement)) && response.getPercentageLoss() == riskManagement.getMaxRiskPercentPerTrade()) {
                score = score + 25;
            }
            if (!(ObjectUtils.isEmpty(system.getSystemAnalysis()))) {
                score = score + 20;
            }
            if(!(ObjectUtils.isEmpty(exitStrategies))){
                score = score + 10;
            }

            return score;
        }

        //If this was a trade, You scored 45% (with sad emoji). Unlock Our Risk analyzer to gain more insight into you trade.

    }

    //Method to check the current state of opened trades or contracts and update the trade score as follows
    //The method accepts a trade status check parameter from deriv and engages in some checks and the
    // initial tradeRequest and RiskAnalysisResponse.
    public double evaluateWhileInTrade(TradeRequest initRequest, RunningTradeRequest derivRequest, Trades trades, RiskManagement riskManagement){
        double score = trades.getTradeScore();
        TradingPlan tradingPlan = tradingPlanRepo.findByTradeId(trades.getId()).get();
//        RiskManagement riskManagement = riskManagementRepo.findByTradingPlanId(tradingPlan.getPlanId()).get();
        List<TradeChanges> changes = tradeChangesRepo.findByTradeIdOrderByCreatedDateDesc(trades.getId());
        TradeChanges tradeChanges = changes.get(0);
        int changeCount = tradeChanges.getChangeNumber();
        TradeChanges change = new TradeChanges();
        String contractType = initRequest.getTradeType();
        if(contractType.equalsIgnoreCase("SELL")){
            if(!(derivRequest.getStopLossPrice() == tradeChanges.getStopLossChange())){
                change.setChangeNumber(++changeCount);
                change.setStopLossChange(derivRequest.getStopLossPrice());
                if(derivRequest.isInProfit()){
                    double profitLevelPercent = calculateCurrentProfitLevelPercent(trades, derivRequest.getCurrentPrice());
                    double stopLossProfitPercent = calculateStopLossProfitPercentage(trades, derivRequest.getStopLossPrice());
                    double stopLossLevelAfterTradeInProfit = calculateCurrentLossLevelPercent(trades, derivRequest.getStopLossPrice());
                    if(derivRequest.getStopLossPrice() == 0){
                        score = score - 10;
                        change.setPositiveToTrade(false);
                        change.setRiskRewardRatio(0);

                    }
                    if((derivRequest.getStopLossPrice() < initRequest.getStopLossPrice()
                            && derivRequest.getStopLossPrice() > initRequest.getEntryPrice())
                            && !(profitLevelPercent >= riskManagement.getInTradeProfitLevel())
                            && (stopLossLevelAfterTradeInProfit != riskManagement.getStopLossPercentAfterTradeInProfit())){
                        score = score - 4;
                        change.setPositiveToTrade(false);
                        change.setRiskRewardRatio(calculateCurrentRiskToRewardRatio(derivRequest, initRequest.getEntryPrice()));
                    }
                    if((derivRequest.getStopLossPrice() < initRequest.getEntryPrice())
                            && !(profitLevelPercent >= riskManagement.getInTradeProfitLevel())
                            && (stopLossProfitPercent != riskManagement.getSlAfterProfitPercent())){
                        score = score - 4;
                        change.setPositiveToTrade(false);
                    }
                    if(derivRequest.getTakeProfitPrice() != initRequest.getTakeProfitPrice()){
                        score = score - 7;
                        change.setPositiveToTrade(false);
                        change.setTakeProfitChange(derivRequest.getTakeProfitPrice());
                    }
                    if(derivRequest.getTakeProfitPrice() == 0){
                        score = score - 10;
                        change.setPositiveToTrade(false);
                        change.setTakeProfitChange(0);
                    }
                    if(derivRequest.getLotSize() != initRequest.getLotSize()){
                        double lotSizeAfterTradeInProfit = calculateLotSizeAfterTradeInProfit(trades, derivRequest.getLotSize());
                        if(lotSizeAfterTradeInProfit != riskManagement.getLotSizePercentWhenTradeInProfit()
                                && !(profitLevelPercent >= riskManagement.getInTradeProfitLevel())){
                            score = score - 4;
                            change.setPositiveToTrade(false);
                        }
                    }
                }else{
                    score = score - 8;
                    if(derivRequest.getLotSize() < initRequest.getLotSize()) {
                        double lossLevelPercent = calculateCurrentLossLevelPercent(trades, derivRequest.getCurrentPrice());
                        double allowedLotSizeWhenTradeInLoss = calculateAllowedLotSizePercentWhenTradeInLoss(derivRequest.getLotSize(), initRequest.getLotSize());
                        if (lossLevelPercent > riskManagement.getAllowedLossLevelPercentage()
                                && !(allowedLotSizeWhenTradeInLoss >= riskManagement.getLotSizePercentWhenTradeInLoss())) {
                            score = score - 4;
                            change.setPositiveToTrade(false);
                        }
                    }
                }
            }
        }else{
            if(!(derivRequest.getStopLossPrice() == tradeChanges.getStopLossChange())){
                change.setChangeNumber(++changeCount);
                change.setStopLossChange(derivRequest.getStopLossPrice());
                if(derivRequest.isInProfit()){
                    double profitLevelPercent = calculateCurrentProfitLevelPercent(trades, derivRequest.getCurrentPrice());
                    double stopLossProfitPercent = calculateStopLossProfitPercentage(trades, derivRequest.getStopLossPrice());
                    double stopLossLevelAfterTradeInProfit = calculateCurrentLossLevelPercent(trades, derivRequest.getStopLossPrice());
                    if(derivRequest.getStopLossPrice() == 0){
                        score = score - 10;
                        change.setPositiveToTrade(false);
                        change.setRiskRewardRatio(0);
                    }
                    if((derivRequest.getStopLossPrice() > initRequest.getStopLossPrice()
                            && derivRequest.getStopLossPrice() < initRequest.getEntryPrice())
                            && profitLevelPercent != riskManagement.getInTradeProfitLevel()
                            && stopLossLevelAfterTradeInProfit != riskManagement.getStopLossPercentAfterTradeInProfit()){
                        score = score - 4;
                        change.setPositiveToTrade(false);
                        change.setRiskRewardRatio(calculateCurrentRiskToRewardRatio(derivRequest, initRequest.getEntryPrice()));
                    }
                    if((derivRequest.getStopLossPrice() > initRequest.getEntryPrice())
                            && profitLevelPercent != riskManagement.getInTradeProfitLevel()
                            && stopLossProfitPercent != riskManagement.getSlAfterProfitPercent()){
                        score = score - 4;
                        change.setPositiveToTrade(false);
                    }
                    if(derivRequest.getTakeProfitPrice() != initRequest.getTakeProfitPrice()){
                        score = score - 7;
                        change.setPositiveToTrade(false);
                    }
                    if(derivRequest.getTakeProfitPrice() == 0){
                        score = score - 10;
                        change.setPositiveToTrade(false);
                        change.setTakeProfitChange(0);
                    }
                    if(derivRequest.getLotSize() != initRequest.getLotSize()){
                        double lotSizeAfterTradeInProfit = calculateLotSizeAfterTradeInProfit(trades, derivRequest.getLotSize());
                        if(lotSizeAfterTradeInProfit != riskManagement.getLotSizePercentWhenTradeInProfit()
                                && profitLevelPercent != riskManagement.getInTradeProfitLevel()){
                            score = score - 4;
                        }
                    }
                }else{
                    score = score - 8;
                    if(derivRequest.getLotSize() < initRequest.getLotSize()) {
                        double lossLevelPercent = calculateCurrentLossLevelPercent(trades, derivRequest.getCurrentPrice());
                        double allowedLotSizeWhenTradeInLoss = calculateAllowedLotSizePercentWhenTradeInLoss(derivRequest.getLotSize(), initRequest.getLotSize());
                        if (lossLevelPercent > riskManagement.getAllowedLossLevelPercentage()
                                && (allowedLotSizeWhenTradeInLoss != riskManagement.getLotSizePercentWhenTradeInLoss())) {
                            score = score - 4;
                        }
                    }
                }
            }
        }
        change.setChangeDate(LocalDateTime.now());
        change.setTradeScore(score);
        tradeChangesRepo.save(change);
        return score;

    }

    private double calculateCurrentRiskToRewardRatio(RunningTradeRequest derivRequest, double entryPrice) {
        double RRR = Math.abs(derivRequest.getTakeProfitPrice() - entryPrice)/Math.abs(entryPrice - derivRequest.getStopLossPrice() );
        return RRR;
    }

    private double calculateLotSizeAfterTradeInProfit(Trades trades, double lotSize) {
        double lotSiz = 0;
        double result = Math.abs(lotSize/trades.getLotSize());
        lotSiz = result * 100;
        return lotSiz;
    }

    private double calculateAllowedLotSizePercentWhenTradeInLoss(double lotSize, double initLotSize) {
        double lotSizes = 0;
        double result = Math.abs(lotSize/initLotSize);
        lotSizes = result * 100;
        return lotSizes;
    }


    private double calculateCurrentLossLevelPercent(Trades trades, double currentPrice) {
        double lossLevel = 0;
        double result = Math.abs(trades.getEntryPrice() - currentPrice)/trades.getPipsLoss();
        lossLevel = result * 100;
        return lossLevel;
    }

    private double  calculateStopLossProfitPercentage(Trades trades, double currentSlPrice) {
        double profitPercent = 0;
        double sLprofit = Math.abs(currentSlPrice - trades.getEntryPrice())/trades.getPipsProfit();
        profitPercent = sLprofit * 100;
        return profitPercent;
    }

    private double calculateCurrentProfitLevelPercent(Trades trades, double currentPrice) {
        double profitLevel = 0;
        double currentProfitPercent = Math.abs(trades.getEntryPrice() - currentPrice)/trades.getPipsProfit();
        profitLevel = currentProfitPercent * 100;

        return profitLevel;
    }


    public TradeChanges updateTradeRecordAfterTradeIsClosed(RunningTradeRequest request, Trades trades) {

        TradingPlan tradingPlan = tradingPlanRepo.findByTradeId(trades.getId()).get();
        int count = 10;
        List<TradeChanges> changes = tradeChangesRepo.findByTradeIdOrderByCreatedDateDesc(trades.getId());
        TradeChanges tradeChanges = new TradeChanges();
        if(ObjectUtils.isEmpty(changes)){
            trades.setWasChanged(false);
        }else {
            trades.setWasChanged(true);
            tradeChanges = changes.get(0);
        }
        RiskManagement riskManagement = riskManagementRepo.findByTradingPlanId(tradingPlan.getPlanId()).get();
//        ExitStrategy exitStrategy = exitStrategyRepo.findByTradingPlanIdAndCount(tradingPlan.getPlanId(), count).get();

        List<ExitStrategy> exitStrategies = exitStrategyRepo.findByTradingPlanId(tradingPlan.getPlanId());
        TradeChanges endTradeCondition = new TradeChanges(tradeChanges);
        if(request.isEndedInProfit()){
            if(request.getExitPrice() != trades.getTakeProfit()){
                endTradeCondition.setTradeScore(endTradeCondition.getTradeScore() - 5);
            }
            for(ExitStrategy ex : exitStrategies) {
                double sLpercent = ex.getStopLossPercentAfterTradeInProfit();
                if (!ObjectUtils.isEmpty(sLpercent)) {
                    double profitPercentage = calculateStopLossProfitPercentage(trades, request.getExitPrice());
                   if (profitPercentage == sLpercent){
                       endTradeCondition.setTradeScore(endTradeCondition.getTradeScore() - 3);
                   }
                }
            }
        }else{
            if(request.getExitPrice() != trades.getStopLoss()){
                endTradeCondition.setTradeScore(endTradeCondition.getTradeScore() - 5);
            }
        }
        endTradeCondition.setActive(false);
        return endTradeCondition;
    }
    
}
