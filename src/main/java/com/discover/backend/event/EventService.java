package com.discover.backend.event;

import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepo;

    public void record(User user, String eventType, String entityType, Long entityId, Map<String, Object> context) {
        Event event = Event.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .context(context)
                .user(user)
                .build();

        eventRepo.save(event);
    }
}
