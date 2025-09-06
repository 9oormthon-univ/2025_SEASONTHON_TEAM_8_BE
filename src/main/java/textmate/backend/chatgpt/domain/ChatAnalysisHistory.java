package textmate.backend.chatgpt.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import textmate.backend.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ChatAnalysisHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user; // 기존 User 엔티티와 연관

    @Enumerated(EnumType.STRING)
    private RelationshipType relationshipType; // LOVER, FRIEND, WORK, FAMILY, OTHER

    @Column(length = 4000)
    private String summary;

    private int messageCount;

    private LocalDateTime createdAt = LocalDateTime.now();

    // LLM 구조화 출력 저장용 (JSON 또는 TEXT)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String analysisJson;

    public ChatAnalysisHistory(User user, RelationshipType relationshipType,
                               String summary, int messageCount, String analysisJson) {
        this.user = user;
        this.relationshipType = relationshipType;
        this.summary = summary;
        this.messageCount = messageCount;
        this.analysisJson = analysisJson;
    }
}
