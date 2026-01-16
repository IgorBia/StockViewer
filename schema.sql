--
-- PostgreSQL database dump
--

\restrict K2fVChISyK7SyqWexuXS7ZWfGcaSDTYmnuZZjU1S3rH5kMAJv35rDfKf9K7Ro7s

-- Dumped from database version 15.15 (Debian 15.15-1.pgdg13+1)
-- Dumped by pg_dump version 15.15 (Debian 15.15-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: stock_data; Type: SCHEMA; Schema: -; Owner: user
--

CREATE SCHEMA stock_data;


ALTER SCHEMA stock_data OWNER TO "user";

--
-- Name: user_management; Type: SCHEMA; Schema: -; Owner: user
--

CREATE SCHEMA user_management;


ALTER SCHEMA user_management OWNER TO "user";

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: asset; Type: TABLE; Schema: stock_data; Owner: user
--

CREATE TABLE stock_data.asset (
    asset_id uuid DEFAULT gen_random_uuid() NOT NULL,
    symbol character varying(32) NOT NULL,
    display_name character varying(128) NOT NULL
);


ALTER TABLE stock_data.asset OWNER TO "user";

--
-- Name: candle; Type: TABLE; Schema: stock_data; Owner: user
--

CREATE TABLE stock_data.candle (
    candle_id uuid DEFAULT gen_random_uuid() NOT NULL,
    pair_id uuid NOT NULL,
    open_time timestamp without time zone NOT NULL,
    open numeric(18,8) NOT NULL,
    high numeric(18,8) NOT NULL,
    low numeric(18,8) NOT NULL,
    close numeric(18,8) NOT NULL,
    volume numeric(18,8) NOT NULL,
    close_time timestamp without time zone NOT NULL,
    quote_volume numeric(18,8) NOT NULL,
    trades integer NOT NULL,
    taker_base_vol numeric(18,8) NOT NULL,
    taker_quote_vol numeric(18,8) NOT NULL,
    timeframe character varying(3) NOT NULL
);


ALTER TABLE stock_data.candle OWNER TO "user";

--
-- Name: indicator; Type: TABLE; Schema: stock_data; Owner: user
--

CREATE TABLE stock_data.indicator (
    candle_id uuid NOT NULL,
    name text NOT NULL,
    value numeric,
    ts timestamp without time zone NOT NULL
);


ALTER TABLE stock_data.indicator OWNER TO "user";

--
-- Name: pair; Type: TABLE; Schema: stock_data; Owner: user
--

CREATE TABLE stock_data.pair (
    pair_id uuid DEFAULT gen_random_uuid() NOT NULL,
    symbol character varying(20) NOT NULL,
    base_asset_id uuid NOT NULL,
    quote_asset_id uuid NOT NULL,
    market character varying(10) NOT NULL,
    exchange character varying(15) NOT NULL,
    risk_tolerance integer NOT NULL
);


ALTER TABLE stock_data.pair OWNER TO "user";

--
-- Name: app_user; Type: TABLE; Schema: user_management; Owner: user
--

CREATE TABLE user_management.app_user (
    user_id uuid DEFAULT gen_random_uuid() NOT NULL,
    email character varying(50) NOT NULL,
    password character varying(60) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE user_management.app_user OWNER TO "user";

--
-- Name: owned_asset; Type: TABLE; Schema: user_management; Owner: user
--

CREATE TABLE user_management.owned_asset (
    owned_asset_id uuid DEFAULT gen_random_uuid() NOT NULL,
    wallet_id uuid NOT NULL,
    asset_id uuid NOT NULL,
    amount numeric(18,8) NOT NULL,
    avg_price numeric(18,8)
);


ALTER TABLE user_management.owned_asset OWNER TO "user";

--
-- Name: role; Type: TABLE; Schema: user_management; Owner: user
--

CREATE TABLE user_management.role (
    role_id uuid DEFAULT gen_random_uuid() NOT NULL,
    name character varying(50) NOT NULL
);


ALTER TABLE user_management.role OWNER TO "user";

--
-- Name: trade; Type: TABLE; Schema: user_management; Owner: user
--

CREATE TABLE user_management.trade (
    trade_id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    pair_id uuid NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    price numeric(18,8) NOT NULL,
    transaction_type character varying(10) NOT NULL,
    base_amount numeric(18,8) NOT NULL,
    quote_amount numeric(18,8) NOT NULL,
    stop_loss numeric(18,8),
    take_profit numeric(18,8),
    pnl numeric(18,8)
);


ALTER TABLE user_management.trade OWNER TO "user";

--
-- Name: user_role; Type: TABLE; Schema: user_management; Owner: user
--

CREATE TABLE user_management.user_role (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);


ALTER TABLE user_management.user_role OWNER TO "user";

--
-- Name: wallet; Type: TABLE; Schema: user_management; Owner: user
--

CREATE TABLE user_management.wallet (
    wallet_id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    managed_asset_id uuid
);


ALTER TABLE user_management.wallet OWNER TO "user";

--
-- Name: wallet_worth_snapshot; Type: TABLE; Schema: user_management; Owner: user
--

CREATE TABLE user_management.wallet_worth_snapshot (
    snapshot_id uuid DEFAULT gen_random_uuid() NOT NULL,
    wallet_id uuid NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    total_worth_usd numeric(18,8) NOT NULL
);


ALTER TABLE user_management.wallet_worth_snapshot OWNER TO "user";

--
-- Name: watchlist; Type: TABLE; Schema: user_management; Owner: user
--

CREATE TABLE user_management.watchlist (
    watchlist_id uuid DEFAULT gen_random_uuid() NOT NULL,
    name text NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE user_management.watchlist OWNER TO "user";

--
-- Name: watchlist_item; Type: TABLE; Schema: user_management; Owner: user
--

CREATE TABLE user_management.watchlist_item (
    watchlist_id uuid NOT NULL,
    pair_id uuid NOT NULL
);


ALTER TABLE user_management.watchlist_item OWNER TO "user";

--
-- Name: asset asset_pkey; Type: CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.asset
    ADD CONSTRAINT asset_pkey PRIMARY KEY (asset_id);


--
-- Name: asset asset_symbol_key; Type: CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.asset
    ADD CONSTRAINT asset_symbol_key UNIQUE (symbol);


--
-- Name: candle candle_pkey; Type: CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.candle
    ADD CONSTRAINT candle_pkey PRIMARY KEY (candle_id);


--
-- Name: indicator indicator_pkey; Type: CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.indicator
    ADD CONSTRAINT indicator_pkey PRIMARY KEY (candle_id, name);


--
-- Name: pair pair_pkey; Type: CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.pair
    ADD CONSTRAINT pair_pkey PRIMARY KEY (pair_id);


--
-- Name: candle uniq_candle; Type: CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.candle
    ADD CONSTRAINT uniq_candle UNIQUE (pair_id, timeframe, close_time);


--
-- Name: app_user app_user_email_key; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.app_user
    ADD CONSTRAINT app_user_email_key UNIQUE (email);


--
-- Name: app_user app_user_pkey; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.app_user
    ADD CONSTRAINT app_user_pkey PRIMARY KEY (user_id);


--
-- Name: owned_asset owned_asset_pkey; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.owned_asset
    ADD CONSTRAINT owned_asset_pkey PRIMARY KEY (owned_asset_id);


--
-- Name: role role_name_key; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.role
    ADD CONSTRAINT role_name_key UNIQUE (name);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (role_id);


--
-- Name: trade trade_pkey; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.trade
    ADD CONSTRAINT trade_pkey PRIMARY KEY (trade_id);


--
-- Name: user_role user_role_pkey; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: wallet wallet_pkey; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.wallet
    ADD CONSTRAINT wallet_pkey PRIMARY KEY (wallet_id);


--
-- Name: wallet_worth_snapshot wallet_worth_snapshot_pkey; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.wallet_worth_snapshot
    ADD CONSTRAINT wallet_worth_snapshot_pkey PRIMARY KEY (snapshot_id);


--
-- Name: watchlist_item watchlist_item_pkey; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.watchlist_item
    ADD CONSTRAINT watchlist_item_pkey PRIMARY KEY (watchlist_id, pair_id);


--
-- Name: watchlist watchlist_pkey; Type: CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.watchlist
    ADD CONSTRAINT watchlist_pkey PRIMARY KEY (watchlist_id);


--
-- Name: candle candle_pair_id_fkey; Type: FK CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.candle
    ADD CONSTRAINT candle_pair_id_fkey FOREIGN KEY (pair_id) REFERENCES stock_data.pair(pair_id) ON DELETE CASCADE;


--
-- Name: indicator indicator_candle_id_fkey; Type: FK CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.indicator
    ADD CONSTRAINT indicator_candle_id_fkey FOREIGN KEY (candle_id) REFERENCES stock_data.candle(candle_id) ON DELETE CASCADE;


--
-- Name: pair pair_base_asset_id_fkey; Type: FK CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.pair
    ADD CONSTRAINT pair_base_asset_id_fkey FOREIGN KEY (base_asset_id) REFERENCES stock_data.asset(asset_id) ON DELETE CASCADE;


--
-- Name: pair pair_quote_asset_id_fkey; Type: FK CONSTRAINT; Schema: stock_data; Owner: user
--

ALTER TABLE ONLY stock_data.pair
    ADD CONSTRAINT pair_quote_asset_id_fkey FOREIGN KEY (quote_asset_id) REFERENCES stock_data.asset(asset_id) ON DELETE CASCADE;


--
-- Name: owned_asset owned_asset_asset_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.owned_asset
    ADD CONSTRAINT owned_asset_asset_id_fkey FOREIGN KEY (asset_id) REFERENCES stock_data.asset(asset_id) ON DELETE CASCADE;


--
-- Name: owned_asset owned_asset_wallet_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.owned_asset
    ADD CONSTRAINT owned_asset_wallet_id_fkey FOREIGN KEY (wallet_id) REFERENCES user_management.wallet(wallet_id) ON DELETE CASCADE;


--
-- Name: trade trade_pair_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.trade
    ADD CONSTRAINT trade_pair_id_fkey FOREIGN KEY (pair_id) REFERENCES stock_data.pair(pair_id) ON DELETE CASCADE;


--
-- Name: trade trade_user_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.trade
    ADD CONSTRAINT trade_user_id_fkey FOREIGN KEY (user_id) REFERENCES user_management.app_user(user_id) ON DELETE CASCADE;


--
-- Name: user_role user_role_role_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.user_role
    ADD CONSTRAINT user_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES user_management.role(role_id) ON DELETE CASCADE;


--
-- Name: user_role user_role_user_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.user_role
    ADD CONSTRAINT user_role_user_id_fkey FOREIGN KEY (user_id) REFERENCES user_management.app_user(user_id) ON DELETE CASCADE;


--
-- Name: wallet wallet_managed_asset_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.wallet
    ADD CONSTRAINT wallet_managed_asset_id_fkey FOREIGN KEY (managed_asset_id) REFERENCES stock_data.asset(asset_id) ON DELETE SET NULL;


--
-- Name: wallet wallet_user_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.wallet
    ADD CONSTRAINT wallet_user_id_fkey FOREIGN KEY (user_id) REFERENCES user_management.app_user(user_id) ON DELETE CASCADE;


--
-- Name: wallet_worth_snapshot wallet_worth_snapshot_wallet_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.wallet_worth_snapshot
    ADD CONSTRAINT wallet_worth_snapshot_wallet_id_fkey FOREIGN KEY (wallet_id) REFERENCES user_management.wallet(wallet_id) ON DELETE CASCADE;


--
-- Name: watchlist_item watchlist_item_pair_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.watchlist_item
    ADD CONSTRAINT watchlist_item_pair_id_fkey FOREIGN KEY (pair_id) REFERENCES stock_data.pair(pair_id) ON DELETE CASCADE;


--
-- Name: watchlist_item watchlist_item_watchlist_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.watchlist_item
    ADD CONSTRAINT watchlist_item_watchlist_id_fkey FOREIGN KEY (watchlist_id) REFERENCES user_management.watchlist(watchlist_id) ON DELETE CASCADE;


--
-- Name: watchlist watchlist_user_id_fkey; Type: FK CONSTRAINT; Schema: user_management; Owner: user
--

ALTER TABLE ONLY user_management.watchlist
    ADD CONSTRAINT watchlist_user_id_fkey FOREIGN KEY (user_id) REFERENCES user_management.app_user(user_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict K2fVChISyK7SyqWexuXS7ZWfGcaSDTYmnuZZjU1S3rH5kMAJv35rDfKf9K7Ro7s

