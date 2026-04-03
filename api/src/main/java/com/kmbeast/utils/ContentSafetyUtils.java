package com.kmbeast.utils;

public class ContentSafetyUtils {

    private ContentSafetyUtils() {
    }

    public static FilterResult filter(String content) {
        AhoCorasickFilter ahoCorasickFilter = new AhoCorasickFilter();
        for (String sensitiveWord : AhoCorasickFilter.sensitiveWords) {
            ahoCorasickFilter.addWord(sensitiveWord);
        }
        ahoCorasickFilter.buildFailurePointer();
        String filteredContent = ahoCorasickFilter.filter(content);
        return new FilterResult(filteredContent, !filteredContent.equals(content));
    }

    public static class FilterResult {
        private final String content;
        private final boolean hitSensitive;

        public FilterResult(String content, boolean hitSensitive) {
            this.content = content;
            this.hitSensitive = hitSensitive;
        }

        public String getContent() {
            return content;
        }

        public boolean isHitSensitive() {
            return hitSensitive;
        }
    }
}
