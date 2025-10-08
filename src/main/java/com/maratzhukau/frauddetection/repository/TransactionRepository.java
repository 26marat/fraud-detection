package com.maratzhukau.frauddetection.repository;

import com.maratzhukau.frauddetection.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find transactions for an account within a time range
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp BETWEEN :startTime AND :endTime ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountIdAndTimestampBetween(@Param("accountId") String accountId, 
                                                        @Param("startTime") LocalDateTime startTime, 
                                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * Count transactions for an account within a time range
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp BETWEEN :startTime AND :endTime")
    long countByAccountIdAndTimestampBetween(@Param("accountId") String accountId, 
                                           @Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find transactions with amount greater than specified value for an account
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.amount > :amount ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountIdAndAmountGreaterThan(@Param("accountId") String accountId, 
                                                         @Param("amount") BigDecimal amount);
    
    /**
     * Find recent transactions for an account from different locations
     */
    @Query("SELECT DISTINCT t.location FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp >= :since AND t.location IS NOT NULL")
    List<String> findDistinctLocationsByAccountIdAndTimestampAfter(@Param("accountId") String accountId, 
                                                                  @Param("since") LocalDateTime since);
}
