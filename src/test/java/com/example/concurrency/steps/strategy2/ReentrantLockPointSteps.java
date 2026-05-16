package com.example.concurrency.steps.strategy2;

import com.example.concurrency.point.strategy2.ReentrantLockPointService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class ReentrantLockPointSteps {

    @Autowired
    private ReentrantLockPointService reentrantLockPointService;

    private static final Long USER_ID = 2L; // H2는 인메모리DB라서 테스트 전체가 하나의DB를 공유함.
    // 전략1은 1L, 전략2는 2L, 전략3은 3L로 구분해서 테스트. 왜? 전략1이 userId=1로 저장한 데이터랑 전략2데이터가 충돌하지 않도록 userId를 다르게 씀.

    @Given("ReentrantLock 전략으로 잔액이 0원인 사용자가 있다")
    public void 잔액초기화() {
        reentrantLockPointService.initPoint(USER_ID, 0L);
    }

    @When("10명이 동시에 100원씩 ReentrantLock으로 충전한다")
    public void 동시충전() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    reentrantLockPointService.charge(USER_ID, 100L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    @Then("ReentrantLock 최종 잔액은 1000원이어야 한다")
    public void 잔액검증() {
        long balance = reentrantLockPointService.getBalance(USER_ID);
        assertThat(balance).isEqualTo(1000L);
    }
}