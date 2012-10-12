package net.medcommons.router.web.stripes;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.StringReader;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import net.medcommons.modules.utils.Str;
import net.sourceforge.stripes.action.StreamingResolution;

/**
 * A convenience class that makes it easy to return a JSON
 * result from an action
 * 
 * @author ssadedin
 */
public class JSONResolution extends StreamingResolution {
    
    JSONObject result = new JSONObject();

    public JSONResolution() {
        this("application/json");
    }
    
    /**
     * The content type can be passed explicitly if you want to use something other than 
     * text/json.  The main reason you want to do that is if you are relying on the 
     * browser at the other end rendering it to a DOM node in which case 
     * text/plain is useful.
     */
    public JSONResolution(String contentType) {
        super(contentType);
        result.put("status", "ok");
    }
        public JSONResolution(Throwable e) {
        this("application/json", e);
    }
    
    /**
     * Create an "error" JSON result with the 
     * specified exception as the result.
     */
    public JSONResolution(String contentType, Throwable e) {
        super(contentType);
        result.put("status", "error")
              .put("error", Str.bvl(e.getMessage(), e.toString()));
    }
    
    /**
     * Create a successful JSON result with the 
     * specified result as the "result" attribute
     */
    public JSONResolution(JSONObject result) {
        this(); 
        this.result.put("result", result);
    }

    public JSONResolution put(String key, Object value) {
        this.result.put(key, value);
        return this;
    }

    @Override
    protected void stream(HttpServletResponse response) throws Exception {
        StringReader reader = new StringReader(result.toString());
        try {
            IOUtils.copy(reader, response.getWriter());
        }
        finally {
            closeQuietly(reader);
        }
    }
}
