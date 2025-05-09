
#### Functional Requirements

- **User Authentication**
    
    - Users must be able to register with an email and password.
        
    - Users must be able to log in using their credentials.
        
    - Users must be able to recover their password via email if forgotten.
        
    - Users must be able to log out from the application.
        
- **Stock Data Display**
    
    - Users must be able to view historical stock data for selected instruments.
        
- **Mock Trading**

	- Users should be able to simulate buy/sell operations.
	
    - Users must have a virtual wallet balance upon registration.
        
    - Users must be able to view their current assets (mock positions).
        
    - Users must see the interest rate since their registration date.
        
    - Users must be able to view a complete history of their mock trades.
        
- **Stock Search**
    
    - Users must be able to search for stocks using a search bar.
        
- **Watchlist Management**
    
    - Users must be able to add stocks to a personal watchlist.
        
    - Users must be able to remove stocks from their watchlist.

### Non-functional Requirements

- **Performance**
    
    - **Response time**: The homepage should load within ≤ 2 seconds with 100 concurrent users.
        
    - **Throughput**: The system must handle at least 100 API requests per second.
        
- **Availability**
    
    - **Uptime**: The platform should be available 99.8% of the time.
        
- **Scalability**
    
    - **Data growth**: The database should handle an increase of up to 250MB of data per year.
        
- **Security**
    
    - **Authorization and authentication**: JWT tokens should be valid for 20 minutes.
        
    - **Data encryption**: Passwords must be hashed using bcrypt with a minimum cost factor of 10.
        
- **Usability**
    
    - A guest user should be able to find the registration page within a maximum of 2 clicks.