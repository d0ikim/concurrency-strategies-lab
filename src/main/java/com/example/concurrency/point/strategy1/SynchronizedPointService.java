// 전략1. synchronized 키워드로 동기화 처리한 서비스 클래스
package com.example.concurrency.point.strategy1;

import com.example.concurrency.point.Point;
import com.example.concurrency.point.PointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SynchronizedPointService {

    private final PointRepository pointRepository;

    public SynchronizedPointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    public synchronized Point charge(Long userId, long amount) {
        // synchronized 한 단어가 "한번에 한스레드만 실행"을 보장해줌
        Point point = pointRepository.findById(userId).orElseThrow();   // findById()는 자체적으로 @Transactional이 붙어있어서 트랜잭션이 걸려있음

        long currentBalance = point.getBalance();

        try { Thread.sleep(200); } catch (InterruptedException e) {}

        point.setBalance(currentBalance + amount);
        return pointRepository.save(point); // save()도 자체적으로 @Transactional이 붙어있어서 트랜잭션이 걸려있음. (트랜잭션 열고 -> 저장 -> 닫음(커밋))
    }

    @Transactional(readOnly = true)
    public long getBalance(Long userId) {
        return pointRepository.findById(userId).orElseThrow().getBalance();
    }

    @Transactional
    public void initPoint(Long userId, long initialBalance) {
        pointRepository.save(Point.of(userId, initialBalance));
    }
}
