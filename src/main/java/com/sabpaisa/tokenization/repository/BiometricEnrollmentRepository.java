package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.entity.BiometricEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for biometric enrollment data
 */
@Repository
public interface BiometricEnrollmentRepository extends JpaRepository<BiometricEnrollment, Long> {
    
    /**
     * Find enrollment by enrollment ID
     */
    Optional<BiometricEnrollment> findByEnrollmentId(String enrollmentId);
    
    /**
     * Find enrollments by user ID
     */
    List<BiometricEnrollment> findByUserId(String userId);
    
    /**
     * Find enrollments by merchant ID
     */
    List<BiometricEnrollment> findByMerchantId(String merchantId);
    
    /**
     * Find active enrollments for a user
     */
    @Query("SELECT e FROM BiometricEnrollment e WHERE e.userId = :userId AND e.status = 'ACTIVE'")
    List<BiometricEnrollment> findActiveEnrollmentsByUserId(@Param("userId") String userId);
    
    /**
     * Find enrollments by modality
     */
    @Query("SELECT e FROM BiometricEnrollment e WHERE :modality MEMBER OF e.enabledModalities")
    List<BiometricEnrollment> findByModality(@Param("modality") String modality);
    
    /**
     * Find enrollments created after a specific date
     */
    List<BiometricEnrollment> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find enrollments with high quality scores
     */
    @Query("SELECT e FROM BiometricEnrollment e WHERE e.overallQualityScore >= :minScore")
    List<BiometricEnrollment> findHighQualityEnrollments(@Param("minScore") double minScore);
    
    /**
     * Count enrollments by merchant
     */
    long countByMerchantId(String merchantId);
    
    /**
     * Check if enrollment exists for user and merchant
     */
    boolean existsByUserIdAndMerchantId(String userId, String merchantId);
    
    /**
     * Find enrollments requiring key rotation
     */
    @Query("SELECT e FROM BiometricEnrollment e WHERE e.lastKeyRotation < :cutoffDate OR e.lastKeyRotation IS NULL")
    List<BiometricEnrollment> findEnrollmentsRequiringKeyRotation(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Update enrollment status
     */
    @Query("UPDATE BiometricEnrollment e SET e.status = :status WHERE e.enrollmentId = :enrollmentId")
    void updateEnrollmentStatus(@Param("enrollmentId") String enrollmentId, @Param("status") String status);
}