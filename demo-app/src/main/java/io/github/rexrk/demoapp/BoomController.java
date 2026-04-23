package io.github.rexrk.demoapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boom")
public class BoomController {

    // 1️⃣ HTTP exception
    @GetMapping("/http")
    public String httpBoom() {
        throw new RuntimeException("HTTP boom test");
    }

    // 2️⃣ Async exception
    @Async
    @GetMapping("/async")
    public void asyncBoom() {
        throw new RuntimeException("Async boom test");
    }

    // 3️⃣ Thread exception
    @GetMapping("/thread")
    public String threadBoom() {
        new Thread(() -> {
            throw new RuntimeException("Thread boom test");
        }).start();
        return "Thread started";
    }

    // 4️⃣ Event listener exception
    @Autowired
    private ApplicationEventPublisher publisher;

    @GetMapping("/event")
    public String eventBoom() {
        publisher.publishEvent(new BoomEvent(this));
        return "Event published";
    }

    // 5️⃣ Transactional exception
//    @Transactional
//    @GetMapping("/tx")
//    public String transactionalBoom() {
//        throw new RuntimeException("Transactional boom test");
//    }

    // 6️⃣ Scheduled exception trigger
    @Scheduled(fixedDelay = 10000)
    public void scheduledBoom() {
        throw new RuntimeException("Scheduled boom test");
    }
}