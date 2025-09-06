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
public class RelationshipDto {
    private String me;           // 나
    private String partner;      // 상대방
    private String analysis;     // 관계 분석
    private String solution;     // 해결 및 조언
    private List<String> strengths; // 강점
    private List<String> weaknesses; // 보완점
}
