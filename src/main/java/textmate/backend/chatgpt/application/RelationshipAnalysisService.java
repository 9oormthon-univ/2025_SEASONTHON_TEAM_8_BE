package textmate.backend.chatgpt.application;


import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatgpt.api.dto.RelationshipAnalysisResponse;
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
        return """
                카톡 대화 로그입니다. 최근 %s 대화에 대해 분석해주세요.
                반드시 JSON 형식으로 출력하세요.
                {
                  "relationships": [
                    {
                      "me": "사용자 이름",
                      "partner": "상대방 이름",
                      "analysis": "관계 분석",
                      "solution": "해결 및 조언",
                      "strengths": ["..."],
                      "weaknesses": ["..."]
                    }
                  ],
                  "summary": {
                    "strengths": ["..."],
                    "weaknesses": ["..."],
                    "advice": "종합 조언"
                  }
                }
                
                대화:
                %s
                """.formatted(type == ChatRoomType.GROUP ? "단체톡방" : "개인톡방", conversation);
    }
}