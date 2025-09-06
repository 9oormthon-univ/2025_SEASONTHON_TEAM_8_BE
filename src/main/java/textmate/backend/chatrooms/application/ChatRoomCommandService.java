package textmate.backend.chatrooms.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomCommandService {

    private final InMemoryChatRoomStore store;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** 1.3 방 단건 조회 */
    @Transactional(readOnly = true)
    public ChatRoomResponse getOne(Long userId, String roomId) {
        try {
            log.info("채팅방 단건 조회 요청 - userId: {}, roomId: {}", userId, roomId);
            
            if (roomId == null || roomId.trim().isEmpty()) {
                log.warn("roomId가 null이거나 비어있습니다");
                throw new IllegalArgumentException("roomId는 필수입니다");
            }
            
            ChatRoom r = getOrThrow(roomId);
            ChatRoomResponse response = toResponse(userId, r);
            log.info("채팅방 단건 조회 완료 - roomId: {}", roomId);
            
            return response;
            
        } catch (Exception e) {
            log.error("채팅방 단건 조회 중 오류 발생 - roomId: {}", roomId, e);
            throw new RuntimeException("채팅방 조회 실패: " + e.getMessage(), e);
        }
    }

    /** 1.4 방 이름/속성 수정 (모두 선택적) */
    @Transactional
    public ChatRoomResponse patch(Long userId, String roomId, UpdateChatRoomRequest req) {
        try {
            log.info("채팅방 수정 요청 - userId: {}, roomId: {}", userId, roomId);
            
            if (roomId == null || roomId.trim().isEmpty()) {
                log.warn("roomId가 null이거나 비어있습니다");
                throw new IllegalArgumentException("roomId는 필수입니다");
            }
            
            if (req == null) {
                log.warn("수정 요청이 null입니다");
                throw new IllegalArgumentException("수정 요청은 필수입니다");
            }
            
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
            ChatRoomResponse response = toResponse(userId, updated);
            log.info("채팅방 수정 완료 - roomId: {}", roomId);
            
            return response;
            
        } catch (Exception e) {
            log.error("채팅방 수정 중 오류 발생 - roomId: {}", roomId, e);
            throw new RuntimeException("채팅방 수정 실패: " + e.getMessage(), e);
        }
    }

    /** 1.5 방 삭제(소프트 삭제) → deleted=true */
    @Transactional
    public void softDelete(String roomId) {
        try {
            log.info("채팅방 삭제 요청 - roomId: {}", roomId);
            
            if (roomId == null || roomId.trim().isEmpty()) {
                log.warn("roomId가 null이거나 비어있습니다");
                throw new IllegalArgumentException("roomId는 필수입니다");
            }
            
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
            log.info("채팅방 삭제 완료 - roomId: {}", roomId);
            
            // 핀 목록에서도 뺄 거면: store.setPinned(userId, id, false) 가 필요하지만
            // 사용자별이므로 여기서는 방 전역 삭제만 처리
            
        } catch (Exception e) {
            log.error("채팅방 삭제 중 오류 발생 - roomId: {}", roomId, e);
            throw new RuntimeException("채팅방 삭제 실패: " + e.getMessage(), e);
        }
    }

    // ===== Helpers =====

    private ChatRoom getOrThrow(String roomId) {
        try {
            UUID uuid = UUID.fromString(roomId);
            return store.findById(uuid)
                    .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + roomId));
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 UUID 형식: {}", roomId);
            throw new IllegalArgumentException("잘못된 roomId 형식입니다: " + roomId, e);
        }
    }

    private ChatRoomResponse toResponse(Long userId, ChatRoom r) {
        try {
            return ChatRoomResponse.builder()
                    .id(r.getId().toString())
                    .name(r.getName())
                    .type(r.getType())
                    .pinned(userId != null ? store.isPinned(userId, r.getId()) : false)
                    .deleted(r.isDeleted())
                    .createdAt(ISO.format(r.getCreatedAt()))
                    .updatedAt(r.getUpdatedAt() == null ? null : ISO.format(r.getUpdatedAt()))
                    .build();
        } catch (Exception e) {
            log.error("ChatRoomResponse 변환 중 오류 발생 - roomId: {}", r.getId(), e);
            throw new RuntimeException("응답 변환 실패: " + e.getMessage(), e);
        }
    }
}