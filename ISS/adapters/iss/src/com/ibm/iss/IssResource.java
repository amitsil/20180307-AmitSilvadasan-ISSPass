/*
 *    Licensed Materials - Property of IBM
 *    5725-I43 (C) Copyright IBM Corp. 2015. All Rights Reserved.
 *    US Government Users Restricted Rights - Use, duplication or
 *    disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.ibm.iss;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.io.IOUtils;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.worklight.adapters.rest.api.WLServerAPI;
import com.worklight.adapters.rest.api.WLServerAPIProvider;

@Path("/")
public class IssResource {

	// Define logger (Standard java.util.Logger)
	static Logger logger = Logger.getLogger(IssResource.class.getName());

	// Define the server api to be able to perform server operations
	WLServerAPI api = WLServerAPIProvider.getWLServerAPI();

	static final Double EQUATORIAL_EARTH_RADIUS = 6378.1370D;
	static final Double D2R = (Math.PI / 180D);
	static final Long SPEED_OF_ISS = 28000L;
	static final Double TOTAL_TIME_FOR_ISS_TO_REVOLVE = 5152500D;

	/**
	 * 
	 * This method returns response with ISS Pass Times. Path for method:
	 * "<server address>/ISS/adapters/iss/iss-pass"
	 * 
	 * @param lat
	 * @param lon
	 * @param alt
	 * @param n
	 * @return
	 */
	@POST
	@Path("/iss-pass")
	@Consumes("application/json")
	@Produces("application/json")
	public JSONObject getIssPasses(@QueryParam("lat") Double lat,
			@QueryParam("log") Double lon,
			@DefaultValue("100") @QueryParam("alt") Long alt,
			@DefaultValue("5") @QueryParam("n") Integer n) {

		JSONObject apiResponse = new JSONObject();
		
		try {
	
			// Validation of input parameters.
			if (lat == null) {
				apiResponse.put("message", "failure");
				apiResponse.put("reason", "Latitude must be specified");
				return apiResponse;
			} else if (lon == null) {
				apiResponse.put("message", "failure");
				apiResponse.put("reason", "Longitude must be specified");
				return apiResponse;
			} else if (lat > 90.0 || lat < -90.0) {
				apiResponse.put("message", "failure");
				apiResponse.put("reason",
						"Latitude must be number between -90.0 and 90.0");
				return apiResponse;
			} else if (lon > 180.0 || lon < -180.0) {
				apiResponse.put("message", "failure");
				apiResponse.put("reason",
						"Longitude must be number between -180.0 and 180.0");
				return apiResponse;
			}
		
			// Get the current location of ISS.
			JSONObject issNow = new JSONObject(IOUtils.toString(new URL(
					"http://api.open-notify.org/iss-now.json"), Charset
					.forName("UTF-8")));

			Long issTime = (Long) issNow.get("timestamp");
			JSONObject issPosition = (JSONObject) issNow.get("iss_position");
			Double issLatitude = Double.valueOf((String) issPosition
					.get("latitude"));
			Double issLongitude = Double.valueOf((String) issPosition
					.get("longitude"));

			// Calculate the distance between ISS and given co-ordinates.
			Double distance = distanceBetweenCoordinatesInKM(lat, lon,
					issLatitude, issLongitude);

			// Calculate time for ISS to pass based on distance to ISS from
			// given co-ordinates and speed of ISS.
			Double time = distance / SPEED_OF_ISS;

			// Build the output object
			apiResponse.put("message", "success");
			JSONObject request = new JSONObject();
			
			// Add the request parameter values
			request.put("latitude", lat);
			request.put("longitude", lon);
			request.put("altitude", alt);
			request.put("datetime", System.currentTimeMillis());
			request.put("passes", n);
			
			// Build the response array
			JSONArray response = new JSONArray();

			for (int i = 0; i < n; i++) {
				JSONObject responseObj = new JSONObject();
				
				// Next pass will be after complete revolution around the Earth.
				responseObj.put("risetime", time + i * TOTAL_TIME_FOR_ISS_TO_REVOLVE);
				responseObj.put("duration", ""); // TODO: Need to calculate duration.
				
				// Add each object to the array.
				response.add(responseObj);
			}
			
			apiResponse.put("response", response);

		} catch (JSONException | IOException e) {
			logger.log(Level.SEVERE, "Exception is thrown. Please try after sometime.");
			e.printStackTrace();
		}

		return apiResponse;
	}

	/**
	 * Method to calculate distance between two co-ordinates.
	 * 
	 * @param lat1
	 * @param long1
	 * @param lat2
	 * @param long2
	 * @return
	 */
	public Double distanceBetweenCoordinatesInKM(Double lat1, Double long1,
			Double lat2, Double long2) {
		Double dlong = (long2 - long1) * D2R;
		Double dlat = (lat2 - lat1) * D2R;
		Double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(lat1 * D2R)
				* Math.cos(lat2 * D2R) * Math.pow(Math.sin(dlong / 2D), 2D);
		Double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
		Double d = EQUATORIAL_EARTH_RADIUS * c;

		return d;
	}

}
