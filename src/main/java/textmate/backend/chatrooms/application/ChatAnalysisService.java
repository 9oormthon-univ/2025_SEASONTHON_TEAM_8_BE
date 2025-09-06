package textmate.backend.chatrooms.application;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatrooms.api.dto.request.ChatAnalysisRequest;
import textmate.backend.chatrooms.api.dto.response.ChatAnalysisResponse;
import textmate.backend.chatrooms.domain.ChatAnalysisHistory;
import textmate.backend.chatrooms.domain.ChatAnalysisHistoryRepository;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ChatAnalysisService.class);
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

    private String extractText(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
            }

            // 1차 시도: UTF-8
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);

            // 혹시 깨진다면 EUC-KR(MS949)로 다시 시도 가능
            if (text.contains("�")) {
                log.warn("텍스트 깨짐 감지, MS949로 재시도합니다.");
                text = new String(file.getBytes(), Charset.forName("MS949"));
            }

            log.info("업로드된 파일명: {}, 크기: {} bytes", file.getOriginalFilename(), file.getSize());
            return text;

        } catch (Exception e) {
            log.error("파일 읽기 실패", e);
            throw new RuntimeException("파일 읽기 실패: " + e.getMessage(), e);
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