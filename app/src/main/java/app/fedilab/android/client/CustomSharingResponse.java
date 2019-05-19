package app.fedilab.android.client;
/* Copyright 2019 Curtis Rock
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

import app.fedilab.android.client.Entities.Error;

/**
 * Created by Curtis on 13/02/2019.
 * Hydrate response from the remote content aggregator for Custom Sharing
 */

public class CustomSharingResponse {

    private Error error = null;
    private String response;

    public Error getError() {
        return error;
    }

    public String getResponse() {
        return response;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
