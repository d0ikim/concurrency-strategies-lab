// 3. (feature파일에서 시나리오 읽어 목록만들고) 문장과 코드 매칭
package com.example.concurrency.steps.strategy1;

import com.example.concurrency.point.strategy1.SynchronizedPointService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class SynchronizedPointSteps {

    @Autowired
    private SynchronizedPointService synchronizedPointService;

    private static final Long USER_ID = 1L;

    @Given("synchronized 전략으로 잔액이 0원인 사용자가 있다")
    public void 잔액초기화() {
        synchronizedPointService.initPoint(USER_ID, 0L);
    }

    @When("10명이 동시에 100원씩 충전한다")
    public void 동시충전() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    synchronizedPointService.charge(USER_ID, 100L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    @Then("최종 잔액은 1000원이어야 한다")
    public void 잔액검증() {
        long balance = synchronizedPointService.getBalance(USER_ID);
        assertThat(balance).isEqualTo(1000L);
    }
}
