package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

import java.io.UnsupportedEncodingException;

public interface UserService {
    AuthToken login(String username, String password, String clientId, String clientSecret) throws UnsupportedEncodingException;
}
