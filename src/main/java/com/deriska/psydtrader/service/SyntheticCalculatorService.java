package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.*;
import com.deriska.psydtrader.repository.TradeHolderRepo;
import com.deriska.psydtrader.repository.TradeJournalRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.DecimalFormat;

@Service
public class SyntheticCalculatorService {

    private static final int contractSize = 1;
    private static final int stepContractSize = 10;

    private static final String STEP = "STEP";

    @Autowired
    private TradeHolderRepo holderRepo;

    @Autowired
    private PsychEvalService psychEvalService;
    @Autowired
    private TradeJournalRepo journalRepo;

    DecimalFormat df = new DecimalFormat("###.##");

    public RiskAnalysisResponse calculateAll(TradeRequest trade) {

        Trades tradeJournal = new Trades(trade);

        TradeHolder tradeHolder = new TradeHolder();
        TradeChanges tradeChanges = new TradeChanges();
        tradeChanges.setTradeRequestId(trade.getRequestId());
        tradeHolder.setTradeId(journalRepo.save(tradeJournal).getId());
        RiskAnalysisResponse response = new RiskAnalysisResponse();
//        Trades tradeJournal = journalRepo.findByTradeRequestId(trade.getRequestId()).get();
        String currency = trade.getCurrency();
        if(trade.getAsset().startsWith(STEP)) {
            if(trade.isCalculateForMe()){
                response.setStopLossPrice(calculateStepStopLossPriceFromRiskPercent(trade));
                response.setTakeProfitPrice(calculateStepTakeProfitPriceFromRiskPercent(trade));

            }else{
                response.setTakeProfitPrice(trade.getTakeProfitPrice());
                response.setStopLossPrice(trade.getStopLossPrice());
            }
            response.setLossAmount(calculateStepLossAmount(trade.getLotSize(), trade.getStopLossPrice(), trade.getEntryPrice()));
            response.setProfitAmount(calculateStepProfitAmount(trade.getLotSize(), trade.getTakeProfitPrice(), trade.getEntryPrice()));
        }else{
            if(trade.isCalculateForMe()){
                response.setStopLossPrice(calculateStopLossPriceFromRiskPercent(trade));
                response.setTakeProfitPrice(calculateTakeProfitPriceFromRiskPercent(trade));
            }else{
                response.setTakeProfitPrice(trade.getTakeProfitPrice());
                response.setStopLossPrice(trade.getStopLossPrice());
            }
            response.setLossAmount(calculateLossAmount(trade.getLotSize(), response.getStopLossPrice(), trade.getEntryPrice()));
            response.setProfitAmount(calculateProfitAmount(trade.getLotSize(), response.getTakeProfitPrice(),trade.getEntryPrice()));
        }

        response.setStopLossPips(calculateStopLossPips(response.getStopLossPrice(), trade.getEntryPrice()));
        response.setTakeProfitPips(calculateTakeProfitPips(response.getTakeProfitPrice(), trade.getEntryPrice()));
        response.setPercentageLoss(calculatePercentageLoss(trade.getAccountBalance(), response.getLossAmount()));
        response.setPercentageProfit(calculatePercentageProfit(trade.getAccountBalance(), response.getProfitAmount()));
        response.setEntryPrice(trade.getEntryPrice());;
        response.setRiskRewardRatio(calculateRiskRewardRatio(response.getProfitAmount(), response.getLossAmount()));
        response.setTradeType(trade.getTradeType());
        response.setRecommendedLotSize(calculateRecommendedLotSize(response.getLossAmount(), trade.getEntryPrice(), response.getStopLossPrice()));

        response.setPsychEvalScore(psychEvalService.initialTradeEvaluation(response, trade));
        response.setRemarks("To score higher and stand a chance of winning $1000 every month, please sign up with us and register a trading plan");
        journalRepo.save(tradeJournal);
        holderRepo.save(tradeHolder);
//        currencySetter(response, trade.getExchangeRate(), currency);
//        journalRepo.save(updateTradeJournalWithResponse(response, tradeJournal));
        return response;
    }

    private double calculateStepStopLossPriceFromRiskPercent(TradeRequest trade) {
        double stopLossPrice = 0;
        double result = (((trade.getRiskAccountSizeInPercent()/100)*trade.getAccountBalance())/stepContractSize)/trade.getLotSize();
        if(trade.getTradeType().equalsIgnoreCase("SELL")){
            stopLossPrice = trade.getEntryPrice() + result;
        }else{
            stopLossPrice = trade.getEntryPrice() - result;
        }
        //approx to the symbols pips value OR decimal place
        return Double.valueOf(df.format(stopLossPrice));
    }
    private double calculateStepTakeProfitPriceFromRiskPercent(TradeRequest trade) {
        double takeProfitPrice = 0;
        double result = (((trade.getProfitAccountSizeInPercent()/100)*trade.getAccountBalance())/stepContractSize)/trade.getLotSize();
        if(trade.getTradeType().equalsIgnoreCase("SELL")){
            takeProfitPrice = trade.getEntryPrice() - result;
        }else{
            takeProfitPrice = trade.getEntryPrice() + result;
        }

        return takeProfitPrice;
    }
    private double calculateStopLossPriceFromRiskPercent(TradeRequest trade) {
        double stopLossPrice = 0;
        double result = ((trade.getRiskAccountSizeInPercent()/100)*trade.getAccountBalance())/trade.getLotSize();
        if(trade.getTradeType().equalsIgnoreCase("SELL")){
            stopLossPrice = trade.getEntryPrice() + result;
        }else{
            stopLossPrice = trade.getEntryPrice() - result;
        }

        return Double.valueOf(df.format(stopLossPrice));
    }
    private double calculateTakeProfitPriceFromRiskPercent(TradeRequest trade) {
        double takeProfitPrice = 0;
        double result = ((trade.getProfitAccountSizeInPercent()/100)*trade.getAccountBalance())/trade.getLotSize();
        if(trade.getTradeType().equalsIgnoreCase("SELL")){
            takeProfitPrice = trade.getEntryPrice() - result;
        }else{
            takeProfitPrice = trade.getEntryPrice() + result;
        }

        return Double.valueOf(df.format(takeProfitPrice));
    }

    public Trades updateTradeJournalWithResponse(RiskAnalysisResponse response, Trades trades) {
        trades.setTradeScore(response.getPsychEvalScore());
        trades.setRiskRewardRatio(response.getRiskRewardRatio());
        trades.setTakeProfit(response.getTakeProfitPrice());
        trades.setStopLoss(response.getStopLossPrice());
        trades.setEntryPrice(response.getEntryPrice());
        trades.setAmountLoss(response.getLossAmount());
        trades.setAmountProfit(response.getProfitAmount());
        trades.setPipsLoss(response.getStopLossPips());
        trades.setPipsProfit(response.getTakeProfitPips());
        trades.setPercentageLoss(response.getPercentageLoss());
        trades.setPercentageProfit(response.getPercentageProfit());
        trades.setTradeScore(response.getPsychEvalScore());

        return trades;
    }

    private double calculateRiskRewardRatio(double profitAmount, double lossAmount) {
        double result = Math.abs(profitAmount/lossAmount);
        return Double.valueOf(df.format(result));
    }


    private double calculateTakeProfitPips( double takeProfitPrice, double entryPrice) {
        //based on the symbols pips decimal
        double result = Math.abs(entryPrice - takeProfitPrice);
        return Double.valueOf(df.format(result));
    }

    private double calculateStopLossPips( double stopLossPrice, double entryPrice) {
        //Based on the symbols pips decimal
        double result = Math.abs(entryPrice - stopLossPrice);
        return Double.valueOf(df.format(result));
    }


    private void currencySetter(RiskAnalysisResponse response, double exchangeRate, String currency) {
    }

    private double calculatePercentageProfit(double accountBalance, double profitAmount) {
        double result = (profitAmount/accountBalance)*100;
        return Double.valueOf(df.format(result));
    }

    private double calculatePercentageLoss(double accountBalance, double lossAmount ) {
        double result = (lossAmount/accountBalance)*100;
        return Double.valueOf(df.format(result));
    }


    private double calculateLossAmount(double lotSize, double stopLossPrice, double entryPrice) {
        double result = (entryPrice - stopLossPrice)*lotSize;
        return Double.valueOf(df.format(result));
    }

    private double calculateProfitAmount(double lotSize, double takeProfitPrice, double entryPrice){
        double result = (entryPrice - takeProfitPrice)*lotSize;
        return Double.valueOf(df.format(result));
    }

    private double calculateStepLossAmount(double lotSize, double stopLossPrice, double entryPrice){
        double result = (entryPrice - stopLossPrice)*lotSize;
        double stepResult = result * stepContractSize;
        return Double.valueOf(df.format(stepResult));
    }

    private double calculateStepProfitAmount(double lotSize, double takeProfitPrice, double entryPrice) {
        double result = (entryPrice - takeProfitPrice)*lotSize;
        double stepResult = result * stepContractSize;
        return Double.valueOf(df.format(stepResult));
    }

    private double calculateRecommendedLotSize(double lossAmount, double entryPrice, double stopLossPrice){
        double lotSize = Math.abs(lossAmount/(Math.abs(stopLossPrice - entryPrice)));
        return Double.valueOf(df.format(lotSize));
    }
}
