package textmate.backend.chatgpt.api.individual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/* ---------- [5] 대화 패턴 인사이트 ---------- */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PatternInsightsSection {
    private String title;        // "대화 패턴 인사이트"
    private List<String> bullets; // 글머리표 인사이트
}
