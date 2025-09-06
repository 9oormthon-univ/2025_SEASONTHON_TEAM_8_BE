package textmate.backend.chatrooms.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatrooms.api.dto.response.ChatAnalysisResponse;
import textmate.backend.chatrooms.api.dto.request.ChatAnalysisRequest;
import textmate.backend.chatrooms.application.ChatAnalysisService;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

@RestController
@RequestMapping("/api/chat/analysis")
@RequiredArgsConstructor
public class ChatAnalysisController {

    private final ChatAnalysisService chatAnalysisService;

    //카톡 대화 분석 요청 (단체톡방/개인톡방 선택)
    //방생성
    @PostMapping
    public ResponseEntity<ChatAnalysisResponse> analyzeChat(
            @RequestParam("chatRoomType") ChatRoomType chatRoomType,
            @RequestParam("file") MultipartFile file
    ) {
        ChatAnalysisRequest request = new ChatAnalysisRequest();
        request.setFile(file);
        request.setChatRoomType(chatRoomType);

        ChatAnalysisResponse response = chatAnalysisService.analyze(request);
        return ResponseEntity.ok(response);
    }
}