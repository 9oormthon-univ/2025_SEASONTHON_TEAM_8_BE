package textmate.backend.chatrooms.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import textmate.backend.chatrooms.api.dto.request.ChatAnalysisRequest;
import textmate.backend.chatrooms.api.dto.response.ChatAnalysisResponse;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;
import textmate.backend.chatrooms.domain.ChatAnalysisHistory;
import textmate.backend.chatrooms.domain.ChatAnalysisHistoryRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatAnalysisService {

    private final ChatAnalysisHistoryRepository repository;

    @Transactional
    public ChatAnalysisResponse analyze(ChatAnalysisRequest request) {
        // 1. 파일 읽기
        String rawText = extractText(request.getFile());

        // 2. 분석 로직 분기
        String resultJson;
        String summary;
        if (request.getChatRoomType() == ChatRoomType.GROUP) {
            resultJson = analyzeGroupChat(rawText);
            summary = "단체톡방 분석 완료";
        } else {
            resultJson = analyzePrivateChat(rawText);
            summary = "개인톡방 분석 완료";
        }

        // 3. DB 저장
        ChatAnalysisHistory entity = ChatAnalysisHistory.builder()
                .type(request.getChatRoomType())
                .rawText(rawText)
                .resultJson(resultJson)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(entity);

        // 4. Response 반환
        return ChatAnalysisResponse.builder()
                .id(entity.getId())
                .type(request.getChatRoomType())
                .summary(summary)
                .resultJson(resultJson)
                .createdAt(entity.getCreatedAt().toString())
                .build();
    }

    private String extractText(org.springframework.web.multipart.MultipartFile file) {
        try {
            return new String(file.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("파일 읽기 실패", e);
        }
    }

    private String analyzeGroupChat(String text) {
        // TODO: 단체톡방 분석 로직
        return "{ \"group\": \"분석결과\" }";
    }

    private String analyzePrivateChat(String text) {
        // TODO: 개인톡방 분석 로직
        return "{ \"private\": \"분석결과\" }";
    }
}