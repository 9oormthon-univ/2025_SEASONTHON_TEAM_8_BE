package textmate.backend.chatgpt.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import textmate.backend.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface ChatAnalysisHistoryRepository extends JpaRepository<ChatAnalysisHistory, Long> {
    List<ChatAnalysisHistory> findByUserOrderByCreatedAtDesc(User user);
    Optional<ChatAnalysisHistory> findByIdAndUser(Long id, User user);
}
