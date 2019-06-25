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

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Results;


/**
 * Created by Curtis on 13/02/2019.
 * Manage custom sharing of status metadata to remote content aggregator
 */

public class CustomSharing {

    private Context context;
    private Results results;
    private CustomSharingResponse customSharingResponse;
    private Error CustomSharingError;

    public CustomSharing(Context context) {
        this.context = context;
        if( context == null) {
            CustomSharingError = new Error();
            return;
        }
        customSharingResponse = new CustomSharingResponse();
        CustomSharingError = null;
    }

    /***
     * pass status metadata to remote content aggregator *synchronously*
     * @return CustomSharingResponse
     */
    public CustomSharingResponse customShare(String encodedCustomSharingURL) {
        String HTTPResponse = "";
        try {
            HTTPResponse = new HttpsConnection(context, null).get(encodedCustomSharingURL);
        } catch (HttpsConnection.HttpsConnectionException e) {
            e.printStackTrace();
            setError(e.getStatusCode(), e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        customSharingResponse.setResponse(HTTPResponse);
        return customSharingResponse;
    }

    public Error getError(){
        return CustomSharingError;
    }


    /**
     * Set the error message
     * @param statusCode int code
     * @param error Throwable error
     */
    private void setError(int statusCode, Throwable error){
        CustomSharingError = new Error();
        CustomSharingError.setStatusCode(statusCode);
        String message = statusCode + " - " + error.getMessage();
        try {
            JSONObject jsonObject = new JSONObject(error.getMessage());
            String errorM = jsonObject.get("error").toString();
            message = "Error " + statusCode + " : " + errorM;
        } catch (JSONException e) {
            if(error.getMessage().split(".").length > 0) {
                String errorM = error.getMessage().split(".")[0];
                message = "Error " + statusCode + " : " + errorM;
            }
        }
        CustomSharingError.setError(message);
        customSharingResponse.setError(CustomSharingError);
    }
}
