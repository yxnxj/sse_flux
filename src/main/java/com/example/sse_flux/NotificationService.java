package com.example.sse_flux;

import org.slf4j.Logger;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    final Flux<ServerSentEvent<Notification>> notificationFlux = Flux.push(this::generateNotifications);

    private Integer generateId() {
        return new Random().nextInt(4);
    }

    private ServerSentEvent<Notification> generateNotification() {
        return ServerSentEvent.<Notification>builder()
                .data(new Notification(generateId(), "Notification"))
                .build();
    }

    private void generateNotifications(FluxSink<ServerSentEvent<Notification>> sink) {
        Flux.interval(Duration.ofSeconds(2)) // Generate simple notifications every 2 seconds.
                .map(i -> generateNotification())
                .doOnNext(serverSentEvent -> {
                    sink.next(serverSentEvent); // Sending notifications to the global Flux via its FluxSink
                    log.info("Sent for {}", serverSentEvent.data().getId());
                })
                .doFinally(signalType -> log.info("Notification flux closed")) // Logging the closure of our generator
                .takeWhile(notification -> !sink.isCancelled()) // We generate messages until the global Flux is closed
                .subscribe();
    }

    private <T> Flux keepAlive(Duration duration, Flux<T> data, Integer id) {
        Flux<ServerSentEvent<T>> heartBeat = Flux.interval(duration) // Создаем Flux с определенным интервалом
                .map(
                        e -> ServerSentEvent.<T>builder() //Create a new SSE object with a comment and an empty body
      .comment("keep alive for: " + id)
      .build())
      .doFinally(signalType -> log.info("Heartbeat closed for id: {}", id));
        return Flux.merge(heartBeat, data);
    }

    public Flux<ServerSentEvent<Notification>> subscribe(int id) {
        return keepAlive(Duration.ofSeconds(3),
                notificationFlux.filter(notification -> notification.data() == null ||
                        notification.data().getId() == id), id);
    }


}
