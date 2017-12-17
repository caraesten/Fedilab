package fr.gouv.etalab.mastodon.client.Entities;

import android.support.annotation.NonNull;


/**
 * Created by Thomas on 15/09/2017.
 */

public class Version implements Comparable<Version> {

    private String version;

    public final String get() {
        return this.version;
    }

    public Version(String version) {
        if(version == null)
            version = "2.1";
        if( version.endsWith("."))
            version = version.substring(0, version.length() - 1);
        version = version.replaceAll("[^\\d.]", "");
        if(!version.matches("[0-9]+(\\.[0-9]+)*"))
            version = "2.1";
        this.version = version;
    }

    @Override public int compareTo(@NonNull Version that) {
        String[] thisParts = this.get().split("\\.");
        String[] thatParts = that.get().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for(int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if(thisPart < thatPart)
                return -1;
            if(thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    @Override public boolean equals(Object that) {
        return this == that || that != null && this.getClass() == that.getClass() && this.compareTo((Version) that) == 0;
    }

}
