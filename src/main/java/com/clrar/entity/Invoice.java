package com.clrar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String invoiceNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    
    @Column(nullable = false)
    private Double invoiceAmount;
    
    @Column(nullable = false)
    private Double outstandingAmount;
    
    @Column(nullable = false)
    private LocalDate invoiceDate;
    
    @Column(nullable = false)
    private LocalDate dueDate;
    
    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "ar_bucket", nullable = false)
    @Enumerated(EnumType.STRING)
    private ARBucket arBucket;
    
    @Column(name = "days_overdue")
    private Integer daysOverdue = 0;
    
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateBucket();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateBucket();
    }
    
    public void calculateBucket() {
        LocalDate today = LocalDate.now();
        long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(invoiceDate, today);
        
        if (daysDifference <= 30) {
            this.arBucket = ARBucket.BUCKET_0_30;
        } else if (daysDifference <= 60) {
            this.arBucket = ARBucket.BUCKET_31_60;
        } else if (daysDifference <= 90) {
            this.arBucket = ARBucket.BUCKET_61_90;
        } else if (daysDifference <= 120) {
            this.arBucket = ARBucket.BUCKET_91_120;
        } else if (daysDifference <= 180) {
            this.arBucket = ARBucket.BUCKET_121_180;
        } else {
            this.arBucket = ARBucket.BUCKET_181_PLUS;
        }
        
        if (today.isAfter(dueDate)) {
            this.daysOverdue = (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
            if (paymentStatus != PaymentStatus.PAID && paymentStatus != PaymentStatus.CANCELLED) {
                this.paymentStatus = PaymentStatus.OVERDUE;
            }
        }
    }
}