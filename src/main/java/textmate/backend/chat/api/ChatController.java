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
public class ChatController {

    private final ChatAnalysisService chatAnalysisService;

    @PostMapping("/analyze")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatAnalysisResponse> analyze(
            @AuthenticationPrincipal UserPrincipal principal, // 구글 로그인 후 발급된 사용자
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                chatAnalysisService.analyzeAndSave(principal.getUserId(), file)
        );
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatAnalysisHistoryRes>> myHistory(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(chatAnalysisService.getUserHistories(principal.getUserId()));
    }

    @GetMapping("/history/{historyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatAnalysisHistoryRes> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long historyId
    ) {
        return ResponseEntity.ok(chatAnalysisService.getHistoryDetail(principal.getUserId(), historyId));
    }
}
