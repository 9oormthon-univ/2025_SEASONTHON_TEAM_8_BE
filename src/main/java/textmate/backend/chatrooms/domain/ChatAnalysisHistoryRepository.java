package textmate.backend.chatrooms.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatAnalysisHistoryRepository extends JpaRepository<ChatAnalysisHistory, Long> {
}