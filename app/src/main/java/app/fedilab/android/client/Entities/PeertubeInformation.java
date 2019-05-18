package app.fedilab.android.client.Entities;

import java.util.LinkedHashMap;

/* Copyright 2019 Thomas Schneider
 *
 * This file is a part of Fedilab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Fedilab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fedilab; if not,
 * see <http://www.gnu.org/licenses>. */


public class PeertubeInformation {


    private LinkedHashMap<Integer, String> categories;
    private LinkedHashMap<String, String> languages;
    private LinkedHashMap<Integer, String> licences;
    private LinkedHashMap<Integer, String> privacies;
    private LinkedHashMap<String, String> translations;

    public static final LinkedHashMap<String, String> langueMapped;
    static {
        LinkedHashMap<String, String> aMap = new LinkedHashMap<>();
        aMap.put("ca", "ca-ES");
        aMap.put("de", "de-DE");
        aMap.put("en", "en-US");
        aMap.put("es", "es-ES");
        aMap.put("eo", "eo");
        aMap.put("eu", "eu-ES");
        aMap.put("fr", "fr-FR");
        aMap.put("oc", "oc");
        aMap.put("pt", "pt-BR");
        aMap.put("sv", "sv-SE");
        aMap.put("cs", "cs-CZ");
        aMap.put("zh-CN", "zh-Hans-CN");
        aMap.put("zh-TW", "zh-Hans-TW");
        langueMapped = aMap;
    }


    public LinkedHashMap<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(LinkedHashMap<String, String> translations) {
        this.translations = translations;
    }

    public LinkedHashMap<Integer, String> getCategories() {
        return categories;
    }

    public void setCategories(LinkedHashMap<Integer, String> categories) {
        this.categories = categories;
    }

    public LinkedHashMap<String, String> getLanguages() {
        return languages;
    }

    public void setLanguages(LinkedHashMap<String, String> languages) {
        this.languages = languages;
    }

    public LinkedHashMap<Integer, String> getLicences() {
        return licences;
    }

    public void setLicences(LinkedHashMap<Integer, String> licences) {
        this.licences = licences;
    }

    public LinkedHashMap<Integer, String> getPrivacies() {
        return privacies;
    }

    public void setPrivacies(LinkedHashMap<Integer, String> privacies) {
        this.privacies = privacies;
    }
}
