/*
 * $Id: WindowLevelPreset.java 2175 2007-10-19 12:37:08Z ssadedin $
 * Created on 19/10/2007
 */
package net.medcommons.router.services.dicom.util;

/**
 * Represents a preset configuration for display of an image in the viewer.
 * 
 * @author ssadedin
 */
public class WindowLevelPreset {
    
    private int window;
    
    private int level;
    
    private String name;

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() { 
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
