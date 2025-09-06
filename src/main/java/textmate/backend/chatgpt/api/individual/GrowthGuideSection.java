package textmate.backend.chatgpt.api.individual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/* ---------- [6] 관계 성장 가이드 ---------- */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class GrowthGuideSection {
    private String title;        // "관계 성장 가이드"
    private List<String> bullets; // 실천 가이드 (구체 행동)
}
