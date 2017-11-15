package com.example.fangy.videoplayer;

import android.net.Uri;
import android.util.Log;
import android.util.Xml;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by fangy on 2017/11/13.
 */

public class ParseMPD {
    private static final String TAG = ParseMPD.class.getSimpleName();

    private static Pattern PATTERN_TIME = Pattern.compile("PT((\\d+)H)?((\\d+)M)?((\\d+(\\.\\d+)?)S)");

    /**
     * Parses an MPD XML file. This needs to be executed off the main thread, else a
     * NetworkOnMainThreadException gets thrown.
     * @param uri the URl of an MPD XML file
     * @param httpClient the http client instance to use for the request
     * @return a MPD object
     * @throws android.os.NetworkOnMainThreadException if executed on the main thread
     */
    public MPD parse(String uri, OkHttpClient httpClient) throws DashParserException {
        MPD mpd = null;
        Headers.Builder headers = new Headers.Builder();

        Request.Builder request = new Request.Builder()
                .url(uri)
                .headers(headers.build());

        try {
            Response response = httpClient.newCall(request.build()).execute();
            if (!response.isSuccessful()) {
                throw new IOException("error requesting the MPD");
            }

            // Determine this MPD's default BaseURL by removing the last path segment (which is the MPD file)
            Uri baseUrl = Uri.parse(uri.substring(0, uri.lastIndexOf("/") + 1));

           // String htmlStr =  response.body().string();
          //  Log.e(TAG, htmlStr);

            // Parse the MPD file
            mpd = parse(response.body().byteStream(), baseUrl);
        } catch (IOException e) {
            Log.e(TAG, "error downloading the MPD", e);
            throw new DashParserException("error downloading the MPD", e);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "error parsing the MPD", e);
            throw new DashParserException("error parsing the MPD", e);
        }
        Log.e(TAG, "parse mpd success");

        return mpd;
    }

    /**
     * Parses an MPD XML file. This needs to be executed off the main thread, else a
     * NetworkOnMainThreadException gets thrown.
     * @param source the URl of an MPD XML file
     * @return a MPD object
     * @throws android.os.NetworkOnMainThreadException if executed on the main thread
     */
    public MPD parse(String source) throws DashParserException {
        return parse(source, new OkHttpClient());
    }

    private MPD parse(InputStream in, Uri baseUrl) throws XmlPullParserException, IOException, DashParserException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            MPD mpd = new MPD();

            int type = 0;
            while ((type = parser.next()) >= 0) {
                if (type == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equals("MPD")) {
                        Log.e(TAG, "parsing MPD");
                        mpd.mediaPresentationDurationUs = getAttributeValueTime(parser, "mediaPresentationDuration");
                    } else if (tagName.equals("Representation")) {
                        Log.e(TAG, "parsing Representation");
                        try {
                            mpd.representation.add(readRepresentation(mpd, parser));
                        } catch (Exception e) {
                            Log.e(TAG, "error reading representation: " + e.getMessage(), e);
                        }
                    }
                } else if(type == XmlPullParser.END_TAG) {
                    String tagName = parser.getName();
                    if(tagName.equals("MPD")) {
                        break;
                    }
                }
        }

            Log.e(TAG, mpd.toString());
        return mpd;

    } finally {
            in.close();
        }
    }

    private Representation readRepresentation(MPD mpd, XmlPullParser parser)
            throws XmlPullParserException, IOException, DashParserException{
        Representation representation = new Representation();

        representation.id = getAttributeValue(parser, "id");
        representation.bandwidth = getAttributeValueInt(parser, "bandwidth");

        int type = 0;
        while((type = parser.next()) >= 0) {
            String tagName = parser.getName();

            if(type == XmlPullParser.START_TAG) {
                if(tagName.equals("BaseURL")) {
                    Log.e(TAG, "parsing BaseURL");
                    Uri uri = null;
                    uri = extendUrl(uri, parser.nextText());
                    representation.basedUrl = String.valueOf(uri);
                } else if(tagName.equals("SegmentList")){
                    Log.e(TAG, "Reading SegmentList");
                    representation.segmentDurationUs = getAttributeValueLong(parser, "duration");
                } else if(tagName.equals("SegmentURL")) {
                    String media = getAttributeValue(parser, "media");
                    representation.segments.add(new Segment(media));

                }

            } else if(type == XmlPullParser.END_TAG) {
                if(tagName.equals("Representation")) {
                    break;
                }
            }
        }

        Log.e(TAG, representation.toString());
        return representation;
    }


    /**
     * Parse a timestamp and return its duration in microseconds.
     * http://en.wikipedia.org/wiki/ISO_8601#Durations
     */
    private static long parseTime(String time) {
        Matcher matcher = PATTERN_TIME.matcher(time);

        if(matcher.matches()) {
            long hours = 0;
            long minutes = 0;
            double seconds = 0;

            String group = matcher.group(2);
            if (group != null) {
                hours = Long.parseLong(group);
            }
            group = matcher.group(4);
            if (group != null) {
                minutes = Long.parseLong(group);
            }
            group = matcher.group(6);
            if (group != null) {
                seconds = Double.parseDouble(group);
            }

            return (long) (seconds * 1000 * 1000)
                    + minutes * 60 * 1000 * 1000
                    + hours * 60 * 60 * 1000 * 1000;
        }

        return -1;
    }

    private static String getAttributeValue(XmlPullParser parser, String name, String defValue) {
        String value = parser.getAttributeValue(null, name);
        return value != null ? value : defValue;
    }

    private static String getAttributeValue(XmlPullParser parser, String name) {
        return getAttributeValue(parser, name, null);
    }

    private static int getAttributeValueInt(XmlPullParser parser, String name) {
        return Integer.parseInt(getAttributeValue(parser, name, "0"));
    }

    private static long getAttributeValueLong(XmlPullParser parser, String name) {
        return Long.parseLong(getAttributeValue(parser, name, "0"));
    }

    private static long getAttributeValueTime(XmlPullParser parser, String name) {
        return parseTime(getAttributeValue(parser, name, "PT0S"));
    }

    /**
     * Extends an URL with an extended path if the extension is relative, or replaces the entire URL
     * with the extension if it is absolute.
     */
    private static Uri extendUrl(Uri url, String urlExtension) {
        urlExtension = urlExtension.replace(" ", "%20"); // Convert spaces

        Uri newUrl = Uri.parse(urlExtension);

        if(newUrl.isRelative()) {
            /* Uri.withAppendedPath appends the extension to the end of the "real" server path,
             * instead of the end of the uri string.
             * Example: http://server.com/foo?file=http://server2.net/ + file1.mp4
             *           => http://server.com/foo/file1.mp4?file=http://server2.net/
             * To avoid this, we need to join as strings instead. */
            newUrl = Uri.parse(url.toString() + urlExtension);
        }
        return newUrl;
    }


}
