package taint.analysis;

public class HttpServletRequest {

    public String getParameter(String string) {
        return "Real library returns the actual request parameter";
    }

}