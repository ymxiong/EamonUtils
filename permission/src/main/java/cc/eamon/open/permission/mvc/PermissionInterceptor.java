package cc.eamon.open.permission.mvc;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cc.eamon.open.permission.annotation.Permission;
import cc.eamon.open.permission.annotation.PermissionLimit;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import cc.eamon.open.status.Status;
import cc.eamon.open.status.StatusCode;


/**
 * @author Eamon
 * ʕ•ﻌ•ʔ 留给小纯洁的ajax权限header
 */
public class PermissionInterceptor implements HandlerInterceptor {

    private PermissionChecker checker;

    public PermissionChecker getChecker() {
        return checker;
    }

    public void setChecker(PermissionChecker checker) {
        this.checker = checker;
    }

    public PermissionInterceptor(PermissionChecker checker) {
        this.checker = checker;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", "*");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With, X-File-Name");
        return roleControl(req, resp, handler);
    }

    /**
     * 角色权限控制访问
     */
    private boolean roleControl(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            // Object target = hm.getBean();
            Class<?> clazz = hm.getBeanType();
            Method m = hm.getMethod();
            try {
                if (clazz != null && m != null) {
                    boolean isClzAnnotation = clazz.isAnnotationPresent(Permission.class);
                    boolean isMethodAnnotation = m.isAnnotationPresent(PermissionLimit.class);

                    Permission pc;
                    PermissionLimit rc;
                    String methodPermissionName;

                    if (isClzAnnotation){
                        pc = clazz.getAnnotation(Permission.class);
                        methodPermissionName = pc.value().toLowerCase();
                    }else {
                        return true;
                    }

                    methodPermissionName += "_";

                    if (isMethodAnnotation) {
                        rc = m.getAnnotation(PermissionLimit.class);
                        if (rc.name().equals("")){
                            methodPermissionName += m.getName().toLowerCase();
                        }else {
                            methodPermissionName += rc.name().toLowerCase();
                        }
                    }else {
                        return true;
                    }

                    if (!checker.check(request, response, methodPermissionName, rc.value())){
                        throw new Exception();
                    }
                }
            } catch (Exception e) {
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/json; charset=utf-8");
                try {
                    response.getWriter().write(
                            new Status(false,
                                    StatusCode.getCode("PERMISSION_LOW"),
                                    0,
                                    0).toJson());


                } catch (IOException e1) {
                    e1.printStackTrace();
                    response.setStatus(StatusCode.getCode("PERMISSION_LOW"));
                    return false;
                }
                response.setStatus(StatusCode.getCode("PERMISSION_LOW"));
                return false;
            }
        }

        return true;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // TODO Auto-generated method stub

    }

}
