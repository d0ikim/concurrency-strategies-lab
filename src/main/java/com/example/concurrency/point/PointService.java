package com.example.concurrency.point;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service    // "이 클래스는 비즈니스로직 담당자야" 라는 스티커
public class PointService {

    private final PointRepository pointRepository;

    public PointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    @Transactional  // DB작업을 하나의 묶음으로 처리
    public Point charge(Long userId, long amount) {
        Point point = pointRepository.findById(userId).orElseThrow();

        // 1. 읽기
        long currentBalance = point.getBalance();

        // 로직 수행 시간 시뮬레이션 (동시성 이슈 유발)
        try { 
            // 2. 틈 발생 
            Thread.sleep(200);
        } catch (InterruptedException e) {}

        // 3. 덮어쓰기 위험!
        point.setBalance(currentBalance + amount);
        return pointRepository.save(point);
    }

    @Transactional(readOnly = true) // 읽기전용(성능최적화)
    public long getBalance(Long userId) {
        return pointRepository.findById(userId).orElseThrow().getBalance();
    }

    @Transactional
    public void initPoint(Long userId, long initialBalance) {   // 테스트 전 초기잔액세팅용
        pointRepository.save(Point.of(userId, initialBalance));
    }
}