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
        super("text/json");
        result.put("status", "ok");
    }
    
    /**
     * Create an "error" JSON result with the 
     * specified exception as the result.
     */
    public JSONResolution(Throwable e) {
        super("text/json");
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
