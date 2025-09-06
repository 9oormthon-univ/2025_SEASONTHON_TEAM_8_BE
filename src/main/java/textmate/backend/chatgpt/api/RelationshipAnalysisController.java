package textmate.backend.chatgpt.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatgpt.api.dto.RelationshipAnalysisResponse;
import textmate.backend.chatgpt.application.RelationshipAnalysisService;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

@RestController
@RequestMapping("/api/chat-analysis/relationship")
@RequiredArgsConstructor
public class RelationshipAnalysisController {

    private final RelationshipAnalysisService service;

    @PostMapping
    public ResponseEntity<RelationshipAnalysisResponse> analyze(
            @RequestParam("type") ChatRoomType type,        // GROUP | PRIVATE
            @RequestParam("file") MultipartFile file        // 카톡 txt
    ) {
        return ResponseEntity.ok(service.analyze(type, file));
    }
}