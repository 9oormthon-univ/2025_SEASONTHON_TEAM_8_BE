package textmate.backend.chatgpt.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChatAnalysisRequest {
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
}