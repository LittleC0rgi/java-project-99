package hexlet.code.component;

import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserPermissionChecker {

    @Autowired
    private UserRepository userRepository;

    public boolean isOwner(Long id, Authentication authentication) {
        var currentUsername = authentication.getName();
        return userRepository.findById(id)
                .map(user -> user.getUsername().equals(currentUsername))
                .orElse(false);
    }
}
