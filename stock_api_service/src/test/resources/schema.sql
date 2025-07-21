CREATE SCHEMA IF NOT EXISTS user_management;
CREATE SCHEMA IF NOT EXISTS stock_data;

-- Nie ma SET search_path w H2 – pomijamy

CREATE TABLE user_management.app_user (
                                          user_id INT PRIMARY KEY AUTO_INCREMENT,
                                          email VARCHAR(50) NOT NULL UNIQUE,
                                          password VARCHAR(60) NOT NULL,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_management.role (
                                      role_id INT PRIMARY KEY AUTO_INCREMENT,
                                      name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO user_management.role (name)
VALUES
    ('ROLE_ADMIN'),
    ('ROLE_USER');

CREATE TABLE user_management.user_role (
                                           user_id INT,
                                           role_id INT,
                                           PRIMARY KEY (user_id, role_id),
                                           FOREIGN KEY (user_id) REFERENCES user_management.app_user(user_id) ON DELETE CASCADE,
                                           FOREIGN KEY (role_id) REFERENCES user_management.role(role_id) ON DELETE CASCADE
);

CREATE TABLE user_management.wallet (
                                        wallet_id INT PRIMARY KEY AUTO_INCREMENT,
                                        user_id INT NOT NULL,
                                        FOREIGN KEY (user_id) REFERENCES user_management.app_user(user_id) ON DELETE CASCADE
);

CREATE TABLE stock_data.pair (
                                 pair_id INT PRIMARY KEY AUTO_INCREMENT,
                                 symbol VARCHAR(10) NOT NULL,
                                 base VARCHAR(5) NOT NULL,
                                 quote VARCHAR(5),
                                 market VARCHAR(10) NOT NULL,
                                 exchange VARCHAR(15) NOT NULL
);

INSERT INTO stock_data.pair (symbol, base, quote, market, exchange)
VALUES
    ('BTCUSDC', 'BTC', 'USDC', 'Spot', 'Binance'),
    ('ETHUSDC', 'ETH', 'USDC', 'Spot', 'Binance'),
    ('ETHBTC', 'ETH', 'BTC', 'Spot', 'Binance'),
    ('SOLUSDC', 'SOL', 'USDC', 'Spot', 'Binance');

CREATE TABLE user_management.owned_asset (
                                             owned_asset_id INT PRIMARY KEY AUTO_INCREMENT,
                                             wallet_id INT NOT NULL,
                                             pair_id INT NOT NULL,
                                             amount DECIMAL(18,8) NOT NULL,
                                             FOREIGN KEY (wallet_id) REFERENCES user_management.wallet(wallet_id) ON DELETE CASCADE,
                                             FOREIGN KEY (pair_id) REFERENCES stock_data.pair(pair_id) ON DELETE CASCADE
);

CREATE TABLE user_management.trade (
                                       trade_id INT PRIMARY KEY AUTO_INCREMENT,
                                       user_id INT NOT NULL,
                                       pair_id INT NOT NULL,
                                       time TIMESTAMP NOT NULL,
                                       price DECIMAL(18,8) NOT NULL,
                                       amount DECIMAL(18,8) NOT NULL,
                                       FOREIGN KEY (user_id) REFERENCES user_management.app_user(user_id) ON DELETE CASCADE,
                                       FOREIGN KEY (pair_id) REFERENCES stock_data.pair(pair_id) ON DELETE CASCADE
);

CREATE TABLE stock_data.candle (
                                   candle_id INT PRIMARY KEY AUTO_INCREMENT,
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
                                   FOREIGN KEY (pair_id) REFERENCES stock_data.pair(pair_id) ON DELETE CASCADE
);

CREATE TABLE user_management.watchlist (
                                           watchlist_id INT PRIMARY KEY AUTO_INCREMENT,
                                           user_id INT NOT NULL,
                                           FOREIGN KEY (user_id) REFERENCES user_management.app_user(user_id) ON DELETE CASCADE
);

CREATE TABLE user_management.watchlist_item (
                                                watchlist_item_id INT PRIMARY KEY AUTO_INCREMENT,
                                                watchlist_id INT NOT NULL,
                                                pair_id INT NOT NULL,
                                                FOREIGN KEY (watchlist_id) REFERENCES user_management.watchlist(watchlist_id) ON DELETE CASCADE,
                                                FOREIGN KEY (pair_id) REFERENCES stock_data.pair(pair_id) ON DELETE CASCADE
);
