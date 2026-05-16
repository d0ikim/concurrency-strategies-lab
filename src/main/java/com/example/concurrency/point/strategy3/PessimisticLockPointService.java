// 전략3. DB가 직접 행(row)을 잠그는 비관적 락 서비스 클래스
package com.example.concurrency.point.strategy3;

import com.example.concurrency.point.Point;
import com.example.concurrency.point.PointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessimisticLockPointService {

    private final PointRepository pointRepository;

    public PessimisticLockPointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    @Transactional  // 필수! DB잠금은 트랜잭션이 끝날 때(커밋 시) 해제됨. @Transactional 없으면 findByIdWithLock() 직후 잠금 해제돼서 의미없음
    public Point charge(Long userId, long amount) {
        // SELECT * FROM point WHERE id = ? FOR UPDATE
        // → DB가 이 행(row)을 잠금. 다른 트랜잭션은 이 행에 접근하려면 잠금 해제될 때까지 대기
        Point point = pointRepository.findByIdWithLock(userId).orElseThrow();   // 잠금 걸며 조회

        long currentBalance = point.getBalance();

        try { Thread.sleep(200); } catch (InterruptedException e) {}

        point.setBalance(currentBalance + amount);
        return pointRepository.save(point);
        // save() 후 @Transactional 커밋 → DB 잠금 해제 → 다음 스레드 진입
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