package com.deriska.psydtrader.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@RequiredArgsConstructor
@Data
public class RiskManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long riskId;
    private String riskManagementType;
    private int riskCount;
    private Long tradingPlanId;
    private Long tradeId;
    private double accountNumber;
    private Long accountId;
    private String userId;
    private double riskRewardRatio;
    private double minAcceptedScore;
    private double lotSizePercentWhenTradeInLoss;
    private double lotSizePercentWhenTradeInProfit;
    private double allowedLossLevelPercentage;
    private double maxRiskPercentPerTrade;
    private double totalPercentRiskPerDay;
    private double maxProfitPercentPerTrade;
    private double totalProfitPercentPerDay;
    private double slAfterProfitPercent;
    private double inTradeProfitLevel;
    private double stopLossPercentAfterTradeInProfit;
}
