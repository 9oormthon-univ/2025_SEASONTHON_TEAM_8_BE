package textmate.backend.chat.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chat.api.dto.character.Diagnosis;
import textmate.backend.chat.api.dto.character.GroupAnalysisResponse;
import textmate.backend.chat.api.dto.character.ParticipantCard;
import textmate.backend.chat.api.dto.character.TopContributor;
import textmate.backend.chat.domain.ChatAnalysisHistory;
import textmate.backend.chat.domain.ChatAnalysisHistoryRepository;
import textmate.backend.chat.domain.RelationshipType;
import textmate.backend.user.domain.User;
import textmate.backend.user.domain.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatAnalysisService {

    private static final int MAX_LINES = 500;

    private final OpenAiService openAiService;
    private final ChatAnalysisHistoryRepository historyRepo;
    private final UserRepository userRepo;
    private final ObjectMapper om = new ObjectMapper();

    @Transactional
    public GroupAnalysisResponse analyzeAndSave(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 1) 최근 500줄 파싱
        List<String> tail = readTailLines(file, MAX_LINES);
        List<Msg> msgs = parseChat(tail);
        if (msgs.isEmpty()) {
            throw new IllegalArgumentException("분석 가능한 메시지가 없습니다.");
        }

        // 2) 기초 통계
        int total = msgs.size();
        Map<String, Integer> countBySender = new LinkedHashMap<>();
        msgs.forEach(m -> countBySender.merge(m.sender(), 1, Integer::sum));

        // 3) LLM 프롬프트 (JSON 강제)
        String compact = compactForLLM(msgs, Math.min(total, 350));
        String prompt = """
            너는 단체 채팅방 대화 분석 전문가다.
            아래 대화(최근 500줄 이내)를 분석해서 **반드시 JSON만** 출력해라.

            출력 JSON:
            {
              "summaryOfContents": "문단 요약",
              "participants": [
                {"name":"", "messageCount":0, "participation":0.0, "persona":"", "sentiment":"POSITIVE|NEGATIVE|NEUTRAL"}
              ],
              "topContributor": {"name":"", "messageCount":0, "brief":""},
              "diagnosis": {"relationInsight":"", "actions":"", "context":""},
              "oneLineSummary": ["", ""]
            }

            참고 통계(발화자별 메시지 수): %s
            대화(일부 축약):
            %s
            """.formatted(om.valueToTree(countBySender), compact);

        String json = callGptJson(prompt);
        json = sanitizeToJson(json);

        JsonNode root;
        try {
            root = om.readTree(json);
        } catch (Exception e) {
            log.error("LLM JSON 파싱 실패. 원문: {}", json);
            throw new IllegalStateException("분석 결과 파싱 실패", e);
        }

        // 4) DB 저장
        ChatAnalysisHistory history = new ChatAnalysisHistory(
                user,
                RelationshipType.OTHER, // 그룹방 기본값
                root.path("summaryOfContents").asText(),
                total,
                json
        );
        historyRepo.save(history);

        // 5) 응답 DTO
        return GroupAnalysisResponse.builder()
                .historyId(history.getId())
                .analyzedCount(total)
                .summaryOfContents(root.path("summaryOfContents").asText())
                .participants(toParticipants(root.path("participants")))
                .topContributor(toTopContributor(root.path("topContributor")))
                .diagnosis(toDiagnosis(root.path("diagnosis")))
                .oneLineSummary(toListOfString(root.path("oneLineSummary")))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ===== Helpers =====

    private List<String> readTailLines(MultipartFile file, int max) {
        String all;
        try {
            all = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("파일 읽기 실패", e);
        }
        String[] lines = all.split("\\R");
        int start = Math.max(0, lines.length - max);
        return Arrays.asList(lines).subList(start, lines.length);
    }

    record Msg(String sender, String text) {}

    private List<Msg> parseChat(List<String> lines) {
        List<Msg> out = new ArrayList<>();
        Pattern p1 = Pattern.compile("^\\[(.*?)\\]\\s*(.*?):\\s*(.*)$"); // [yyyy.MM.dd HH:mm] 이름: 내용
        Pattern p2 = Pattern.compile("^(.*?):\\s*(.+)$");               // 이름: 내용
        for (String l : lines) {
            String line = l.trim();
            if (line.isEmpty()) continue;
            Matcher m1 = p1.matcher(line);
            Matcher m2 = p2.matcher(line);
            if (m1.find()) out.add(new Msg(m1.group(2), m1.group(3)));
            else if (m2.find()) out.add(new Msg(m2.group(1), m2.group(2)));
        }
        return out;
    }

    private String compactForLLM(List<Msg> msgs, int maxRows) {
        int from = Math.max(0, msgs.size() - maxRows);
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < msgs.size(); i++) {
            Msg m = msgs.get(i);
            sb.append(m.sender()).append(": ").append(m.text()).append('\n');
        }
        return sb.toString();
    }

    private String callGptJson(String prompt) {
        var req = CompletionRequest.builder()
                .model("gpt-4o-mini")
                .prompt(prompt + "\n\n오직 JSON만 출력.")
                .maxTokens(900)
                .temperature(0.4)
                .build();
        var res = openAiService.createCompletion(req);
        return res.getChoices().get(0).getText().trim();
    }

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

    private List<ParticipantCard> toParticipants(JsonNode arr) {
        if (!arr.isArray()) return List.of();
        List<ParticipantCard> list = new ArrayList<>();
        arr.forEach(n -> list.add(ParticipantCard.builder()
                .name(n.path("name").asText(null))
                .messageCount(n.path("messageCount").asInt(0))
                .participation(n.path("participation").asDouble(0.0))
                .persona(n.path("persona").asText(null))
                .sentiment(n.path("sentiment").asText(null))
                .build()));
        return list;
    }

    private TopContributor toTopContributor(JsonNode n) {
        return TopContributor.builder()
                .name(n.path("name").asText(null))
                .messageCount(n.path("messageCount").asInt(0))
                .brief(n.path("brief").asText(null))
                .build();
    }

    private Diagnosis toDiagnosis(JsonNode n) {
        return Diagnosis.builder()
                .relationInsight(n.path("relationInsight").asText(null))
                .actions(n.path("actions").asText(null))
                .context(n.path("context").asText(null))
                .build();
    }

    private List<String> toListOfString(JsonNode arr) {
        if (!arr.isArray()) return List.of();
        List<String> list = new ArrayList<>();
        arr.forEach(n -> list.add(n.asText()));
        return list;
    }
}