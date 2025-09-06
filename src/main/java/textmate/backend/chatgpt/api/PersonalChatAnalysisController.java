package textmate.backend.chatgpt.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatgpt.api.individual.PersonalAnalysisResponse;
import textmate.backend.chatgpt.application.PersonalChatAnalysisService;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

/**
 * 개인톡 분석 API
 * - POST /api/chat-analysis/personal
 * - form-data: file=카톡txt
 */
@Slf4j
@RestController
@RequestMapping("/api/personal/analysis")
@RequiredArgsConstructor
public class PersonalChatAnalysisController {

    private final PersonalChatAnalysisService service;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PersonalAnalysisResponse> analyzePersonal(
            @RequestPart("file") MultipartFile file   // 카톡 txt
    ) {
        try {
            log.info("개인 분석 요청 받음 - 파일명: {}", file.getOriginalFilename());

            if (file == null || file.isEmpty()) {
                log.warn("파일이 비어있음");
                return ResponseEntity.badRequest().build();
            }

            // ChatRoomType.PRIVATE 고정
            PersonalAnalysisResponse res = service.analyze(ChatRoomType.PRIVATE, file);
            log.info("개인 분석 완료");

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("개인 분석 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}