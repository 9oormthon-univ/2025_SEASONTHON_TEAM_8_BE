package textmate.backend.chatrooms.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import textmate.backend.chatrooms.api.dto.response.ChatRoomItemResponse;
import textmate.backend.chatrooms.api.dto.response.ChatRoomPageResponse;
import textmate.backend.chatrooms.domain.Enum.ChatRoomSort;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;
import textmate.backend.chatrooms.domain.chatRoom.ChatRoom;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        // 1) 원본 목록 로드
        List<ChatRoom> rooms = new ArrayList<>(store.findAllRooms());

        // 2) 필터: 삭제 제외 + 이름 검색 + 타입 필터
        String q = (query == null) ? "" : query.trim().toLowerCase();
        rooms = rooms.stream()
                .filter(r -> !r.isDeleted())
                .filter(r -> q.isEmpty() || r.getName().toLowerCase().contains(q))
                .filter(r -> type == null || r.getType() == type)
                .collect(Collectors.toList());

        // 3) 정렬
        Comparator<ChatRoom> cmpDefault =
                Comparator.comparing(ChatRoom::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();

        Comparator<ChatRoom> cmpRecentlyActive =
                Comparator.<ChatRoom, Integer>comparing(r -> r.getLastMessageAt() == null ? 1 : 0) // null last
                        .thenComparing(ChatRoom::getLastMessageAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                        .thenComparing(ChatRoom::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();

        Comparator<ChatRoom> cmpPinnedTop = (a, b) -> {
            boolean ap = store.isPinned(userId, a.getId());
            boolean bp = store.isPinned(userId, b.getId());
            if (ap != bp) return ap ? -1 : 1; // pinned 먼저
            // pinned 동일하면 최근활동 → 생성일
            int recent = Comparator
                    .<ChatRoom, Integer>comparing(r -> r.getLastMessageAt() == null ? 1 : 0)
                    .thenComparing(ChatRoom::getLastMessageAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                    .thenComparing(ChatRoom::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                    .compare(a, b);
            return recent;
        };

        ChatRoomSort applied = (sort == null) ? ChatRoomSort.DEFAULT : sort;
        rooms.sort(
                switch (applied) {
                    case RECENTLY_ACTIVE -> cmpRecentlyActive;
                    case PINNED_TOP     -> cmpPinnedTop;
                    default             -> cmpDefault;
                }
        );

        // 4) 페이지네이션 (메모리에서 수동 처리)
        int total = rooms.size();
        int from = Math.max(page * size, 0);
        int to = Math.min(from + size, total);
        List<ChatRoom> pageSlice = (from >= total) ? Collections.emptyList() : rooms.subList(from, to);

        // 5) DTO 매핑
        List<ChatRoomItemResponse> content = pageSlice.stream()
                .map(r -> ChatRoomItemResponse.builder()
                        .id(r.getId().toString())
                        .name(r.getName())
                        .type(r.getType())
                        .pinned(store.isPinned(userId, r.getId()))
                        .deleted(r.isDeleted())
                        .lastMessageAt(r.getLastMessageAt() == null ? null : ISO.format(r.getLastMessageAt()))
                        .createdAt(ISO.format(r.getCreatedAt()))
                        .build())
                .toList();

        int totalPages = (size <= 0) ? 1 : (int) Math.ceil((double) total / size);

        return ChatRoomPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }
}