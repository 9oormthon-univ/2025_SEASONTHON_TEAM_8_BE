package textmate.backend.chatrooms.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

@Getter
@Setter
@NoArgsConstructor
// 카톡 대화 분석 요청
// 사용자가 올린 카카오톡 txt와 type 전달
public class ChatAnalysisRequest {
    private ChatRoomType chatRoomType;   // GROUP or PRIVATE
    private MultipartFile file;          // 카카오톡 txt 파일
}