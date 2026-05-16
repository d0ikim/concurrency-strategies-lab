package com.example.concurrency.point;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PointRepository extends JpaRepository<Point, Long> {
    // JpaRepository<Point, Long>를 상속받으면
    // findById, save 등 기본DB기능이 자동으로 생김
    // Point = 다룰 엔티티, Long = 기본키 타입

    @Lock(LockModeType.PESSIMISTIC_WRITE)   // SELECT FOR UPDATE(3. 비관적 락)
    @Query("SELECT p FROM Point p WHERE p.id = :id")
    java.util.Optional<Point> findByIdWithLock(Long id);    // 전략3번 비관적 락에서 사용
}