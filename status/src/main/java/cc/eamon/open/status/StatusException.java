package cc.eamon.open.status;

public class StatusException extends Exception {
    private static final long serialVersionUID = 6922891378191357989L;

    private int statusCode;

    private String statusMsg;

    public StatusException(int statusCode) {
        super();
        this.setStatusCode(statusCode);
    }

    public StatusException(String errStatusMsg) {
        super();
        this.statusCode = StatusCode.getCode(errStatusMsg);
        this.statusMsg = StatusCode.getMsg(errStatusMsg);
    }

    public StatusException(int statusCode, String statusMsg) {
        super();
        this.statusCode = statusCode;
        this.statusMsg = statusMsg;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public static Status procExcp(Exception e) {
        if (e instanceof StatusException)
            return new Status(false, ((StatusException) e).getStatusCode(), ((StatusException) e).getStatusMsg(), 0);
        return new Status(false, 500, "Server Error", 0);
    }

}
