package textmate.backend.chatrooms.domain;

import jakarta.persistence.*;
import lombok.*;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;
import textmate.backend.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatAnalysisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;   // GROUP | PRIVATE

    @Lob
    private String rawText;      // 원본 텍스트

    @Lob
    private String resultJson;   // 분석 결과 JSON

    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_user_id", nullable = false)
    private User user;
}
