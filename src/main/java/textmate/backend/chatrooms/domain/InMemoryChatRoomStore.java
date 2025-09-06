package textmate.backend.chatrooms.domain;

import org.springframework.stereotype.Component;
import textmate.backend.chatrooms.domain.chatRoom.ChatRoom;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 저장소
 * - rooms: 방 목록
 * - pinnedByUser: 사용자별 고정 방 목록
 *
 * 쓰레드 세이프를 위해 ConcurrentHashMap 사용.
 */
@Component
public class InMemoryChatRoomStore {

    /** 모든 방 저장 (id → room) */
    private final Map<UUID, ChatRoom> rooms = new ConcurrentHashMap<>();

    /** 사용자별 핀 상태 저장 (userId → roomId 집합) */
    private final Map<Long, Set<UUID>> pinnedByUser = new ConcurrentHashMap<>();

    public Collection<ChatRoom> findAllRooms() {
        return rooms.values();
    }

    public void upsertRoom(ChatRoom room) {
        rooms.put(room.getId(), room);
    }

    public void deleteRoom(UUID roomId) {
        rooms.remove(roomId);
        pinnedByUser.values().forEach(set -> set.remove(roomId));
    }

    public boolean isPinned(Long userId, UUID roomId) {
        return pinnedByUser.getOrDefault(userId, Collections.emptySet()).contains(roomId);
    }

    public void setPinned(Long userId, UUID roomId, boolean pinned) {
        pinnedByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        if (pinned) {
            pinnedByUser.get(userId).add(roomId);
        } else {
            pinnedByUser.get(userId).remove(roomId);
        }
    }

    public Optional<ChatRoom> findById(UUID id) {
        return Optional.ofNullable(rooms.get(id));
    }
}