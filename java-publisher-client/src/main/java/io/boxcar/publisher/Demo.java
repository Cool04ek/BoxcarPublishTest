package io.boxcar.publisher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.boxcar.publisher.client.APIClient;
import io.boxcar.publisher.client.builder.RequestStrategy;
import io.boxcar.publisher.model.Alert;
import io.boxcar.publisher.model.Tag;

/**
 * This is the main entry point to execut the demo application.
 * <p/>
 * This demo just connects with the Boxcar Universal Push Notification
 * Platform and publishes a "Hello World" content to all Android devices
 * using the URL signature authorization method.
 *
 * @author jpcarlino
 */
public class Demo {

    static org.apache.log4j.Logger logger;

    static {
        logger = org.apache.log4j.Logger.getLogger(Demo.class);
    }

    static final String PROP_PUBLISH_KEY = "io.boxcar.publisher.key";
    static final String PROP_PUBLISH_SECRET = "io.boxcar.publisher.secret";

    static String PUBLISH_KEY;
    static String PUBLISH_SECRET;

    /**
     * @param args
     */
    public static void main(String[] args) {

        APIClient apiClient = null;

        // 1. Build the api client

        try {

            Properties properties = loadProperties();

            PUBLISH_KEY = properties.getProperty(PROP_PUBLISH_KEY);
            PUBLISH_SECRET = properties.getProperty(PROP_PUBLISH_SECRET);

            apiClient = new APIClient(properties, PUBLISH_KEY, PUBLISH_SECRET,
                    RequestStrategy.URL_SIGNATURE);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // 2. Publish

        try {
            StringBuffer text = getText(args);
            StringBuffer url = getUrl(args);
            int id = sendPush(text.toString(), url.toString(), 120, apiClient);
            logger.debug("Push sent with id " + id);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // 3. List tags
        try {
            List<Tag> tags = getTags(apiClient);
            logger.debug("Available tags on SaaS: ");
            for (Tag tag : tags) {
                logger.debug("Tag " + tag.getName() + " - id " + tag.getId()
                        + " - created at : " + tag.getCreatedAt()
                        + " - devices: " + tag.getDevices()
                        + " - deprecated: " + tag.isDeprecated());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // 4. Create, get, deprecate and remove a tag
        try {
            String tagName = "api-test";
            int id = createTag(tagName, apiClient);
            logger.debug("Tag " + tagName + " created with id " + id);
            Tag tag = getTag(id, apiClient);
            logger.debug("Tag " + tag.getName() + " - id " + tag.getId()
                    + " - created at : " + tag.getCreatedAt()
                    + " - devices: " + tag.getDevices()
                    + " - deprecated: " + tag.isDeprecated());
            deprecateTag(id, apiClient);
            logger.debug("Tag with id " + id + " was deprecated");
            tag = getTag(id, apiClient);
            logger.debug("Tag " + tag.getName() + " - id " + tag.getId()
                    + " - created at : " + tag.getCreatedAt()
                    + " - devices: " + tag.getDevices()
                    + " - deprecated: " + tag.isDeprecated());
            deleteTag(id, apiClient);
            logger.debug("Tag with id " + id + " was removed");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static int sendPush(String text, String url, int ttl, APIClient apiClient) throws IOException {
        Alert<String> alert = new Alert<String>(text);
        // remove this line if you just want to send it to all registered
        // devices
        alert.setTTL(ttl);

        List<String> tags = new ArrayList<String>();
        // send push to all registered devices
        tags.add(Alert.ALL_PUSH_TAG);
        alert.setTags(tags);

        // make this push 'normal' priority, meaning the gateway (GCM, APNS)
        // could delay it if device is sleeping
        alert.setNotificationPriority(Alert.Priority.normal);

        alert.setCustomUrl(url);

        return apiClient.publish(alert);
    }

    private static int sendSegmentPush(String text, String targetOS, int ttl, APIClient apiClient) throws IOException {
        Alert<String> alert = new Alert<String>(text);
        // remove this line if you just want to send it to all registered
        // devices
        alert.setTargetOS(targetOS);
        // do not keep the push more than 2 minutes if device is not
        // available
        alert.setTTL(ttl);

        // push to all devices that didn't open the app since
        // last day (last 24 hours).
        List<Alert.BehaviorSegment> segments = new ArrayList<Alert.BehaviorSegment>();
        Alert.BehaviorSegment segment = new Alert.BehaviorSegment(Alert.Behavior.last_app_open,
                new Alert.BehaviorSegment.Interval(Alert.IntervalUnit.day, 1));
        // we are interested on devices that did NOT open the app, so we want to exclude devices
        // that did match the above interval
        segment.setExclude(true);
        segments.add(segment);

        alert.setBehaviorSegments(segments);

        return apiClient.publish(alert);
    }

    private static int createTag(String tagName, APIClient apiClient) throws IOException {
        Tag tag = new Tag(tagName);

        int id = apiClient.createTag(tag);

        return id;
    }

    private static void deprecateTag(int id, APIClient apiClient) throws IOException {
        apiClient.deprecateTag(id);
    }

    private static void deleteTag(int id, APIClient apiClient) throws IOException {
        apiClient.deleteTag(id);
    }

    private static List<Tag> getTags(APIClient apiClient) throws IOException {
        List<Tag> tags = null;

        tags = apiClient.getTags();

        return tags;
    }

    private static Tag getTag(int id, APIClient apiClient) throws IOException {
        return apiClient.getTag(id);
    }

    private static Properties loadProperties() throws IOException {
        InputStream fileUrl = Demo.class.getResourceAsStream("/publisher.properties");
        Properties properties = new Properties();
        properties.load(fileUrl);
        return properties;
    }

    private static StringBuffer getText(String[] args) throws IOException {
        StringBuffer text = new StringBuffer();
        String defaultText = "Hello World!";
        if (args.length > 0) {
            if (args[0].toLowerCase().trim().equals("--file")) {
                String filename = args[1].trim();
                FileReader fileReader = new FileReader(filename);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    text.append(line);
                    text.append("\n");
                }
                fileReader.close();
            } else if (args[0].toLowerCase().trim().equals("--text")) {
                text.append(args[1].trim());
            } else {
                text.append(defaultText);
            }
        } else {
            text.append(defaultText);
        }
        return text;
    }

    private static StringBuffer getUrl(String[] args) throws IOException {
        StringBuffer text = new StringBuffer();
        String defaultText = "http://autoportal.com/upcomingcars/model/au";
        if (args.length > 0) {
            if (args[2].toLowerCase().trim().equals("--url")) {
                text.append(args[3].trim());
            } else {
                text.append(defaultText);
            }
        } else {
            text.append(defaultText);
        }
        return text;
    }
}
