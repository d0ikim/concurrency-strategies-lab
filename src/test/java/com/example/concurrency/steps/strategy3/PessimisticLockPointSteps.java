package com.example.concurrency.steps.strategy3;

import com.example.concurrency.point.strategy3.PessimisticLockPointService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class PessimisticLockPointSteps {

    @Autowired
    private PessimisticLockPointService pessimisticLockPointService;

    private static final Long USER_ID = 3L; // 전략마다 userId 구분 (DB 데이터 충돌 방지)

    @Given("비관적 락 전략으로 잔액이 0원인 사용자가 있다")
    public void 잔액초기화() {
        pessimisticLockPointService.initPoint(USER_ID, 0L); // DB에 잔액 0원 저장
    }

    @When("10명이 동시에 100원씩 비관적 락으로 충전한다")
    public void 동시충전() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pessimisticLockPointService.charge(USER_ID, 100L);  // 각 스레드가 findByIdWithLock() -> DB가 행 잠금 -> 
                } finally {
                    latch.countDown();  // 순서대로 처리
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    @Then("비관적 락 최종 잔액은 1000원이어야 한다")
    public void 잔액검증() {
        long balance = pessimisticLockPointService.getBalance(USER_ID);
        assertThat(balance).isEqualTo(1000L);   // 1000원이면 통과
    }
}