package textmate.backend.chatrooms.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import textmate.backend.chatrooms.api.dto.request.UpdateChatRoomRequest;
import textmate.backend.chatrooms.api.dto.response.ChatRoomResponse;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;
import textmate.backend.chatrooms.domain.InMemoryChatRoomStore;
import textmate.backend.chatrooms.domain.chatRoom.ChatRoom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 방 단건 조회/수정/삭제 (인메모리)
 * - 조회: pinned 포함해서 DTO로 변환
 * - 수정: 불변 객체 재생성 후 upsert
 * - 삭제: 소프트 삭제(deleted=true)로 마킹
 */
@Service
@RequiredArgsConstructor
public class ChatRoomCommandService {

    private final InMemoryChatRoomStore store;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** 1.3 방 단건 조회 */
    @Transactional(readOnly = true)
    public ChatRoomResponse getOne(Long userId, String roomId) {
        ChatRoom r = getOrThrow(roomId);
        return toResponse(userId, r);
    }

    /** 1.4 방 이름/속성 수정 (모두 선택적) */
    @Transactional
    public ChatRoomResponse patch(Long userId, String roomId, UpdateChatRoomRequest req) {
        ChatRoom cur = getOrThrow(roomId);

        String nextName = (req.getName() == null || req.getName().isBlank())
                ? cur.getName() : req.getName().trim();
        ChatRoomType nextType = (req.getType() == null) ? cur.getType() : req.getType();

        ChatRoom updated = ChatRoom.builder()
                .id(cur.getId())
                .name(nextName)
                .type(nextType)
                .deleted(cur.isDeleted())
                .createdAt(cur.getCreatedAt())
                .updatedAt(LocalDateTime.now())   // ✅ 수정 시간 갱신
                .lastMessageAt(cur.getLastMessageAt())
                .build();

        store.upsertRoom(updated);
        return toResponse(userId, updated);
    }

    /** 1.5 방 삭제(소프트 삭제) → deleted=true */
    @Transactional
    public void softDelete(String roomId) {
        ChatRoom cur = getOrThrow(roomId);
        ChatRoom deleted = ChatRoom.builder()
                .id(cur.getId())
                .name(cur.getName())
                .type(cur.getType())
                .deleted(true)                    // ✅ 소프트 삭제
                .createdAt(cur.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .lastMessageAt(cur.getLastMessageAt())
                .build();

        store.upsertRoom(deleted);
        // 핀 목록에서도 뺄 거면: store.setPinned(userId, id, false) 가 필요하지만
        // 사용자별이므로 여기서는 방 전역 삭제만 처리
    }

    // ===== Helpers =====

    private ChatRoom getOrThrow(String roomId) {
        return store.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + roomId));
    }

    private ChatRoomResponse toResponse(Long userId, ChatRoom r) {
        return ChatRoomResponse.builder()
                .id(r.getId().toString())
                .name(r.getName())
                .type(r.getType())
                .pinned(store.isPinned(userId, r.getId()))
                .deleted(r.isDeleted())
                .createdAt(ISO.format(r.getCreatedAt()))
                .updatedAt(r.getUpdatedAt() == null ? null : ISO.format(r.getUpdatedAt()))
                .build();
    }
}