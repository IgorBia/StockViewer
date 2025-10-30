import { WatchlistItem } from "./watchlist-item";

export interface Watchlist {
    id: string;
    name: string;
    watchlistItems: WatchlistItem[];
}