package vn.hoidanit.laptopshop.service.userInfo;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.Role;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.UserService;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // call api
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // get provider
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // Process oAuth2User or map it to your local user database
        String email = registrationId.equals("github") ? (String) attributes.get("id")
                : (String) attributes.get("email");
        String fullName = (String) attributes.get("name");
        Role userRole = this.userService.getRoleByName("USER");
        if (email != null) {
            User user = this.userService.getUserByEmail(email);
            if (user == null) {
                User OUser = new User();
                OUser.setEmail(email);
                OUser.setAvatar("default-google.png");
                OUser.setFullName(fullName);
                OUser.setProvider(registrationId.equalsIgnoreCase("github") ? "GITHUB" : "GOOGLE");
                OUser.setPassword(null);
                OUser.setRole(userRole);
                this.userService.saveUser(OUser);

            }
        }

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole.getName())),
                oAuth2User.getAttributes(),
                registrationId.equalsIgnoreCase("github") ? "id" : "email");
    }

}