// 전략2. ReentrantLock으로 명시적 잠금/해제를 처리한 서비스 클래스
package com.example.concurrency.point.strategy2;

import com.example.concurrency.point.Point;
import com.example.concurrency.point.PointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.ReentrantLock;

@Service
public class ReentrantLockPointService {

    private final PointRepository pointRepository;
    private final ReentrantLock lock = new ReentrantLock(); // ReentrantLock 객체를 서비스클래스 안에 하나 만듬(자물쇠 역할). 서비스가 스프링에의해 하나만 만들어지니까, 이 자물쇠도 하나만 존재함

    public ReentrantLockPointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    public Point charge(Long userId, long amount) {
        lock.lock();    // ① 직접 자물쇠 잠금 - 다른 스레드는 여기서 대기

        // ①lock() ~ ③unlock() 사이만 잠금이 걸려서, 다른 스레드는 ①lock()에서 대기하다가, ③unlock()이 호출되면 잠금이 풀리고 다음 스레드가 ①lock()에서 잠금 시도 -> 순차적으로 한 스레드씩 ②charge() 실행

        try {   // ② 충전 로직 (한 스레드만 실행)
            Point point = pointRepository.findById(userId).orElseThrow();

            long currentBalance = point.getBalance();

            try { Thread.sleep(200); } catch (InterruptedException e) {}

            point.setBalance(currentBalance + amount);
            return pointRepository.save(point);
        } finally { // ②에서 에러가 터졌을 때
            // finally 없으면: 자물쇠 잠긴채로 메서드 종료 -> 다른스레드가 영원히 대기 -> 서버멈춤!
            // finally 있으면: 에러가나도 반드시 unlock() 실행 -> 자물쇠 항상 해제 -> 안전!
            lock.unlock();  // ③ 반드시 자물쇠 해제. finally로 에러가 나도 반드시 해제 보장
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