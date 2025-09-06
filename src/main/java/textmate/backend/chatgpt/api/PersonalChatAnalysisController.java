package textmate.backend.chatgpt.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatgpt.api.individual.PersonalAnalysisResponse;
import textmate.backend.chatgpt.application.PersonalChatAnalysisService;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

/**
 * 개인톡 분석 API
 * - POST /api/chat-analysis/personal
 * - form-data: file=카톡txt
 */
@RestController
@RequestMapping("/api/chat-analysis/personal")
@RequiredArgsConstructor
public class PersonalChatAnalysisController {

    private final PersonalChatAnalysisService service;

    @PostMapping
    public ResponseEntity<PersonalAnalysisResponse> analyzePersonal(
            @RequestParam("file") MultipartFile file
    ) {
        // ChatRoomType.PRIVATE 로 분석
        PersonalAnalysisResponse res = service.analyze(ChatRoomType.PRIVATE, file);
        return ResponseEntity.ok(res);
    }
}