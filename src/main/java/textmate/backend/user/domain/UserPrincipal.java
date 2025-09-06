package textmate.backend.user.domain;


import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {
    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 예: User 엔티티에 role 필드가 있다고 가정
        // return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));

        // 권한 정보를 아직 안 쓴다면 일단 빈 리스트 반환
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // User 엔티티의 password
    }

    public Long getUserId() {
        return user.getUserId();
    }
    
    public Long getId() {
        return user.getUserId();
    }

    @Override
    public String getUsername() {
        return user.getName(); // User 엔티티의 username (또는 email)
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
