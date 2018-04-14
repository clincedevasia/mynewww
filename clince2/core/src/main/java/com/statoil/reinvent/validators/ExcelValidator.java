package com.statoil.reinvent.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelValidator {

        private Pattern pattern;
        private Matcher matcher;

        private static final String PATTERN =
                "([^\\s]+(\\.(?i)(xls|xlsx))$)";

        public ExcelValidator(){
            pattern = Pattern.compile(PATTERN);
        }

        public boolean validate(final String file){
            matcher = pattern.matcher(file);
            return matcher.matches();
        }
}
