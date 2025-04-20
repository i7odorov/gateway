package com.ivantodorov.gateway.infrastructure.db;

import com.ivantodorov.gateway.infrastructure.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {
    List<CurrencyRate> findByCurrencyAndTimestampAfter(String currency, LocalDateTime fromTime);
    CurrencyRate findTopByCurrencyOrderByTimestampDesc(String currency);
}