package textmate.backend.chatgpt.api.individual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class FriendshipSection {
    private String title;     // "우리의 우정은?"
    private String narrative; // 서술형 분석
}
