package com.example.concurrency.steps.strategy4;

import com.example.concurrency.point.strategy4.OptimisticLockPointService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class OptimisticLockPointSteps {

    @Autowired
    private OptimisticLockPointService optimisticLockPointService;

    private static final Long USER_ID = 4L;

    @Given("낙관적 락 전략으로 잔액이 0원인 사용자가 있다")
    public void 잔액초기화() {
        optimisticLockPointService.initPoint(USER_ID, 0L);
    }

    @When("10명이 동시에 100원씩 낙관적 락으로 충전한다")
    public void 동시충전() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    optimisticLockPointService.charge(USER_ID, 100L);
                    // 충돌 시 Service 안 while(true)가 자동으로 재시도
                    // 스레드 10개 동시 시작 -> 각자 charge() 호출 - Helper.chargeOnce() 시도 -> 스레드1만 저장성공(version 0->1), 나머지9개 - OptimisticLockException -> while(true)로 재시도 -> 재시도 -> 스레드2 저장성공(version 1->2), 나머지 8개 재시도... -> 결국 10개 모두성공
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    @Then("낙관적 락 최종 잔액은 1000원이어야 한다")
    public void 잔액검증() {
        long balance = optimisticLockPointService.getBalance(USER_ID);
        assertThat(balance).isEqualTo(1000L);
    }
}