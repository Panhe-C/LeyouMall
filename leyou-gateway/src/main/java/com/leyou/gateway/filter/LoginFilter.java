package com.leyou.gateway.filter;

import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@Component
@EnableConfigurationProperties({JwtProperties.class,FilterProperties.class})
public class LoginFilter extends ZuulFilter {


    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;


    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();

        //获取request
        HttpServletRequest request = context.getRequest();

        //获取url路径
        String url = request.getRequestURL().toString();

        //判断白名单
        List<String> allowPaths = this.filterProperties.getAllowPaths();

        for (String allowPath : allowPaths) {
            if(StringUtils.contains(url,allowPath)){
                return false;
            }
        }

        return true;
    }

    @Override
    public Object run() throws ZuulException {

        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();

        //获取request
        HttpServletRequest request = context.getRequest();

        //获取cookie中的值token
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());


        /*if(StringUtils.isBlank(token)){
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }*///可以省略，但使用后效率会更高些，如果为空就不用再进行后面的过程

        //校验
        try {
            //校验通过，则什么都不做，放行
            JwtUtils.getInfoFromToken(token,this.jwtProperties.getPublicKey());
        } catch (Exception e) {

            //校验出现异常返回403
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
//            logger.error("非法访问，未登录，地址：{}", request.getRemoteHost(), e );
        }

        return null;
    }
}
