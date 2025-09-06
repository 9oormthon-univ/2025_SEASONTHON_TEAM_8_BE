package textmate.backend.chatrooms.domain;

import jakarta.persistence.*;
import lombok.*;
import textmate.backend.chatrooms.domain.Enum.AnalysisType;

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
    private AnalysisType type;   // GROUP | PRIVATE

    @Lob
    private String rawText;      // 원본 텍스트

    @Lob
    private String resultJson;   // 분석 결과 JSON

    private LocalDateTime createdAt;
}
