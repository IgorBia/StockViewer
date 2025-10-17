package com.stockviewer.stockapi.candle.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
public class IndicatorId implements Serializable {

    @JsonIgnore
    private UUID candleId;
    private String name;

    public IndicatorId() {}

    public IndicatorId(UUID candleId, String name) {
        this.candleId = candleId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndicatorId)) return false;
        IndicatorId that = (IndicatorId) o;
        return Objects.equals(candleId, that.candleId) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candleId, name);
    }
}

