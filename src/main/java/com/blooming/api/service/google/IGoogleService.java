package com.blooming.api.service.google;

import com.blooming.api.entity.GoogleUser;
import com.blooming.api.entity.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface IGoogleService {
    User registerOAuth2User(OAuth2User oAuth2User);
    GoogleUser decryptGoogleToken(String googleToken);
}
