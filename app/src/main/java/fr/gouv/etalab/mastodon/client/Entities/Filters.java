package fr.gouv.etalab.mastodon.client.Entities;
/* Copyright 2018 Thomas Schneider
 *
 * This file is a part of Mastalab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastalab; if not,
 * see <http://www.gnu.org/licenses>. */


import java.util.ArrayList;

/**
 * Created by Thomas on 05/09/2018.
 * Manage filters
 */

public class Filters {

    private String id;
    private String phrase;
    private ArrayList<String> context;
    private boolean irreversible;
    private boolean whole_word;
    private int expires_in;

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public ArrayList<String> getContext() {
        return context;
    }

    public void setContext(ArrayList<String> context) {
        this.context = context;
    }

    public boolean isIrreversible() {
        return irreversible;
    }

    public void setIrreversible(boolean irreversible) {
        this.irreversible = irreversible;
    }

    public boolean isWhole_word() {
        return whole_word;
    }

    public void setWhole_word(boolean whole_word) {
        this.whole_word = whole_word;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
