package jibiki.fr.shishito;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mangeot on 07/12/15.
 */
public class Volume {

        private String contents;
        private String domain;
        private String source;
        private String authors;
        private String legal;
        private String access;

        private Map elements = new HashMap<String, String>();

        protected HashMap<String, String> newoldTagMap = new java.util.HashMap<>();
        protected HashMap<String, String> oldnewTagMap = new java.util.HashMap<>();


    public Volume(){};

        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getAuthors() {
            return authors;
        }

        public void setAuthors(String authors) {
            this.authors = authors;
        }

        public String getLegal() {
            return legal;
        }

        public void setLegal(String legal) {
            this.legal = legal;
        }

        public String getAccess() {
            return access;
        }

        public void setAccess(String access) {
            this.access = access;
        }

        public Map getElements() {
            return elements;
        }

        public void setElements(Map elements) {
            this.elements = elements;
        }

    public HashMap<String, String> getNewOldTagMap() {
        return newoldTagMap;
    }
    public HashMap<String, String> getOldNewTagMap() {
        return oldnewTagMap;
    }

}
