package com.deriska.psydtrader.service;

import com.deriska.psydtrader.entity.Account;
import com.deriska.psydtrader.entity.Pojo.RunningTradeRequest;
import com.deriska.psydtrader.entity.Pojo.TradeRecordResponse;
import com.deriska.psydtrader.entity.Pojo.TradeRecordUpdate;
import com.deriska.psydtrader.entity.StandardResponse;
import com.deriska.psydtrader.entity.TradeChanges;
import com.deriska.psydtrader.entity.Trades;
import com.deriska.psydtrader.repository.TradeChangesRepo;
import com.deriska.psydtrader.repository.TradeJournalRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class TradeJournalService {

    @Autowired
    private TradeJournalRepo journalRepo;
    @Autowired
    private TradeChangesRepo changesRepo;
    @Autowired
    private DerivService derivService;


    public ResponseEntity<StandardResponse> addTradeToJournal(Trades trade) {
        return null;
    }

    public ResponseEntity<StandardResponse> getAllRecordsFromJournal(Long journalId) {
        return null;
    }

    public ResponseEntity<StandardResponse> getRecordByTradeId(Long tradeId) {
        try {
            Trades tradeTaken = journalRepo.findById(tradeId).get();
            TradeRecordResponse response = new TradeRecordResponse();
            List<TradeChanges> tradeChanges;
            if(tradeTaken.isWasChanged()){
                tradeChanges = changesRepo.findByTradeId(tradeId);
                response.setTradeRecord(tradeTaken);
                response.setChangesList(tradeChanges);
                return StandardResponse.sendHttpResponse(true, "Successful", response);
            }else{
                return StandardResponse.sendHttpResponse(true, "Successful", tradeTaken);
            }

        } catch (Exception e) {
            return StandardResponse.sendHttpResponse(false, "Operation Failed");
        }
    }

    public ResponseEntity<StandardResponse> updateRecord(TradeRecordUpdate update, Long tradeId){
        return null;
    }

    public void updateRecordAfterClosedTrade(RunningTradeRequest request, Trades trades, TradeChanges tradeChanges) {
        try {
            trades.setExitDate(request.getExitDate());
//            trades.setExitDate(LocalDateTime.now());
            trades.setProfit(request.isEndedInProfit());
            Account account = derivService.getAccountStatus(trades.getAccountId());
            trades.setClosingBalance(account.getAccountBalance());
            trades.setTradeScore(tradeChanges.getTradeScore());

            journalRepo.save(trades);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
