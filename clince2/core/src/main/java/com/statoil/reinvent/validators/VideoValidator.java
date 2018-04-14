package com.statoil.reinvent.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoValidator {

        private Pattern pattern;
        private Matcher matcher;

        private static final String PATTERN =
                "([^\\s]+(\\.(?i)(wmw|mp4|webm|m4v|mkv|avi))$)";

        public VideoValidator(){
            pattern = Pattern.compile(PATTERN);
        }

        public boolean validate(final String file){
            matcher = pattern.matcher(file);
            return matcher.matches();
        }
}
