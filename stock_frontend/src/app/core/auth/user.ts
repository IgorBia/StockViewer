import { Watchlist } from "./watchlist";

export interface User{
    email: string;
    watchlists: Watchlist[];
}