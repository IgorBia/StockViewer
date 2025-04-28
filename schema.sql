CREATE TABLE users (
        user_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        email VARCHAR(50) NOT NULL UNIQUE,
        password VARCHAR(60) NOT NULL
);

CREATE TABLE wallets (
        wallet_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        user_id INT NOT NULL,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE pairs (
        pair_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        symbol VARCHAR(10) NOT NULL,git
        base VARCHAR(5) NOT NULL,
        quote VARCHAR(5),
        market VARCHAR(10) NOT NULL,
        exchange VARCHAR(15) NOT NULL
);

CREATE TABLE owned_assets (
        owned_asset_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        wallet_id INT NOT NULL,
        pair_id INT NOT NULL,
        amount DECIMAL(18,8) NOT NULL,
        FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
        FOREIGN KEY (pair_id) REFERENCES pairs(pair_id) ON DELETE CASCADE
);

CREATE TABLE trades (
        trade_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        user_id INT NOT NULL,
        pair_id INT NOT NULL,
        time TIMESTAMP NOT NULL,
        price DECIMAL(18,8) NOT NULL,
        amount DECIMAL(18,8) NOT NULL,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
        FOREIGN KEY (pair_id) REFERENCES pairs(pair_id) ON DELETE CASCADE
);

CREATE TABLE candles (
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
        FOREIGN KEY (pair_id) REFERENCES pairs(pair_id) ON DELETE CASCADE
);

CREATE TABLE watchlists (
        watchlist_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        user_id INT NOT NULL,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE watchlist_items (
        watchlist_item_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        watchlist_id INT NOT NULL,
        pair_id INT NOT NULL,
        FOREIGN KEY (watchlist_id) REFERENCES watchlists(watchlist_id) ON DELETE CASCADE,
        FOREIGN KEY (pair_id) REFERENCES pairs(pair_id) ON DELETE CASCADE
);
