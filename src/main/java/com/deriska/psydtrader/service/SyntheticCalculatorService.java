package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.RiskAnalysisResponse;
import com.deriska.psydtrader.entity.TradeRequest;
import com.deriska.psydtrader.entity.Trades;
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
    private PsychEvalService psychEvalService;
    @Autowired
    private TradeJournalRepo journalRepo;

    DecimalFormat df = new DecimalFormat("###.##");

    public RiskAnalysisResponse calculateAll(TradeRequest trade) {

        RiskAnalysisResponse response = new RiskAnalysisResponse();
//        Trades tradeJournal = journalRepo.findByTradeRequestId(trade.getRequestId()).get();
        String currency = trade.getCurrency();
        if(trade.getAsset().startsWith(STEP)) {
            response.setLossAmount(calculateStepLossAmount(trade.getLotSize(), trade.getStopLossPrice(), trade.getEntryPrice()));
            response.setProfitAmount(calculateStepProfitAmount(trade.getLotSize(), trade.getTakeProfitPrice(), trade.getEntryPrice()));
            if(trade.isCalculateForMe()){
                response.setStopLossPrice(calculateStepStopLossPriceFromRiskPercent(trade));
                response.setTakeProfitPrice(calculateStepTakeProfitPriceFromRiskPercent(trade));
            }else{
                response.setTakeProfitPrice(trade.getTakeProfitPrice());
                response.setStopLossPrice(trade.getStopLossPrice());
            }
        }else{
            if(trade.isCalculateForMe()){
                response.setStopLossPrice(calculateStopLossPriceFromRiskPercent(trade));
                response.setTakeProfitPrice(calculateTakeProfitPriceFromRiskPercent(trade));
            }else{
                response.setTakeProfitPrice(trade.getTakeProfitPrice());
                response.setStopLossPrice(trade.getStopLossPrice());
            }
            response.setLossAmount(calculateLossAmount(trade.getLotSize(), trade.getStopLossPrice(), trade.getEntryPrice()));
            response.setProfitAmount(calculateProfitAmount(trade.getLotSize(),trade.getTakeProfitPrice(),trade.getEntryPrice()));
        }

        response.setPercentageLoss(calculatePercentageLoss(trade.getAccountBalance(), response.getLossAmount()));
        response.setPercentageProfit(calculatePercentageProfit(trade.getAccountBalance(), response.getProfitAmount()));
        response.setStopLossPips(calculateStopLossPips(trade.getStopLossPrice(), trade.getEntryPrice()));
        response.setTakeProfitPips(calculateTakeProfitPips( trade.getTakeProfitPrice(), trade.getEntryPrice()));
        response.setRiskRewardRatio(calculateRiskRewardRatio(response.getProfitAmount(), response.getLossAmount()));
        response.setTradeType(trade.getTradeType());


        response.setPsychEvalScore(psychEvalService.initialTradeEvaluation(response, trade));
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

        return stopLossPrice;
    }
    private double calculateStepTakeProfitPriceFromRiskPercent(TradeRequest trade) {
        double takeProfitPrice = 0;
        double result = (((trade.getProfitAccountSizeInPercent()/100)*trade.getAccountBalance())/stepContractSize)/trade.getLotSize();
        if(trade.getTradeType().equalsIgnoreCase("SELL")){
            takeProfitPrice = trade.getEntryPrice() - result;
        }else{
            takeProfitPrice = trade.getEntryPrice() +  result;
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

        return stopLossPrice;
    }
    private double calculateTakeProfitPriceFromRiskPercent(TradeRequest trade) {
        double takeProfitPrice = 0;
        double result = ((trade.getProfitAccountSizeInPercent()/100)*trade.getAccountBalance())/trade.getLotSize();
        if(trade.getTradeType().equalsIgnoreCase("SELL")){
            takeProfitPrice = trade.getEntryPrice() - result;
        }else{
            takeProfitPrice = trade.getEntryPrice() + result;
        }

        return takeProfitPrice;
    }

    private Trades updateTradeJournalWithResponse(RiskAnalysisResponse response, Trades trades) {
        trades.setTradeScore(response.getPsychEvalScore());
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
        double result = Math.abs(entryPrice - takeProfitPrice);
        return result;
    }

    private double calculateStopLossPips( double stopLossPrice, double entryPrice) {
        double result = Math.abs(entryPrice - stopLossPrice);
        return result;
    }


    private void currencySetter(RiskAnalysisResponse response, double exchangeRate, String currency) {
    }

    private double calculatePercentageProfit(double accountBalance, double profitAmount) {
        double result = (profitAmount/accountBalance)*100;
        return result;
    }

    private double calculatePercentageLoss(double accountBalance, double lossAmount ) {
        double result = (lossAmount/accountBalance)*100;
        return Double.valueOf(df.format(result));
    }


    private double calculateLossAmount(double lotSize, double stopLossPrice, double entryPrice) {
        double result = Math.abs(entryPrice - stopLossPrice)*lotSize;
        return result;
    }

    private double calculateProfitAmount(double lotSize, double takeProfitPrice, double entryPrice){
        double result = Math.abs(entryPrice - takeProfitPrice)*lotSize;
        return result;
    }

    private double calculateStepLossAmount(double lotSize, double stopLossPrice, double entryPrice){
        double result = Math.abs(entryPrice - stopLossPrice)*lotSize;
        double stepResult = result * stepContractSize;
        return stepResult;
    }

    private double calculateStepProfitAmount(double lotSize, double takeProfitPrice, double entryPrice) {
        double result = Math.abs(entryPrice - takeProfitPrice)*lotSize;
        double stepResult = result * stepContractSize;
        return stepResult;
    }
}
