package nl.stevenhack.chatter.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * 
 * @author Steven
 *
 * This Helper class handles some basic data manipulations like JSON and Storage.
 * This Helper mainly focuses on handling possible exceptions so other files
 * does not get clutterd with try/catches.
 * 
 */
public class DataHelper {

	/**
	 * Holds the application context of the app and makes it globally available.
	 */
	private static Context CONTEXT;

	/**
	 * Holds the unique identifier of the device and makes it globally available.
	 */
	private static String IDENTIFIER;

	/**
	 * This method can be used to globally retrieve the application context.
	 */
	public static Context getContext() {
		return DataHelper.CONTEXT;
	}

	/**
	 * With this method the application context can be set. It is adviced to only call this once.
	 * Best pratice is to call this when the application first starts up.
	 */
	public static void setContext(Context context) {
		DataHelper.CONTEXT = context;
	}

	/**
	 * This method can be used to globablly retrieve the unique identifier.
	 */
	public static String getIdentifier() {
		return DataHelper.IDENTIFIER;
	}

	/**
	 * With this method the unique identifier can be set. It is adviced to only call this once.
	 * Best pratice is to call this when the application first starts up.
	 */
	public static void setIdentifier(String identifier) {
		DataHelper.IDENTIFIER = identifier;
	}

	/**
	 * This method retrieves the value from the given key from a given JSON Object.
	 * It returns the value as a string.
	 */
	public static String getStringFromJSONObject(JSONObject object, String key) {
		// Return the key from the first object
		try {
			return object.getString(key).toString();
		}
		catch (JSONException e) {
			return "";
		}
		catch (NullPointerException e) {
			return "";
		}
	}

	/**
	 * This method retrieves the value from the given key from a given JSON Object.
	 * It returns the value as a JSONArray.
	 */
	public static JSONArray getArrayFromJSONObject(JSONObject object, String key) {
		// Return the key from the first object
		try {
			return object.getJSONArray(key);
		}
		catch (JSONException e) {
			return null;
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * This method retrieves the value from the given key from a given JSON Object.
	 * It returns the value as a JSONObject.
	 */
	public static JSONObject getObjectFromJSONObject(JSONObject object, String key) {
		// Return the key from the first object
		try {
			return object.getJSONObject(key);
		}
		catch (JSONException e) {
			return null;
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * This method retrieves the value from the given key from a given JSON Array.
	 * It returns the value as a string.
	 */
	public static String getStringFromJSONArray(JSONArray array, String key) {
		// Retrieve the first object in the array
		JSONObject object = getObjectFromJSONArray(array);

		// Retrieve the requested value from the given key
		return getStringFromJSONObject(object, key);
	}

	/**
	 * This method retrieves the first value from the given JSON Array.
	 */
	public static JSONObject getObjectFromJSONArray(JSONArray array) {
		return getObjectFromJSONArray(array, 0);
	}

	/**
	 * This method retrieves the nth value from the given JSON Array.
	 */
	public static JSONObject getObjectFromJSONArray(JSONArray array, int index) {
		// If the array does not containg anything, return null
		if (array.length() <= index)
			return null;

		// Return the first object
		try {
			return array.getJSONObject(index);
		}
		catch (JSONException e) {
			return null;
		}
	}
}
