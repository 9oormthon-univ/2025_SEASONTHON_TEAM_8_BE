package textmate.backend.chat.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chat.api.dto.ChatAnalysisHistoryRes;
import textmate.backend.chat.api.dto.ChatAnalysisResponse;
import textmate.backend.chat.application.ChatAnalysisService;
import textmate.backend.user.domain.UserPrincipal;

import java.util.List;
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()") // 클래스 단위에서 인증 필수
public class ChatController {

    private final ChatAnalysisService chatAnalysisService;

    /**
     * 대화 파일 업로드 → 분석 → DB 저장
     */
    @PostMapping("/analyze")
    public ResponseEntity<ChatAnalysisResponse> analyze(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ChatAnalysisResponse response = chatAnalysisService.analyzeAndSave(principal.getUserId(), file);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 분석 내역 전체 조회
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatAnalysisHistoryRes>> myHistory(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ChatAnalysisHistoryRes> histories = chatAnalysisService.getUserHistories(principal.getUserId());
        return ResponseEntity.ok(histories);
    }

    /**
     * 분석 내역 단건 조회
     */
    @GetMapping("/history/{historyId}")
    public ResponseEntity<ChatAnalysisHistoryRes> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long historyId
    ) {
        ChatAnalysisHistoryRes history = chatAnalysisService.getHistoryDetail(principal.getUserId(), historyId);
        return ResponseEntity.ok(history);
    }
}