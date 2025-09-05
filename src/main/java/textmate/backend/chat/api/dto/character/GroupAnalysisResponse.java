package textmate.backend.chat.api.dto.character;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GroupAnalysisResponse {
    private Long historyId;                // 저장된 기록 ID
    private int analyzedCount;             // 분석한 메시지 수 (최근 500줄)
    private String summaryOfContents;      // Summary of contents

    private List<ParticipantCard> participants; // 상대방 캐릭터 분석 카드들
    private TopContributor topContributor; // 가장 의견을 많이 낸 사람 요약

    private Diagnosis diagnosis;           // 관계 진단/조언 (Frame6의 상단 카드)
    private List<String> oneLineSummary;   // 한줄 요약 리스트 (Op/보라 카드들)
    private LocalDateTime createdAt;
}
