package textmate.backend.chat.application;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chat.api.dto.character.GptResult;
import textmate.backend.chat.domain.ChatAnalysisHistoryRepository;
import textmate.backend.chat.domain.RelationshipType;
import textmate.backend.user.domain.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
@Service
@RequiredArgsConstructor

public class ChatAnalysisService {

    private final ChatAnalysisHistoryRepository repo;
    private final UserRepository userRepo;
    private final OpenAiService openAiService;
    private final ObjectMapper om = new ObjectMapper();

    /** 파일 끝에서 N줄 읽기 */
    private String readTail(MultipartFile file, int lines) {
        try {
            String all = new String(file.getBytes(), StandardCharsets.UTF_8);
            String[] arr = all.split("\\R");
            int start = Math.max(0, arr.length - lines);
            return String.join("\n", Arrays.asList(arr).subList(start, arr.length));
        } catch (Exception e) {
            throw new RuntimeException("파일 읽기 실패", e);
        }
    }

    /** GPT 호출 → JSON 파싱 → GptResult 반환 */
    private GptResult callGpt(String content) {
        if (content.length() > 12000) {
            content = content.substring(content.length() - 12000);
        }

        String prompt = """
            너는 대화 분석 전문가다.
            아래 대화를 분석하고 **오직 JSON만** 출력하라.

            출력 JSON 스키마:
            {
              "type": "LOVER | FRIEND | WORK | FAMILY | OTHER",
              "summary": "핵심 내용 2~3문장 요약",
              "messageCount": <number>
            }

            대화:
            %s
            """.formatted(content);

        var req = CompletionRequest.builder()
                .model("gpt-4o-mini")
                .prompt(prompt + "\n\nJSON만 출력.")
                .maxTokens(600)
                .temperature(0.3)
                .build();

        var raw = openAiService.createCompletion(req)
                .getChoices().get(0).getText().trim();

        String json = sanitizeToJson(raw);

        try {
            JsonNode n = om.readTree(json);

            String type = n.path("type").asText("OTHER");
            String summary = n.path("summary").asText("");
            int count = n.path("messageCount").asInt(0);

            return new GptResult(type, summary, count, json);
        } catch (Exception e) {
            return new GptResult("OTHER", "(분석 결과 파싱 실패) " + raw, 0, raw);
        }
    }

    /** 백틱/잡음 제거해서 순수 JSON만 남기기 */
    private String sanitizeToJson(String raw) {
        String s = raw.trim();
        if (s.startsWith("```")) {
            int i1 = s.indexOf('{');
            int i2 = s.lastIndexOf('}');
            if (i1 >= 0 && i2 > i1) s = s.substring(i1, i2 + 1);
        }
        if (!s.startsWith("{")) {
            int i = s.indexOf('{');
            if (i >= 0) s = s.substring(i);
        }
        if (!s.endsWith("}")) {
            int j = s.lastIndexOf('}');
            if (j >= 0) s = s.substring(0, j + 1);
        }
        return s;
    }

    /** LLM이 준 type 문자열 → enum 매핑 */
    private RelationshipType mapType(String t) {
        if (t == null) return RelationshipType.OTHER;
        String x = t.trim().toUpperCase();
        return switch (x) {
            case "LOVER", "연인", "연인관계" -> RelationshipType.LOVER;
            case "FRIEND", "친구", "친구관계" -> RelationshipType.FRIEND;
            case "WORK", "직장", "직장관계" -> RelationshipType.WORK;
            case "FAMILY", "가족", "가족관계" -> RelationshipType.FAMILY;
            default -> RelationshipType.OTHER;
        };
    }
}