package textmate.backend.chatgpt.api.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryAdviceDto {
    private List<String> strengths;
    private List<String> weaknesses;
    private String advice;   // 종합 조언
}