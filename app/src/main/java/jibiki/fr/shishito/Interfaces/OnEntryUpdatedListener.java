package jibiki.fr.shishito.Interfaces;

import jibiki.fr.shishito.Models.ListEntry;

/**
 * Created by tibo on 29/07/16.
 * Interface representing an object capable of handling an "entry updated" event.
 */
public interface OnEntryUpdatedListener {
    void onEntryUpdatedListener(ListEntry entry);
}