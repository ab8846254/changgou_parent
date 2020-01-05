package Exception;

import entity.Result;
import entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 */
@ControllerAdvice
public class MyException {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result Error(Exception e) {
        return new Result(false, StatusCode.ERROR,e.getMessage());
    }
}
