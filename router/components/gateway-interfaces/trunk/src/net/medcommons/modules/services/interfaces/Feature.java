package net.medcommons.modules.services.interfaces;

import org.json.JSONObject;

/**
 * Represents the enabled / disabled state of a feature or
 * group of features in a MedCommons Appliance.
 * 
 * @author ssadedin
 */
public class Feature {
    
    private String name;
    
    private String description;
    
    private boolean enabled;
    
    public Feature() {
    }
    
    public Feature(String name, String description, boolean enabled) {
        super();
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    /**
     * Tests if this feature name matches the supplied one or is a
     * parent of it.
     * 
     * @param pattern
     * @return
     */
    public boolean match(String pattern) {
        
        if(pattern.indexOf(this.name) != 0)
            return false;
        
        return pattern.length()==this.name.length() || 
                 pattern.length() > this.name.length() && pattern.charAt(this.name.length()) == '.';
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "Feature [description=" + description + ", enabled=" + enabled + ", name=" + name + "]";
    }

    public static Feature fromJSON(JSONObject obj) {
        return new Feature(
                    obj.getString("name"),
                    obj.getString("description"),
                    obj.getBoolean("enabled")
               );
    }
}
