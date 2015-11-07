package jibiki.fr.shishito.Util;

import java.io.Serializable;

/**
 * Created by tibo on 05/04/15.
 */
public class HTTPResult {
    private int httpCode;
    private String content;

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}