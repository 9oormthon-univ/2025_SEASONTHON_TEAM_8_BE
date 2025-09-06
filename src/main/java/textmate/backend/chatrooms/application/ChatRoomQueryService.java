package textmate.backend.chatrooms.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import textmate.backend.chatrooms.api.dto.response.ChatRoomItemResponse;
import textmate.backend.chatrooms.api.dto.response.ChatRoomPageResponse;
import textmate.backend.chatrooms.domain.Enum.ChatRoomSort;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;
import textmate.backend.chatrooms.domain.InMemoryChatRoomStore;
import textmate.backend.chatrooms.domain.chatRoom.ChatRoom;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private final InMemoryChatRoomStore store;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional(readOnly = true)
    public ChatRoomPageResponse getRooms(Long userId,
                                         String query,
                                         ChatRoomType type,
                                         ChatRoomSort sort,
                                         int page,
                                         int size) {
        try {
            log.info("채팅방 목록 조회 서비스 - userId: {}, query: {}, type: {}, sort: {}, page: {}, size: {}", 
                    userId, query, type, sort, page, size);

            // 1) 원본 목록 로드
            List<ChatRoom> rooms = new ArrayList<>(store.findAllRooms());

            // 2) 필터: 삭제 제외 + 이름 검색 + 타입 필터
            String q = (query == null) ? "" : query.trim().toLowerCase();
            rooms = rooms.stream()
                    .filter(r -> !r.isDeleted())
                    .filter(r -> q.isEmpty() || (r.getName() != null && r.getName().toLowerCase().contains(q)))
                    .filter(r -> type == null || r.getType() == type)
                    .collect(Collectors.toList());

            // 3) 정렬
            ChatRoomSort applied = (sort == null) ? ChatRoomSort.DEFAULT : sort;
            rooms.sort(getComparator(applied, userId));

            // 4) 페이지네이션 (메모리에서 수동 처리)
            int total = rooms.size();
            int from = Math.max(page * size, 0);
            int to = Math.min(from + size, total);
            List<ChatRoom> pageSlice = (from >= total) ? Collections.emptyList() : rooms.subList(from, to);

            // 5) DTO 매핑
            List<ChatRoomItemResponse> content = pageSlice.stream()
                    .map(r -> toItemResponse(r, userId))
                    .toList();

            int totalPages = (size <= 0) ? 1 : (int) Math.ceil((double) total / size);

            ChatRoomPageResponse response = ChatRoomPageResponse.builder()
                    .content(content)
                    .page(page)
                    .size(size)
                    .totalElements(total)
                    .totalPages(totalPages)
                    .build();
                    
            log.info("채팅방 목록 조회 완료 - 총 {}개, 페이지 {}개", total, totalPages);
            return response;
            
        } catch (Exception e) {
            log.error("채팅방 목록 조회 중 오류 발생", e);
            throw new RuntimeException("채팅방 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<ChatRoomItemResponse> getAllRooms(Long userId,
                                                  String query,
                                                  ChatRoomType type,
                                                  ChatRoomSort sort) {
        try {
            log.info("채팅방 전체 조회 서비스 - userId: {}, query: {}, type: {}, sort: {}", 
                    userId, query, type, sort);

            // 1) 원본 + 필터
            String q = (query == null) ? "" : query.trim().toLowerCase();
            List<ChatRoom> rooms = new ArrayList<>(store.findAllRooms()).stream()
                    .filter(r -> !r.isDeleted())
                    .filter(r -> q.isEmpty() || (r.getName() != null && r.getName().toLowerCase().contains(q)))
                    .filter(r -> type == null || r.getType() == type)
                    .collect(Collectors.toList());

            // 2) 정렬
            ChatRoomSort applied = (sort == null) ? ChatRoomSort.DEFAULT : sort;
            rooms.sort(getComparator(applied, userId));

            // 3) 매핑
            List<ChatRoomItemResponse> response = rooms.stream()
                    .map(r -> toItemResponse(r, userId))
                    .toList();
                    
            log.info("채팅방 전체 조회 완료 - 총 {}개", response.size());
            return response;
            
        } catch (Exception e) {
            log.error("채팅방 전체 조회 중 오류 발생", e);
            throw new RuntimeException("채팅방 전체 조회 실패: " + e.getMessage(), e);
        }
    }
    private Comparator<ChatRoom> getComparator(ChatRoomSort sort, Long userId) {
        return switch (sort) {
            case RECENTLY_ACTIVE -> cmpRecentlyActive();
            case PINNED_TOP     -> cmpPinnedTop(userId);
            default             -> cmpDefault();
        };
    }
    
    private Comparator<ChatRoom> cmpDefault() {
        return Comparator.comparing(ChatRoom::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed();
    }
    
    private Comparator<ChatRoom> cmpRecentlyActive() {
        return Comparator
                .<ChatRoom, Integer>comparing(r -> r.getLastMessageAt() == null ? 1 : 0) // null → 뒤로
                .thenComparing(ChatRoom::getLastMessageAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
                .thenComparing(ChatRoom::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed();
    }
    
    private Comparator<ChatRoom> cmpPinnedTop(Long userId) {
        return (a, b) -> {
            try {
                boolean ap = userId != null && store.isPinned(userId, a.getId());
                boolean bp = userId != null && store.isPinned(userId, b.getId());

                // pinned 우선
                if (ap != bp) return ap ? -1 : 1;

                // pinned 동일 → 최근 활동 기준
                return cmpRecentlyActive().thenComparing(cmpDefault()).compare(a, b);
            } catch (Exception e) {
                log.warn("정렬 중 오류 발생", e);
                return 0;
            }
        };
    }
    
    private ChatRoomItemResponse toItemResponse(ChatRoom r, Long userId) {
        try {
            return ChatRoomItemResponse.builder()
                    .id(r.getId().toString())
                    .name(r.getName())
                    .type(r.getType())
                    .pinned(userId != null && store.isPinned(userId, r.getId()))
                    .deleted(r.isDeleted())
                    .lastMessageAt(r.getLastMessageAt() == null ? null : ISO.format(r.getLastMessageAt()))
                    .createdAt(ISO.format(r.getCreatedAt()))
                    .build();
        } catch (Exception e) {
            log.error("ChatRoomItemResponse 변환 중 오류 발생 - roomId: {}", r.getId(), e);
            throw new RuntimeException("응답 변환 실패: " + e.getMessage(), e);
        }
    }

}