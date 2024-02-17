package com.bx.api.bean.filter;

import com.bx.api.bean.service.SessionService;
import com.bx.api.common.contant.RedisKey;
import com.bx.api.domain.dto.session.UserSession;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.filter.Filter;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.server.Request;
import io.github.stylesmile.server.Response;
import io.github.stylesmile.tool.StringUtil;

import java.util.HashMap;

@Service
public class TokenFilter implements Filter {
    private static HashMap<String, String> FILTER_MAP = new HashMap<String, String>() {{
        put("/login", "1");
    }};


    @AutoWired
    JedisTemplate jedisTemplate;

    @Override
    public boolean preHandle(Request request, Response response) {
        String str = FILTER_MAP.get(request.getURI().toString());
        if (StringUtil.isNotEmpty(str)) {
            return true;
        }
        String token = request.getHeaders().get("accessToken");
        UserSession userSession = jedisTemplate.getSerializeData(
                String.format(RedisKey.Login.USER_SESSION, token),
                UserSession.class);
        if (userSession != null) {
            SessionService.setSession(userSession);
            return true;
        }
        return false;
    }

    @Override
    public boolean afterCompletion(Request request, Response response) {
        return true;
    }
}
