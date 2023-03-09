package com.deriska.psydtrader.entity.Pojo;

import com.deriska.psydtrader.entity.TradeRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@Data
public class RunningTradeRequest extends TradeRequest {

    private String reqId;
    private Long accountId;
    private double stopLossPrice;
    private double entryPrice;
    private String tradeType;
    private double takeProfitPrice;
    private boolean isActive;
    private double currentPrice;
    private double stopLostPips;
    private double lotSize;
    private LocalDateTime entryDate;
    private LocalDateTime exitDate;
    private double exitPrice;
    private String asset;
    private boolean isInProfit;
    private double accountBalance;
    private boolean endedInProfit;
}
