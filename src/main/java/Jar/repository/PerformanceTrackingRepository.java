package Jar.repository;

import Jar.entity.PerformanceTracking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceTrackingRepository
        extends JpaRepository<PerformanceTracking, Long> {
}