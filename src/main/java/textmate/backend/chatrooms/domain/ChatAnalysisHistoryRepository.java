package textmate.backend.chatrooms.domain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatAnalysisHistoryRepository extends JpaRepository<ChatAnalysisHistory, Long> {
}