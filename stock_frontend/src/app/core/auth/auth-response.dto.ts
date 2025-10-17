import { User } from "./user";

export interface AuthResponseDTO {
    message: string;
    tokenType: string;
    accessToken: string;
    userDetails: User;
}