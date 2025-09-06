package textmate.backend.chatrooms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import textmate.backend.chatrooms.domain.Enum.AnalysisType;

@Getter
@Builder
@AllArgsConstructor
// 카카오톡 대화 분석 결과 응답
// json 결과 포함

public class ChatAnalysisResponse {
    private Long id;             // 분석 결과 id
    private AnalysisType type;   // GROUP or PRIVATE
    private String summary;      // 한 줄 요약
    private String resultJson;   // 상세 분석 결과(JSON)
    private String createdAt;
}
