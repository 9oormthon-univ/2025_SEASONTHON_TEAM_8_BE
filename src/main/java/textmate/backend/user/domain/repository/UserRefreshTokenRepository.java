package textmate.backend.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import textmate.backend.user.domain.User;
import textmate.backend.user.domain.UserRefreshToken;

import java.util.Optional;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
    @Modifying(clearAutomatically = true)
    @Query("delete from UserRefreshToken urt where urt.user = :user")
    void deleteByUserImmediate(@Param("user") User user);
    void deleteByUser(User user);
    Optional<UserRefreshToken> findByUser_UserId(Long userId);
}