package kr.giljabi.api.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 스케줄링 작업을 수행하는 클래스
 * 불필요한 object를 삭제하기 위한 스케줄링 작업을 수행한다.
 */
@Component
public class ScheduledTasks {

    // 매 분 첫 번째 초에 실행
    @Scheduled(cron = "0 * * * * ?")
    public void performTaskUsingCron() {
        System.out.println("매 분 첫 번째 초에 실행 Cron task performed at " + System.currentTimeMillis());
    }
}
