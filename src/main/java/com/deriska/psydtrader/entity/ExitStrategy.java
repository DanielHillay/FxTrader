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
public class ExitStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long lossExitId;
    private Long journalId;
    private Long tradingSystemId;
    private int count;
    private double targetPercentageLoss;
    private Long tradingPlanId;
    private double lotSizePercentWhenTradeInLoss;
    private double allowedLossLevelPercentage;
    private double lotSizePercentWhenTradeInProfit;
    private double sLPlacementPercentAfterProfit;
    private double inTradeProfitLevel;
    private double stopLossPercentAfterTradeInProfit;
}
