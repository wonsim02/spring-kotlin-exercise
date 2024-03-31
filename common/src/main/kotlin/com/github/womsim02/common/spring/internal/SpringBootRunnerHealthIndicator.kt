package com.github.womsim02.common.spring.internal

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener

/**
 * 스프링 부트 어플리케이션을 실행하는 과정에서 등록된 [ApplicationRunner] 및 [CommandLineRunner]을 모두 실행하기 전까지 actuator 엔드포인트를
 * 통한 health 체크가 down`이 되도록 한다.
 * 서버 배포 시 health 체크를 통해 요청 수신 여부를 결정한다면 등록된 runner가 전부 실행되기 전까지 서버가 요청을 수신하지 않도록 해 스프링 부트 실행 이후
 * 서버 준비 작업이 [ApplicationRunner] 혹은 [CommandLineRunner]의 구현을 통해 가능해진다.
 * @see org.springframework.boot.actuate.health.HealthEndpoint.getHealth
 * @see org.springframework.boot.SpringApplication.run
 * @see org.springframework.boot.SpringApplicationRunListeners.ready
 */
internal class SpringBootRunnerHealthIndicator : AbstractHealthIndicator(), ApplicationListener<ApplicationReadyEvent> {

    private var ready: Boolean = false

    override fun doHealthCheck(builder: Health.Builder?) {
        if (ready) {
            builder?.up()
        } else {
            builder?.down()
        }
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        ready = true
    }
}
