package com.example.concurrency.point;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity // "이 클래스를 DB테이블로 만들어줘" 라는 메모스티커
public class Point {

    @Id // "이 필드가 기본키야" (테이블의 행을 구분하는 고유값)
    private Long id;    // 사용자 ID

    private long balance;   // 포인트 잔액

    @Version    // "이 필드로 충돌감지해줘" (낙관적 락 전략에서 사용)
    private Long version;

    protected Point() {}    // JPA가 DB에서 데이터꺼낼때 내부적으로 빈객체 먼저만들때 필요한 빈 생성자(필수)

    public static Point of(Long userId, long initialBalance) {  // 우리가 직접 Point 만들때 쓰는 메서드
        Point point = new Point();
        point.id = userId;
        point.balance = initialBalance;
        return point;
    }

    public Long getId() { return id; }
    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }
}
