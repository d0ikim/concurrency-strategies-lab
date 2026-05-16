// 전략4. 낙관적 락 - 실제 충전 1번 시도 담당 (재시도 루프는 Service에서 처리)
package com.example.concurrency.point.strategy4;

import com.example.concurrency.point.Point;
import com.example.concurrency.point.PointRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component  // @Service 대신 @Component - "나는 서비스가 아니라 도우미야"
public class OptimisticLockPointHelper {

    private final PointRepository pointRepository;

    public OptimisticLockPointHelper(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    @Transactional  // 외부(Service)에서 호출되므로 @Transactional 정상 작동
    public Point chargeOnce(Long userId, long amount) {
        Point point = pointRepository.findById(userId).orElseThrow();
        // JPA가 저장 시점에 version 값을 자동으로 체크함
        // "내가 읽었을 때 version과 지금 DB의 version이 다르면 → OptimisticLockException 던짐"

        long currentBalance = point.getBalance();

        try { Thread.sleep(200); } catch (InterruptedException e) {}

        point.setBalance(currentBalance + amount);
        return pointRepository.save(point);
        // 저장 성공 시 version 자동으로 +1 증가
    }
}