package com.example.concurrency.steps.strategy5;

import com.example.concurrency.point.strategy5.DistributedLockPointService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class DistributedLockPointSteps {

    @Autowired
    private DistributedLockPointService distributedLockPointService;

    private static final Long USER_ID = 5L;

    @Given("분산 락 전략으로 잔액이 0원인 사용자가 있다")
    public void 잔액초기화() {
        distributedLockPointService.initPoint(USER_ID, 0L);
    }

    @When("10명이 동시에 100원씩 분산 락으로 충전한다")
    public void 동시충전() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    distributedLockPointService.charge(USER_ID, 100L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    @Then("분산 락 최종 잔액은 1000원이어야 한다")
    public void 잔액검증() {
        long balance = distributedLockPointService.getBalance(USER_ID);
        assertThat(balance).isEqualTo(1000L);
    }
}
