package jibiki.fr.shishito.Util;

import jibiki.fr.shishito.Models.ListEntry;

/**
 * Created by mangeot on 06/01/16.
 */
public class RomajiComparator implements java.util.Comparator<ListEntry> {
        @Override
        public int compare(ListEntry o1, ListEntry o2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(o1.getRomajiDisplay(), o2.getRomajiDisplay());
            if (res == 0) {
                res = o2.getRomajiDisplay().compareTo(o1.getRomajiDisplay());
            }
            return res;
        }
    }
