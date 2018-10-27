package cc.eamon.open.status;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Log4j2
@ControllerAdvice
public abstract class StatusBaseController {

    /**
     * Status异常处理
     */
    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public Status statusExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e){
        if (e instanceof StatusException){
            return StatusException.procExcp(e);
        }
        log.error(e.getMessage());
        return new Status(false, 500, "Server Error", "Reading Log For Details");
    }


}
