package com.deriska.psydtrader.repository;

import com.deriska.psydtrader.entity.Trades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeJournalRepo extends JpaRepository<Trades, Long> {
    Optional<Trades> findByTradeRequestId(Long requestId);


    Optional<Trades> findByEntryPrice(double entryPrice);
}
