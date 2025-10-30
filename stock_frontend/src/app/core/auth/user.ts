import { Watchlist } from "../../features/watchlist/watchlist";

export interface User{
    email: string;
    watchlists: Watchlist[];
}