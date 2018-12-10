package com.example.jbois.go4lunch.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RestaurantListJson {
    @SerializedName("html_attributions")
    @Expose
    private List<Object> htmlAttributions = new ArrayList<Object>();
    @SerializedName("next_page_token")
    @Expose
    private String nextPageToken;
    @SerializedName("results")
    @Expose
    private List<Result> results = new ArrayList<Result>();
    @SerializedName("status")
    @Expose
    private String status;

    public List<Object> getHtmlAttributions() {
        return htmlAttributions;
    }
    public void setHtmlAttributions(List<Object> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }
    public String getNextPageToken() {
        return nextPageToken;
    }
    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
    public List<Result> getResults() {
        return results;
    }
    public void setResults(List<Result> results) {
        this.results = results;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public class Location {

        @SerializedName("lat")
        @Expose
        private Double lat;
        @SerializedName("lng")
        @Expose
        private Double lng;

        public Double getLat() {
            return lat;
        }
        public void setLat(Double lat) {
            this.lat = lat;
        }
        public Double getLng() {
            return lng;
        }
        public void setLng(Double lng) {
            this.lng = lng;
        }
    }
    public class Geometry {

        @SerializedName("location")
        @Expose
        private Location location;

        public Location getLocation() {
            return location;
        }
        public void setLocation(Location location) {
            this.location = location;
        }

    }
    public class Result {

        @SerializedName("geometry")
        @Expose
        private Geometry geometry;
        @SerializedName("icon")
        @Expose
        private String icon;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("opening_hours")
        @Expose
        private OpeningHours openingHours;
        @SerializedName("photos")
        @Expose
        private List<Photo> photos = new ArrayList<Photo>();
        @SerializedName("place_id")
        @Expose
        private String placeId;
        @SerializedName("rating")
        @Expose
        private Double rating;
        @SerializedName("reference")
        @Expose
        private String reference;
        @SerializedName("scope")
        @Expose
        private String scope;
        @SerializedName("types")
        @Expose
        private List<String> types = new ArrayList<String>();
        @SerializedName("vicinity")
        @Expose
        private String vicinity;
        @SerializedName("price_level")
        @Expose
        private Integer priceLevel;


        public Geometry getGeometry() {
            return geometry;
        }
        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }
        public String getIcon() {
            return icon;
        }
        public void setIcon(String icon) {
            this.icon = icon;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public OpeningHours getOpeningHours() {
            return openingHours;
        }
        public void setOpeningHours(OpeningHours openingHours) {
            this.openingHours = openingHours;
        }
        public List<Photo> getPhotos() {
            return photos;
        }
        public void setPhotos(List<Photo> photos) {
            this.photos = photos;
        }
        public String getPlaceId() {
            return placeId;
        }
        public void setPlaceId(String placeId) {
            this.placeId = placeId;
        }
        public Double getRating() {
            return rating;
        }
        public void setRating(Double rating) {
            this.rating = rating;
        }
        public String getReference() {
            return reference;
        }
        public void setReference(String reference) {
            this.reference = reference;
        }
        public String getScope() {
            return scope;
        }
        public void setScope(String scope) {
            this.scope = scope;
        }
        public List<String> getTypes() {
            return types;
        }
        public void setTypes(List<String> types) {
            this.types = types;
        }
        public String getVicinity() {
            return vicinity;
        }
        public void setVicinity(String vicinity) {
            this.vicinity = vicinity;
        }
        public Integer getPriceLevel() {
            return priceLevel;
        }
        public void setPriceLevel(Integer priceLevel) {
            this.priceLevel = priceLevel;
        }

    }
    public class Photo {

        @SerializedName("height")
        @Expose
        private Integer height;
        @SerializedName("html_attributions")
        @Expose
        private List<String> htmlAttributions = new ArrayList<String>();
        @SerializedName("photo_reference")
        @Expose
        private String photoReference;
        @SerializedName("width")
        @Expose
        private Integer width;

        public Integer getHeight() {
            return height;
        }
        public void setHeight(Integer height) {
            this.height = height;
        }
        public List<String> getHtmlAttributions() {
            return htmlAttributions;
        }
        public void setHtmlAttributions(List<String> htmlAttributions) {
            this.htmlAttributions = htmlAttributions;
        }
        public String getPhotoReference() {
            return photoReference;
        }
        public void setPhotoReference(String photoReference) {
            this.photoReference = photoReference;
        }
        public Integer getWidth() {
            return width;
        }
        public void setWidth(Integer width) {
            this.width = width;
        }

    }
    public class OpeningHours {

        @SerializedName("open_now")
        @Expose
        private Boolean openNow;
        @SerializedName("weekday_text")
        @Expose
        private List<Object> weekdayText = new ArrayList<Object>();

        public Boolean getOpenNow() {
            return openNow;
        }
        public void setOpenNow(Boolean openNow) {
            this.openNow = openNow;
        }
        public List<Object> getWeekdayText() {
            return weekdayText;
        }
        public void setWeekdayText(List<Object> weekdayText) {
            this.weekdayText = weekdayText;
        }

    }
}
