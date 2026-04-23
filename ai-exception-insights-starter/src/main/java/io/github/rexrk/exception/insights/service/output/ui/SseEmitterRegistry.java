//package io.github.rexrk.exception.insights.service.output.ui;
//
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class SseEmitterRegistry {
//
//    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
//
//    public SseEmitter register(String clientId) {
//        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
//
//        emitter.onCompletion(() -> emitters.remove(clientId));
//        emitter.onTimeout(() -> emitters.remove(clientId));
//        emitter.onError(e -> emitters.remove(clientId));
//
//        emitters.put(clientId, emitter);
//        return emitter;
//    }
//
//    public void broadcast(String eventName, Object data) {
//        emitters.forEach((clientId, emitter) -> {
//            try {
//                emitter.send(SseEmitter.event()
//                    .name(eventName)
//                    .data(data));
//            } catch (IOException e) {
//                emitters.remove(clientId);
//            }
//        });
//    }
//}