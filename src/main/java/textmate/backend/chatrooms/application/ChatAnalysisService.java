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
import textmate.backend.user.domain.User;
import textmate.backend.user.domain.repository.UserRepository;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ChatAnalysisService.class);
    private final ChatAnalysisHistoryRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public ChatAnalysisResponse analyze(ChatAnalysisRequest request) {
        try {
            log.info("채팅 분석 요청 시작 - 타입: {}", request.getChatRoomType());
            
            // 1. 파일 읽기
            String rawText = extractText(request.getFile());
            log.info("파일 읽기 완료 - 텍스트 길이: {}", rawText.length());

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

            // 3. 기본 사용자 조회 (임시로 userId=1 사용)
            User user = userRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId: 1"));

            // 4. DB 저장
            ChatAnalysisHistory history = ChatAnalysisHistory.builder()
                    .type(request.getChatRoomType())
                    .rawText(rawText)
                    .resultJson(resultJson)
                    .createdAt(LocalDateTime.now())
                    .user(user)
                    .build();

            history = repository.save(history);
            log.info("분석 결과 저장 완료 - ID: {}", history.getId());

            // 5. Response 반환
            return ChatAnalysisResponse.builder()
                    .id(history.getId())
                    .type(request.getChatRoomType())
                    .summary(summary)
                    .resultJson(resultJson)
                    .createdAt(history.getCreatedAt().toString())
                    .build();
                    
        } catch (Exception e) {
            log.error("채팅 분석 중 오류 발생", e);
            throw new RuntimeException("채팅 분석 실패: " + e.getMessage(), e);
        }
    }

    private String extractText(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
            }
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (text.contains("�")) { // 깨짐 감지 시
                text = new String(file.getBytes(), Charset.forName("MS949"));
            }
            return text;
        } catch (Exception e) {
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