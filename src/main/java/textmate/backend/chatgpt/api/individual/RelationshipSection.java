package textmate.backend.chatgpt.api.individual;

import java.util.List;

public class RelationshipSection {
    private String title;              // "당신과의 관계는?"
    private String pairTitle;          // "나 & 김민수" 형태 (예: "정수경 & 000")
    private String analysis;           // 관계 분석 (자연어)
    private String solution;           // 해결 및 조언 (자연어)
    private List<String> exampleLines; // 예시 멘트 1~3개
}