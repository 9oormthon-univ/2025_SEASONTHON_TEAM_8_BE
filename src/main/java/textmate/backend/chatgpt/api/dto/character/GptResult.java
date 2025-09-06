package textmate.backend.chatgpt.api.dto.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GptResult {
    private String type;
    private String summary;
    private int messageCount;
    private String rawJson;
}