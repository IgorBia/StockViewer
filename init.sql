\set ON_ERROR_STOP on
CREATE SCHEMA IF NOT EXISTS user_management AUTHORIZATION "user";
CREATE SCHEMA IF NOT EXISTS stock_data AUTHORIZATION "user";

SET search_path TO stock_data, user_management;

CREATE TABLE user_management.app_user (
    user_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_management.role (
    role_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO user_management.role (name)
VALUES
    ('ROLE_ADMIN'),
    ('ROLE_USER');

CREATE TABLE user_management.user_role (
    user_id INT REFERENCES user_management.app_user(user_id) ON DELETE CASCADE,
    role_id INT REFERENCES user_management.role(role_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE user_management.wallet (
    wallet_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE
);

CREATE TABLE stock_data.pair (
    pair_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    symbol VARCHAR(10) NOT NULL,
    base VARCHAR(5) NOT NULL,
    quote VARCHAR(5),
    market VARCHAR(10) NOT NULL,
    exchange VARCHAR(15) NOT NULL
);

INSERT INTO stock_data.pair (symbol, base, quote, market, exchange)
VALUES
    ('BTCUSDC', 'BTC', 'USDC', 'Spot', 'Binance'),
    ( 'ETHUSDC', 'ETH', 'USDC', 'Spot', 'Binance'),
    ( 'ETHBTC', 'ETH', 'BTC', 'Spot', 'Binance'),
    ( 'SOLUSDC', 'SOL', 'USDC', 'Spot', 'Binance');


CREATE TABLE user_management.owned_asset (
    owned_asset_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    wallet_id INT NOT NULL,
    pair_id INT NOT NULL,
    amount DECIMAL(18,8) NOT NULL,
    FOREIGN KEY (wallet_id) REFERENCES wallet(wallet_id) ON DELETE CASCADE,
    FOREIGN KEY (pair_id) REFERENCES pair(pair_id) ON DELETE CASCADE
);

CREATE TABLE user_management.trade (
    trade_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INT NOT NULL,
    pair_id INT NOT NULL,
    time TIMESTAMP NOT NULL,
    price DECIMAL(18,8) NOT NULL,
    amount DECIMAL(18,8) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (pair_id) REFERENCES pair(pair_id) ON DELETE CASCADE
);

CREATE TABLE stock_data.candle (
    candle_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    pair_id INT NOT NULL,
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
    FOREIGN KEY (pair_id) REFERENCES pair(pair_id) ON DELETE CASCADE
);

CREATE TABLE user_management.watchlist (
    watchlist_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE
);

CREATE TABLE user_management.watchlist_item (
    watchlist_item_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    watchlist_id INT NOT NULL,
    pair_id INT NOT NULL,
    FOREIGN KEY (watchlist_id) REFERENCES watchlist(watchlist_id) ON DELETE CASCADE,
    FOREIGN KEY (pair_id) REFERENCES pair(pair_id) ON DELETE CASCADE
);