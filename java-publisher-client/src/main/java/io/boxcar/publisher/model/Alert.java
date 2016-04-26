package io.boxcar.publisher.model;

import io.boxcar.publisher.client.util.TimeUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the "content" to be published
 *
 * @author jpcarlino
 */
public class Alert<T> {

    public static final String ALL_PUSH_TAG = "@all";

    public static class I18nAlert {
        @SerializedName("loc-key")
        public String key;
        @SerializedName("loc-args")
        public String[] args;
    }

    public class Aps<T> {
        String badge;
        String sound;
        String category;
        T alert;
    }

    public enum Behavior {
        last_app_open, last_app_registered
    }

    public enum Priority {
        high, normal
    }

    public enum IntervalUnit {
        day, month
    }

    public static class BehaviorSegment {
        public static class Interval {
            Integer value;
            IntervalUnit unit;

            public Interval(IntervalUnit unit, Integer value) {
                this.unit = unit;
                this.value = value;
            }
        }

        public BehaviorSegment(Behavior behavior, Interval interval) {
            this.behavior = behavior;
            this.interval = interval;
            this.exclude = false;
        }

        Behavior behavior;
        boolean exclude;
        Interval interval;

        public boolean isExclude() {
            return exclude;
        }

        public void setExclude(boolean exclude) {
            this.exclude = exclude;
        }

    }

    Aps<T> aps;

    @SerializedName("behavior_segments")
    List<BehaviorSegment> segments;
    List<String> tags;
    List<String> target_os;
    List<Integer> client_ids;
    String id;
    long expires;
    Integer expires_after;
    List<String> device_tokens;
    Priority notification_priority;

    @SerializedName("@img")
    String img;

    @SerializedName("custom_url")
    String customUrl;

    public Alert(T content) {
        this.aps = new Aps<T>();
        this.aps.badge = "auto";
        this.aps.sound = "beep.wav";
        this.aps.category = null;
        this.aps.alert = content;
        this.tags = new ArrayList<String>();
        this.id = null;
        this.target_os = null;
        this.client_ids = null;
        this.device_tokens = null;
        this.expires_after = null; // do not set TTL by default
        this.img = null;
        this.segments = null;
        this.notification_priority = null;
        setAPICallTimeToLive(30000);
    }

    public Alert(T content, String id) {
        this.aps = new Aps<T>();
        this.aps.badge = "auto";
        this.aps.sound = "beep.wav";
        this.aps.category = null;
        this.aps.alert = content;
        this.tags = new ArrayList<String>();
        this.id = id;
        this.target_os = null;
        this.client_ids = null;
        this.device_tokens = null;
        this.expires_after = null; // do not set TTL by default
        this.img = null;
        this.notification_priority = null;
        this.segments = null;
        setAPICallTimeToLive(30000);
    }

    public T getAlert() {
        return aps.alert;
    }

    public void setAlert(T content) {
        aps.alert = content;
    }

    public String getSound() {
        return aps.sound;
    }

    public void setSound(String sound) {
        aps.sound = sound;
    }

    public String getCategory() {
        return aps.category;
    }

    public void setCategory(String category) {
        aps.category = category;
    }

    public String getBadge() {
        return aps.badge;
    }

    public void setBadge(String badge) {
        aps.badge = badge;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the Time To Live (TTL) in seconds for this push.
     * This value is used by the gateway provider (Google GCM, Apple APNS,
     * Amazon ADM, etc) to know how much time to keep the push if device
     * is offline.
     * Ignored if value <= 0
     *
     * @param seconds TTL in seconds. It won't be considered for delivery
     *                if <= 0
     */
    public void setTTL(int seconds) {
        this.expires_after = seconds;
    }

    public int getTTL() {
        if (expires_after == null) {
            return -1;
        }
        return this.expires_after;
    }

    public List<String> getTargetOS() {
        return target_os;
    }

    public void setTargetOS(List<String> targetOS) {
        this.target_os = targetOS;
    }

    public void setTargetOS(String targetOS) {
        this.target_os = new ArrayList<String>();
        this.target_os.add(targetOS);
    }

    public List<Integer> getClientIds() {
        return client_ids;
    }

    public void setClientIds(List<Integer> clientIds) {
        this.client_ids = clientIds;
    }

    public List<String> getDeviceTokens() {
        return device_tokens;
    }

    public void setDeviceTokens(List<String> deviceTokens) {
        this.device_tokens = deviceTokens;
    }

    public String getBigImageURL() {
        return img;
    }

    public void setBigImageURL(String url) {
        this.img = url;
    }

    public Priority getNotificationPriority() {
        return notification_priority;
    }

    public void setNotificationPriority(Priority notificationPriority) {
        this.notification_priority = notificationPriority;
    }

    public List<BehaviorSegment> getBehaviorSegments() {
        return segments;
    }

    public void setBehaviorSegments(List<BehaviorSegment> segments) {
        this.segments = segments;
    }

    protected void setAPICallTimeToLive(long millis) {
        this.expires = TimeUtils.getExpiryDate(millis).getTime() / 1000;
    }

    public String getCustomUrl() {
        return customUrl;
    }

    public void setCustomUrl(String customUrl) {
        this.customUrl = customUrl;
    }
}
