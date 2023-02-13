package com.deriska.psydtrader.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@RequiredArgsConstructor
public class RiskAnalysisResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseId;
    private double riskRewardRatio;
    private Long tradeId;
    private double entryPrice;
    private double stopLossPrice;
    private double takeProfitPrice;
    private double stopLossPips;
    private double takeProfitPips;
    private double recommendedLotSize;
    private double profitAmount;
    private double lossAmount;
    private String remarks;
    private String tradeType;
    private String tradingPlanName;
    private double psychEvalScore;
    private double percentageLoss;
    private double percentageProfit;


}
