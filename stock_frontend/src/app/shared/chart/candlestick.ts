// Minimal local BigDecimal type to avoid the missing-module compile error.
// Replace with a proper BigDecimal implementation or restore ../big-decimal module as needed.
export type BigDecimal = string;
export type LocalDateTime = string;

export interface Candlestick {
    openTime: LocalDateTime,
    timestamp: LocalDateTime,
    closeTime: LocalDateTime,
    open: BigDecimal,
    close: BigDecimal,
    high: BigDecimal,
    low: BigDecimal,
    volume: BigDecimal,
    indicators: IndicatorDTO[]
}

export interface IndicatorDTO {
    name: string,
    value: BigDecimal
}
