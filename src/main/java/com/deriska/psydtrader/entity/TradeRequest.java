package com.deriska.psydtrader.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@RequiredArgsConstructor
@Data
public class TradeRequest extends Auditable<String>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;
    private Long accountId;
    private String tradeType;
    private double stopLossPrice;
    private double entryPrice;
    private double takeProfitPrice;
    private double stopLostPips;
    private double lotSize;
    private double takeProfitPips;
    private double riskAccountSizeInPercent;
    private double profitAccountSizeInPercent;
    private String currency;
    private String assetCategory;
    private boolean calculateForMe;
    private Long tradingPlanId;
    private String tradingPlanName;
//    @Transient
//    private List<Analysis> requestAnalysis;
    private String asset;
    private double accountBalance;
    private double exchangeRate;

    public TradeRequest(Long requestId, Long accountId, String tradeType, double stopLossPrice, double entryPrice, double takeProfitPrice,
                        double stopLostPips, double lotSize, double takeProfitPips, String currency, String assetCategory,
                        Long tradingPlanId, String tradingPlanName, String asset, double accountBalance, double exchangeRate) {
        this.requestId = requestId;
        this.accountId = accountId;
        this.tradeType = tradeType;
        this.stopLossPrice = stopLossPrice;
        this.entryPrice = entryPrice;
        this.takeProfitPrice = takeProfitPrice;
        this.stopLostPips = stopLostPips;
        this.lotSize = lotSize;
        this.takeProfitPips = takeProfitPips;
        this.currency = currency;
        this.assetCategory = assetCategory;
        this.tradingPlanId = tradingPlanId;
        this.tradingPlanName = tradingPlanName;
        this.asset = asset;
        this.accountBalance = accountBalance;
        this.exchangeRate = exchangeRate;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public double getStopLossPrice() {
        return stopLossPrice;
    }

    public void setStopLossPrice(double stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getTakeProfitPrice() {
        return takeProfitPrice;
    }

    public void setTakeProfitPrice(double takeProfitPrice) {
        this.takeProfitPrice = takeProfitPrice;
    }

    public double getStopLostPips() {
        return stopLostPips;
    }

    public void setStopLostPips(double stopLostPips) {
        this.stopLostPips = stopLostPips;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public double getLotSize() {
        return lotSize;
    }

    public void setLotSize(double lotSize) {
        this.lotSize = lotSize;
    }

    public String getTradingPlanName() {
        return tradingPlanName;
    }

    public void setTradingPlanName(String tradingPlanName) {
        this.tradingPlanName = tradingPlanName;
    }

    public double getTakeProfitPips() {
        return takeProfitPips;
    }

    public void setTakeProfitPips(double takeProfitPips) {
        this.takeProfitPips = takeProfitPips;
    }

    public Long getTradingPlanId() {
        return tradingPlanId;
    }

    public void setTradingPlanId(Long tradingPlanId) {
        this.tradingPlanId = tradingPlanId;
    }

    public String getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(String assetCategory) {
        this.assetCategory = assetCategory;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }
}
