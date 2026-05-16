// 전략5. FakeRedisLock으로 분산 락을 시뮬레이션하는 서비스 클래스
package com.example.concurrency.point.strategy5;

import com.example.concurrency.lock.FakeRedisLock;
import com.example.concurrency.point.Point;
import com.example.concurrency.point.PointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DistributedLockPointService {

    private final PointRepository pointRepository;
    private final FakeRedisLock fakeRedisLock;

    public DistributedLockPointService(PointRepository pointRepository, FakeRedisLock fakeRedisLock) {
        this.pointRepository = pointRepository;
        this.fakeRedisLock = fakeRedisLock;
    }

    public Point charge(Long userId, long amount) {
        String lockKey = "point:" + userId; // 잠글 키 이름 (사용자별로 다른 키 사용)

        while (!fakeRedisLock.tryLock(lockKey)) {
            // tryLock 실패(다른 스레드가 이미 잠금 중) → 10ms 기다렸다가 재시도
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }

        try {
            Point point = pointRepository.findById(userId).orElseThrow();

            long currentBalance = point.getBalance();

            try { Thread.sleep(200); } catch (InterruptedException e) {}

            point.setBalance(currentBalance + amount);
            return pointRepository.save(point);
        } finally {
            fakeRedisLock.unlock(lockKey); // 에러가 나도 반드시 잠금 해제
        }
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
