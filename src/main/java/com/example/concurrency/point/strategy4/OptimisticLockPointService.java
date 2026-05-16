// 전략4. 낙관적 락 - 재시도 루프 담당 서비스 클래스
package com.example.concurrency.point.strategy4;

import com.example.concurrency.point.Point;
import com.example.concurrency.point.PointRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimisticLockPointService {

    private final PointRepository pointRepository;
    private final OptimisticLockPointHelper helper;

    public OptimisticLockPointService(PointRepository pointRepository, OptimisticLockPointHelper helper) {
        this.pointRepository = pointRepository;
        this.helper = helper;
    }

    public Point charge(Long userId, long amount) {
        // @Transactional 없음 - 재시도 루프는 트랜잭션 밖에 있어야 함
        // 트랜잭션 안에서 재시도하면 처음 읽은 낡은 version을 계속 쓰게 됨

        while (true) {  // 성공할 때까지 재시도
            try {
                return helper.chargeOnce(userId, amount);  // Helper가 트랜잭션 안에서 충전 1번 시도
            } catch (ObjectOptimisticLockingFailureException e) {
                // version 충돌! 다른 스레드가 먼저 저장함 → try문으로 돌아가 재시도
            }
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