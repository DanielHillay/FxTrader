package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.*;
import com.deriska.psydtrader.entity.Pojo.RunningTradeRequest;
import com.deriska.psydtrader.repository.*;
import com.deriska.psydtrader.service.websocket.WSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.DeploymentException;
import java.io.IOException;

@Service
public class DerivService {

    @Autowired
    private TradeHolderRepo holderRepo;
    @Autowired
    private TradeJournalRepo journalRepo;
    @Autowired
    private PsychEvalService psychEvalService;
    @Autowired
    private TradeRequestRepo tradeRequestRepo;
    @Autowired
    private TradingPlanRepo tradingPlanRepo;
    @Autowired
    private TradeJournalService tradingJournalService;
    @Autowired
    private RiskManagementRepo riskManagementRepo;
    @Autowired
    private RiskAnalysisResponseRepo responseRepo;
    @Autowired
    private ExitStrategyRepo exitStrategyRepo;

    private Long tradeId;

    public void createConnectionWithDeriv() throws DeploymentException, IOException, InterruptedException {
        WSClient.makeConnectionToDeriv();
    }

    public void evaluateTrade(RunningTradeRequest request){
//        RiskAnalysisRequest request = new RiskAnalysisRequest();//Replace with function that calls Deriv API to check current state of trade
        Trades trades = journalRepo.findByEntryPrice(request.getEntryPrice()).get();
        this.tradeId = trades.getId();
        TradeHolder holder = holderRepo.findTopByTradeIdOrderByCreatedDateDesc(tradeId).get();
        int count = 0;
        if(request.isActive()) {
            if (request.getStopLossPrice() != holder.getPreviousStopLossLevel()
                    || request.getLotSize() != holder.getPreviousLotSize()
                    || request.getTakeProfitPrice() != holder.getPreviousTakeProfitLevel()) {
                count = count + 1;
                TradingPlan tradingPlan = tradingPlanRepo.findByTradeId(tradeId).get();
                ExitStrategy exitStrategy = exitStrategyRepo.findByTradingPlanIdAndCount(tradingPlan.getPlanId(), count).get();
                RiskManagement riskManagement = riskManagementRepo.findByTradingPlanIdAndRiskCount(tradingPlan.getPlanId(), count).get();
                TradeRequest tradeRequest = tradeRequestRepo.findByEntryPrice(request.getEntryPrice()).get();
                psychEvalService.evaluateWhileInTrade(tradeRequest, request, trades, riskManagement);

                holder.setPreviousStopLossLevel(request.getStopLossPrice());
                holder.setPreviousTakeProfitLevel(request.getTakeProfitPrice());
                holder.setPreviousLotSize(request.getLotSize());

                holderRepo.save(holder);
            }
        }else{
            TradeChanges tradeChange = psychEvalService.updateTradeRecordAfterTradeIsClosed(request, trades);
            tradingJournalService.updateRecordAfterClosedTrade(request, trades, tradeChange);
        }
    }

    public Account getAccountStatus(Long accountId){
        Account accountCheck = new Account();
        return accountCheck;
    }
}
