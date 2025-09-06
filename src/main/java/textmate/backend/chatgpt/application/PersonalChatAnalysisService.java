package textmate.backend.chatgpt.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatgpt.api.individual.PersonalAnalysisResponse;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 개인톡 분석 서비스
 * - 파일(txt) 읽기 → 최근 라인만 유지 → OpenAI 호출 → JSON 파싱
 * - 섹션: 관계/캐릭터/말투/우정/패턴/성장가이드
 */
@Service
@RequiredArgsConstructor
public class PersonalChatAnalysisService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param type ChatRoomType.PRIVATE 이어야 의미 있음(유연성 위해 인자 유지)
     * @param file 카카오톡 txt
     */
    public PersonalAnalysisResponse analyze(ChatRoomType type, MultipartFile file) {
        String raw = readFile(file);
        String truncated = keepLastLines(raw, 1000);

        ChatCompletionRequest req = ChatCompletionRequest.builder()
                .model("gpt-4o-mini") // 비용 효율/속도 균형
                .messages(List.of(
                        new ChatMessage("system",
                                "너는 카카오톡 개인 대화를 분석하는 상담가다. " +
                                        "반드시 JSON만 출력한다. 코드펜스(```)나 불필요한 접두/접미 텍스트를 붙이지 마라."),
                        new ChatMessage("user", buildPrompt(truncated))
                ))
                .temperature(0.7)
                .maxTokens(1800)
                .build();

        ChatCompletionResult res = openAiService.createChatCompletion(req);
        String content = res.getChoices().get(0).getMessage().getContent();
        String json = stripCodeFence(content);

        try {
            return objectMapper.readValue(json, PersonalAnalysisResponse.class);
        } catch (Exception e) {
            // 파싱 실패 시, 비어 있는 형태로 반환하여 프론트 깨짐 방지
            return PersonalAnalysisResponse.builder().build();
        }
    }

    // --------- helpers ---------

    private String readFile(MultipartFile file) {
        try { return new String(file.getBytes(), StandardCharsets.UTF_8); }
        catch (Exception e) { throw new RuntimeException("파일 읽기 실패", e); }
    }

    private String keepLastLines(String text, int lines) {
        String[] arr = text.split("\n");
        int start = Math.max(0, arr.length - lines);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < arr.length; i++) sb.append(arr[i]).append('\n');
        return sb.toString().trim();
    }

    private String stripCodeFence(String s) {
        if (s == null) return "";
        Pattern p = Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL);
        Matcher m = p.matcher(s);
        return m.find() ? m.group(1) : s.trim();
    }

    /**
     * 모델이 정확히 이 스키마로 생성하도록 강제
     */
    private String buildPrompt(String conversation) {
        return """
            다음은 카카오톡 '개인톡' 대화의 최근 일부다.
            아래 JSON 스키마에 정확히 맞춰 결과만 출력하라.
            이름을 추론하되 확실하지 않으면 "나", "상대방"으로 표기해라.

            {
              "relationship": {
                "title": "당신과의 관계는?",
                "pairTitle": "나 & 상대방",
                "analysis": "두 사람의 관계를 한 줄 또는 한 단락으로 요약",
                "solution": "관계를 개선/유지하기 위한 구체적 조언 한 단락",
                "exampleLines": ["예시 멘트1","예시 멘트2"]
              },
              "character": {
                "title": "캐릭터 분석",
                "me": {
                  "name": "나",
                  "role": "역할/별칭",
                  "description": "한 줄 설명",
                  "strengths": ["강점1","강점2"],
                  "weaknesses": ["보완점1","보완점2"],
                  "advice": "개인 조언"
                },
                "partner": {
                  "name": "상대방",
                  "role": "역할/별칭",
                  "description": "한 줄 설명",
                  "strengths": ["강점1","강점2"],
                  "weaknesses": ["보완점1","보완점2"],
                  "advice": "개인 조언"
                }
              },
              "speechStyle": {
                "title": "당신의 말투는?",
                "narrative": "말투 전반 설명(문장 길이/감정 표현/질문 스타일/톤 등)",
                "strengths": ["강점1","강점2"],
                "weaknesses": ["보완점1","보완점2"],
                "advice": "말투 개선 팁"
              },
              "friendship": {
                "title": "우리의 우정은?",
                "narrative": "서로의 친밀감/신뢰/분위기/가치 공유를 설명"
              },
              "patternInsights": {
                "title": "대화 패턴 인사이트",
                "bullets": [
                  "시간대, 반응 속도/비율, 사진/이모지 사용, 주제 확장/집중 습관 등"
                ]
              },
              "growthGuide": {
                "title": "관계 성장 가이드",
                "bullets": [
                  "실행 가능한 구체 행동/말투/주제 선택 가이드 3~6개"
                ]
              }
            }

            --- 분석 시작 ---
            %s
            --- 분석 끝 ---
            """.formatted(conversation);
    }
}