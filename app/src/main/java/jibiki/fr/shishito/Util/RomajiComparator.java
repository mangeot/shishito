/* Copyright (C) 2016 Thibaut Le Guilly et Mathieu Mangeot
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package jibiki.fr.shishito.Util;

import jibiki.fr.shishito.Models.ListEntry;

/**
 * Created by mangeot on 06/01/16.
 * A class enabling comparison of two {@link ListEntry} items based on their Romaji.
 */
class RomajiComparator implements java.util.Comparator<ListEntry> {
        @Override
        public int compare(ListEntry o1, ListEntry o2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(o1.getRomajiDisplay(), o2.getRomajiDisplay());
            if (res == 0) {
                res = o2.getRomajiDisplay().compareTo(o1.getRomajiDisplay());
            }
            return res;
        }
    }
