package com.stockviewer.stockapi.indicators;

import com.stockviewer.stockapi.candle.entity.Indicator;
import com.stockviewer.stockapi.candle.entity.IndicatorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, IndicatorId> {
}
