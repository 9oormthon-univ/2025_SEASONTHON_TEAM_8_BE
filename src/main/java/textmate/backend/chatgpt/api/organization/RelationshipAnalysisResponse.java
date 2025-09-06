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
public class RelationshipAnalysisResponse {
    private List<RelationshipDto> relationships;
    private SummaryAdviceDto summary;
}