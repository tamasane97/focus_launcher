package com.example.easyapps.focusmode.launcher.utils


import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException

import java.lang.reflect.Type

/**
 * Created by MMT5762 on 02-07-2017.
 */

object GsonUtils {

    private val TAG = "GsonUtils"
    /**
     * Deserializes the JSON object passed as String and returns an object of the the type specified
     *
     *
     * This method expects the classPath to refer to a non generic type.
     *
     * @param  json the InputStream to read the incoming JSON object
     * @param  type   typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
     * @return            the deserialized object of type classPath if the class exists and json represents an object of the type classPath     * @return           the deserialized object of type classPath if the class exists and json represents an object of the type classPath, null otherwise
     */
    fun <T> deserializeJSON(json: String?, type: Type?): T? {
        var queryResult: T? = null
        if (!json.isNullOrEmpty() && type != null) {
            try {
                queryResult = Gson().fromJson(json, type)
            } catch (e: JsonSyntaxException) {
            } catch (e: JsonIOException) {
            } catch (e: Exception) {
            }

        }
        return queryResult
    }

    /**
     * Deserializes the JSON object passed as String and returns an object of the the type specified
     *
     *
     * This method expects the classPath to refer to a non generic type.
     *
     * @param  json the InputStream to read the incoming JSON object
     * @param  className   typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
     * @return            the deserialized object of type classPath if the class exists and json represents an object of the type classPath     * @return           the deserialized object of type classPath if the class exists and json represents an object of the type classPath, null otherwise
     */
    fun <T> deserializeJSON(json: String?, className: Class<T>?): T? {
        var queryResult: T? = null
        if (!json.isNullOrEmpty() && className != null) {
            try {
                queryResult = Gson().fromJson(json, className)
            } catch (e: JsonSyntaxException) {
            } catch (e: JsonIOException) {
            } catch (e: Exception) {
            }

        }
        return queryResult
    }

    /**
     * Serializes an object to its JSON representation
     *
     *
     * This method should be used only with non generic objects.
     *
     * @param  object the InputStream to read the incoming JSON object
     * @return        the  JSON representation in form of a String
     */
    fun serializeToJson(`object`: Any, cls: Class<*>): String {
        return Gson().toJson(`object`, cls)
    }

}