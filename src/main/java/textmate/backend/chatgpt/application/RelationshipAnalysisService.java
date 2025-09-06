package textmate.backend.chatgpt.application;


import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatgpt.api.organization.RelationshipAnalysisResponse;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelationshipAnalysisService {

    private final OpenAiService openAiService;

    public RelationshipAnalysisResponse analyze(ChatRoomType type, MultipartFile file) {
        String rawText = readFile(file);
        String truncated = truncate(rawText, 1000); // 최근 1000줄

        String prompt = buildPrompt(type, truncated);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini") // 비용 효율적인 모델
                .messages(List.of(
                        new ChatMessage("system", "너는 카톡 대화를 분석하는 상담사야. " +
                                "JSON 형식으로 관계분석, 해결 및 조언, 강점·보완점, 종합 조언을 만들어."),
                        new ChatMessage("user", prompt)
                ))
                .temperature(0.7)
                .maxTokens(1500)
                .build();

        var result = openAiService.createChatCompletion(request);
        ChatCompletionChoice choice = result.getChoices().get(0);

        String content = choice.getMessage().getContent();

        // TODO: content(JSON) → RelationshipAnalysisResponse 파싱
        // ex) new ObjectMapper().readValue(content, RelationshipAnalysisResponse.class);

        return RelationshipAnalysisResponse.builder()
                .summary(null) // 파싱 후 채워넣기
                .relationships(List.of())
                .build();
    }

    private String readFile(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("파일 읽기 실패", e);
        }
    }

    private String truncate(String text, int lines) {
        String[] arr = text.split("\n");
        int start = Math.max(arr.length - lines, 0);
        return String.join("\n", List.of(arr).subList(start, arr.length));
    }

    private String buildPrompt(ChatRoomType type, String conversation) {
        String scope = (type == ChatRoomType.GROUP) ? "단체톡방" : "개인톡방";
        return """
      다음은 %s 카카오톡 대화의 최근 일부다.
      아래 JSON 스키마에 '정확히' 맞춰 결과만 출력해라. 코드펜스(```), 불필요한 텍스트 금지.

      {
        "relationshipSection": {
          "title": "당신과의 관계는?",
          "items": [
            {
              "pairTitle": "나 & 상대방",          // 예: "정수경 & 000"
              "oneLine": "두 사람의 관계를 한 줄로 요약",
              "partnerPersona": "상대방은 어떤 사람(대화 습관/톤/에너지)이다",
              "perceivedAs": "그래서 때때로 ~처럼 보일 때가 있다",
              "solution": "이 관계가 더 좋아지도록 내가 취할 행동/말투/빈도에 대한 구체 조언",
              "exampleLines": ["예시 멘트1","예시 멘트2"]  // 1~3개
            }
          ],
          "summary": {
            "strengths": ["강점1","강점2"],
            "weaknesses": ["보완점1","보완점2"],
            "advice": "종합 조언"
          }
        }
      }

      --- 분석 시작 ---
      %s
      --- 분석 종료 ---
      """.formatted(scope, conversation);
    }
}