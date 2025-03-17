package model;

import java.io.Serializable;

public class LicenceSecure implements Serializable{
private String iMac;
private String Name;
private String ordre;
private String key;

@Override
public int hashCode() {
    int hash = 2;
    hash = hash * 17 + iMac.hashCode();
    hash = hash * 31 + Name.hashCode();
    hash = hash * 13 + ordre.hashCode();
    return hash;
}

}
