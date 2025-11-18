\set ON_ERROR_STOP on
CREATE SCHEMA IF NOT EXISTS user_management AUTHORIZATION "user";
CREATE SCHEMA IF NOT EXISTS stock_data AUTHORIZATION "user";

SET search_path TO stock_data, user_management;

CREATE TABLE user_management.app_user (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_management.role (
    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO user_management.role (name)
VALUES
    ('ROLE_ADMIN'),
    ('ROLE_USER');

CREATE TABLE user_management.user_role (
    user_id UUID REFERENCES user_management.app_user(user_id) ON DELETE CASCADE,
    role_id UUID REFERENCES user_management.role(role_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);


CREATE TABLE stock_data.asset (
  asset_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  symbol VARCHAR(32) NOT NULL UNIQUE,
  display_name VARCHAR(128) NOT NULL
);

INSERT INTO stock_data.asset (symbol, display_name)
VALUES
    ('BTC', 'Bitcoin'),
    ('ETH', 'Ethereum'),
    ('USDC', 'USD Coin'),
    ('SOL', 'Solana');

CREATE TABLE user_management.wallet (
    wallet_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    managed_asset_id UUID DEFAULT NULL,
    FOREIGN KEY (managed_asset_id) REFERENCES stock_data.asset(asset_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE
);

CREATE TABLE user_management.owned_asset (
    owned_asset_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    amount DECIMAL(18,8) NOT NULL,
    FOREIGN KEY (wallet_id) REFERENCES wallet(wallet_id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
);

CREATE TABLE stock_data.pair (
    pair_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) NOT NULL,
    base_asset_id UUID NOT NULL,
    quote_asset_id UUID NOT NULL,
    market VARCHAR(10) NOT NULL,
    exchange VARCHAR(15) NOT NULL,
    FOREIGN KEY (base_asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE,
    FOREIGN KEY (quote_asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
);

INSERT INTO stock_data.pair (symbol, base_asset_id, quote_asset_id, market, exchange)
VALUES
    ('BTCUSDC', (SELECT asset_id FROM asset WHERE symbol = 'BTC'), (SELECT asset_id FROM asset WHERE symbol = 'USDC'), 'Spot', 'Binance'),
    ('ETHUSDC', (SELECT asset_id FROM asset WHERE symbol = 'ETH'), (SELECT asset_id FROM asset WHERE symbol = 'USDC'), 'Spot', 'Binance'),
    ('ETHBTC', (SELECT asset_id FROM asset WHERE symbol = 'ETH'), (SELECT asset_id FROM asset WHERE symbol = 'BTC'), 'Spot', 'Binance'),
    ('SOLUSDC', (SELECT asset_id FROM asset WHERE symbol = 'SOL'), (SELECT asset_id FROM asset WHERE symbol = 'USDC'), 'Spot', 'Binance');


CREATE TABLE user_management.trade (
    trade_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    pair_id UUID NOT NULL,
    time TIMESTAMP NOT NULL,
    price DECIMAL(18,8) NOT NULL,
    amount DECIMAL(18,8) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (pair_id) REFERENCES pair(pair_id) ON DELETE CASCADE
);

CREATE TABLE stock_data.candle (
    candle_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pair_id UUID NOT NULL,
    open_time TIMESTAMP NOT NULL,
    open DECIMAL(18,8) NOT NULL,
    high DECIMAL(18,8) NOT NULL,
    low DECIMAL(18,8) NOT NULL,
    close DECIMAL(18,8) NOT NULL,
    volume DECIMAL(18,8) NOT NULL,
    close_time TIMESTAMP NOT NULL,
    quote_volume DECIMAL(18,8) NOT NULL,
    trades INT NOT NULL,
    taker_base_vol DECIMAL(18,8) NOT NULL,
    taker_quote_vol DECIMAL(18,8) NOT NULL,
    timeframe VARCHAR(10) NOT NULL,
    CONSTRAINT uniq_candle UNIQUE (pair_id, timeframe, close_time),
    FOREIGN KEY (pair_id) REFERENCES pair(pair_id) ON DELETE CASCADE
);

CREATE TABLE stock_data.indicator (
    candle_id UUID REFERENCES candle(candle_id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    value NUMERIC,
    ts TIMESTAMP NOT NULL,
    PRIMARY KEY (candle_id, name)
);

CREATE TABLE user_management.watchlist (
    watchlist_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE
);

CREATE TABLE user_management.watchlist_item (
    watchlist_id UUID NOT NULL REFERENCES watchlist(watchlist_id) ON DELETE CASCADE,
    pair_id UUID NOT NULL REFERENCES pair(pair_id) ON DELETE CASCADE,
    PRIMARY KEY (watchlist_id, pair_id)
);