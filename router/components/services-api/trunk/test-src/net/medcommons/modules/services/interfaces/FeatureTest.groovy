package net.medcommons.modules.services.interfaces;
import static org.junit.Assert.*;

import org.junit.Before;
import groovy.util.GroovyTestCase;

import net.medcommons.modules.services.interfaces.Feature;

import org.junit.Test;


class FeatureTest extends GroovyTestCase {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testMatch() {
        def f = new Feature(name:"foo.bar")
        
        assert f.match("foo.bar") : "Identical pattern should match"
        assert !f.match("foo") : "parent should not match more specific child"
        assert !f.match("foobar")
        assert !f.match("chocolate.bar")
        
        assert f.match("foo.bar.candy")
        
        
    }
}
