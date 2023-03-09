package com.deriska.psydtrader.repository;

import com.deriska.psydtrader.entity.RiskManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Repository
public interface RiskManagementRepo extends JpaRepository<RiskManagement, Long> {
    Optional<RiskManagement> findByTradeId(Long id);

    Optional<RiskManagement> findByTradingPlanId(Long planId);


    Optional<RiskManagement> findByTradingPlanIdAndRiskCount(Long planId, int count);

    Optional<RiskManagement> findByAccountId(Long accountId);
}
