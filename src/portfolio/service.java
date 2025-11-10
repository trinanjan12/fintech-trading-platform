package com.fintech.portfolio;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

/**
 * Portfolio service for managing trading portfolios
 * Handles portfolio operations, position tracking, and performance calculations
 */
public class PortfolioService {
    
    private final Map<String, Portfolio> portfolios = new ConcurrentHashMap<>();
    private final DatabaseConnection dbConnection;
    
    public PortfolioService(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }
    
    /**
     * Retrieve portfolio by ID with current positions
     */
    public Portfolio getPortfolio(String portfolioId) {
        try {
            Portfolio portfolio = portfolios.get(portfolioId);
            if (portfolio == null) {
                portfolio = loadPortfolioFromDatabase(portfolioId);
                portfolios.put(portfolioId, portfolio);
            }
            return portfolio;
        } catch (Exception e) {
            throw new PortfolioException("Failed to retrieve portfolio: " + portfolioId, e);
        }
    }
    
    /**
     * Calculate portfolio performance metrics
     */
    public PerformanceMetrics calculatePerformance(String portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        
        double totalValue = portfolio.getPositions().stream()
            .mapToDouble(Position::getCurrentValue)
            .sum();
            
        double totalCost = portfolio.getPositions().stream()
            .mapToDouble(Position::getCostBasis)
            .sum();
            
        double pnl = totalValue - totalCost;
        double pnlPercentage = (pnl / totalCost) * 100;
        
        return new PerformanceMetrics(
            portfolioId,
            totalValue,
            totalCost,
            pnl,
            pnlPercentage,
            LocalDateTime.now()
        );
    }
    
    private Portfolio loadPortfolioFromDatabase(String portfolioId) {
        // Database loading logic
        return dbConnection.loadPortfolio(portfolioId);
    }
}