package textmate.backend.chatgpt.api.individual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public @Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SpeechStyleSection {
    private String title;         // "당신의 말투는?"
    private String narrative;     // 전체 서술
    private List<String> strengths;
    private List<String> weaknesses;
    private String advice;        // 개선 팁
}