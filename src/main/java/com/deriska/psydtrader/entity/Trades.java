package com.deriska.psydtrader.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@RequiredArgsConstructor
@Data
public class Trades extends Auditable<String>{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String asset;
    private double entryPrice;
    private String assetCategory;
    private String tradeType;
    private Long tradeRequestId;
    private double stopLoss;
    private double takeProfit;
    private double lotSize;
    private double pipsProfit;
    private double pipsLoss;
    private double riskRewardRatio;
    private double percentageProfit;
    private double amountProfit;
    private double amountLoss;
    private double percentageLoss;
    private String profitability;
    private Long accountId;
    private double accountChange;
    private double tradeScore;
    private String currency;
    private String tradeDuration;
    private String tradeRemarks;
    private Long planId;
    private boolean wasChanged;
    private double openingBalance;

    private double closingBalance;
    private boolean status;
    private boolean isProfit;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime entryDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime exitDate;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "TRADE_ANALYSIS",
            joinColumns = {
                    @JoinColumn(name = "TRADE_ID")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "ANALYSIS_ID")
            }
    )
    private List<Analysis> listOfAnalysis;

    public Trades(Long id, String asset, String assetCategory, double entryPrice, String tradeType, Long tradeRequestId, double stopLoss, double takeProfit,
                  double lotSize, double pipsProfit, double pipsLoss, double riskRewardRatio, double percentageProfit, double amountProfit,
                  double amountLoss, double percentageLoss, String profitability, Long accountId, double accountChange, boolean wasChanged, double tradeScore,
                  String tradeDuration, String tradeRemarks, Long planId, double openingBalance, double closingBalance,
                  boolean status, LocalDateTime entryDate,String currency, LocalDateTime exitDate, boolean isProfit, List<Analysis> listOfAnalysis) {
        this.id = id;
        this.asset = asset;
        this.assetCategory = assetCategory;
        this.entryPrice = entryPrice;
        this.tradeType = tradeType;
        this.tradeRequestId = tradeRequestId;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.lotSize = lotSize;
        this.pipsProfit = pipsProfit;
        this.currency  = currency;
        this.pipsLoss = pipsLoss;
        this.riskRewardRatio = riskRewardRatio;
        this.percentageProfit = percentageProfit;
        this.amountProfit = amountProfit;
        this.amountLoss = amountLoss;
        this.percentageLoss = percentageLoss;
        this.profitability = profitability;
        this.accountId = accountId;
        this.accountChange = accountChange;
        this.wasChanged = wasChanged;
        this.tradeScore = tradeScore;
        this.tradeDuration = tradeDuration;
        this.tradeRemarks = tradeRemarks;
        this.planId = planId;
        this.isProfit = isProfit;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.status = status;
        this.entryDate = entryDate;
        this.exitDate = exitDate;
        this.listOfAnalysis = listOfAnalysis;
    }

    public Trades(TradeRequest trade){
        this.entryPrice = trade.getEntryPrice();
        this.stopLoss = trade.getStopLossPrice();
        this.lotSize = trade.getLotSize();
        this.asset = trade.getAsset();
        this.tradeType = trade.getTradeType();
        this.currency = trade.getCurrency();
        this.openingBalance = trade.getAccountBalance();
        this.takeProfit = trade.getTakeProfitPrice();
        this.tradeRequestId = trade.getRequestId();
        this.accountId = trade.getAccountId();
        this.assetCategory = trade.getAssetCategory();
    }

}

