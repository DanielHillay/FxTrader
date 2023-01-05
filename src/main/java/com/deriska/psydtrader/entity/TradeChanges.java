package com.deriska.psydtrader.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@RequiredArgsConstructor
@Data
public class TradeChanges extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long changeId;
    private double stopLossChange;
    private Long tradeId;
    private Long tradeRequestId;
    private int changeNumber;
    private LocalDateTime changeDate;
    private double lotSizeChange;
    private double takeProfitChange;
    private double percentageLoss;
    private double percentageProfit;
    private double riskRewardRatio;
    private double amountProfit;
    private double amountLoss;
    private boolean isPositiveToTrade;
    private boolean isActive;
    private double tradeScore;
    private String remarks;

    public TradeChanges(TradeChanges changes){
        this.amountLoss = changes.getAmountLoss();
        this.amountProfit = changes.getAmountProfit();
        this.changeNumber = changes.getChangeNumber();
        this.percentageLoss = changes.getPercentageLoss();
        this.percentageProfit = changes.getPercentageProfit();
        this.remarks = changes.getRemarks();
        this.riskRewardRatio = changes.getRiskRewardRatio();
        this.lotSizeChange = changes.getLotSizeChange();
        this.stopLossChange = changes.getStopLossChange();
        this.takeProfitChange = changes.getTakeProfitChange();
    }

}
