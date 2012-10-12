/*
 * $Id: MsDateConverter.java 3156 2008-12-24 07:52:33Z ssadedin $
 * Created on 24/12/2008
 */
package net.medcommons.router.web.stripes;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

public class MsDateConverter implements TypeConverter<Date>{

    public MsDateConverter() {
    }

    public Date convert(String input, Class<? extends Date> targetType, Collection<ValidationError> errors) {
        return new Date(Long.parseLong(input));
    }

    public void setLocale(Locale locale) {
    }

}
