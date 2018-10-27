package cc.eamon.open.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Status {
    /**
     * 状态值
     */
    private boolean result;

    /**
     * 状态码
     */
    private int statusCode;

    /**
     * 需要让用户知道的数据
     */
    private Object data;

    /**
     * 加密验证数据（保护内部逻辑）
     */
    private Object extra;


    public Status(boolean result, String errorMsg) {
        this.result = result;
        this.statusCode = StatusCode.getCode(errorMsg);
        this.data = StatusCode.getMsg(errorMsg);
        this.extra = 0;
    }

    public Status(boolean result, int statusCode, Object data) {
        super();
        this.result = result;
        this.statusCode = statusCode;
        this.data = data;
        this.extra = 0;
    }


    public String toJson() {
        return "{\"result\":" + result + ",'\"statusCode\":" + statusCode + ",\"data\":" + data + ",\"extra\":" + extra + "}";
    }


}
