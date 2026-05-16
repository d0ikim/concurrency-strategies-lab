// 3. (feature파일에서 시나리오 읽어 목록만들고) 문장과 코드 매칭
// =
package com.example.concurrency.steps;

import com.example.concurrency.point.SynchronizedPointService;
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

    @Given("synchronized 전략으로 잔액이 0원인 사용자가 있다")  // 1-1. features 파일에서 읽은 목록 중에"synchronized 전략으로 잔액이 0원인 사용자가 있다" 문자열 일치여부 확인 후 매칭!
    public void 잔액초기화() {  // 2-1. 매칭 후 Given 실행
        synchronizedPointService.initPoint(USER_ID, 0L);    // DB에 잔액0원 저장
    }

    @When("10명이 동시에 100원씩 충전한다") // 1-2. features 파일에서 읽은 목록 중에 "10명이 동시에 100원씩 충전한다" 문자열 일치여부 확인 후 매칭!
    public void 동시충전() throws InterruptedException {    // 2-2. 매칭 후 When 실행
        int threadCount = 10;
        // ExecutorService - 스레드 여러개를 관리하는 매니저
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);   // Executors.newixedThreadPool(10) - 스레드 10개짜리 팀을 만들어줘
        CountDownLatch latch = new CountDownLatch(threadCount); // new CountDownLatch(10) - 빗장에 숫자10을 걸어놓음

        for (int i = 0; i < threadCount; i++) {
            // executor.submit(() -> {...}) - "이 일을 팀원한테 맡겨줘"
            executor.submit(() -> {
                try {
                    synchronizedPointService.charge(USER_ID, 100L);
                } finally {
                    latch.countDown();  // 스레드 하나 끝날때마다 -1 (10->9->8->7->6->5->4->3->2->1->0 빗장열림!)
                }
            });
        }

        latch.await();  // 빗장이 0이될때까지(= 10개 다 끝날때까지) 대기. 0이되면->다음줄 실행(잔액검증으로 넘어감)
        executor.shutdown();
    }

    // 통과/실패 판단 => Then문장과 매칭되는 메소드에서 assertThat()으로 검증
    @Then("최종 잔액은 1000원이어야 한다")  // 1-3. features 파일에서 읽은 목록 중에 "최종 잔액은 1000원이어야 한다" 문자열 일치여부 확인 후 매칭!
    public void 잔액검증() {    // 2-3. 매칭 후 Then 실행
        long balance = synchronizedPointService.getBalance(USER_ID);    // 잔액 조회
        // balance가 1000원이면 -> 통과(초록불)
        // balance가 다른 값이면 -> 실패(빨간불)
        assertThat(balance).isEqualTo(1000L);   // 1000원인지 확인
    }
}