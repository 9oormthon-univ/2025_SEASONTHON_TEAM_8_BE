package textmate.backend.chatgpt.api.individual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 개인톡 분석 응답 - 6개 섹션으로 구성 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalAnalysisResponse {
    private RelationshipSection relationship;   // 당신과의 관계(관계 분석 & 해결 및 조언)
    private CharacterSection character;         // 캐릭터 분석(나/상대방)
    private SpeechStyleSection speechStyle;     // 당신의 말투는?
    private FriendshipSection friendship;       // 우리의 우정은?
    private PatternInsightsSection patternInsights; // 대화 패턴 인사이트
    private GrowthGuideSection growthGuide;     // 관계 성장 가이드
}