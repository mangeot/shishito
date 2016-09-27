package jibiki.fr.shishito.Interfaces;

/**
 * Created by tibo on 29/07/16.
 * An interface representing an object capable of receiving Fast Edit events.
 */
public interface FastEditListener extends IsLoggedIn{
    void putFastEdit(String contribId, String xPath, String content, String title);
}
