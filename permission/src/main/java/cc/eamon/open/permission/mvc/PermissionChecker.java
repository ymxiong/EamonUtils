package cc.eamon.open.permission.mvc;

import cc.eamon.open.status.StatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PermissionChecker {
	
	boolean check(HttpServletRequest request, HttpServletResponse response, String methodName, String roleLimit) throws StatusException;
	
}
