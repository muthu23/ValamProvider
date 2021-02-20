package com.delivery.provider.Bean;

import java.io.Serializable;

/**
 * Created by CSS on 27-11-2017.
 */

public class Flows extends Throwable implements Serializable {

    String order;
    String deliveryAddress;
    String comments;
    String source_lat;
    String source_long;
    String destination_lat;
    String destination_long;
    String status;
    String id;
    String user_request_id;
    String after_image;

    String receiver_name;
    String receiver_number;

    public String getorder() {
        return order;
    }

    public void setorder(String order) {
        this.order = order;
    }

    public String getdeliveryAddress() {
        return deliveryAddress;
    }

    public void setdeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getcomments() {
        return comments;
    }

    public void setcomments(String comments) {
        this.comments = comments;
    }


    public String getSource_lat() {
        return source_lat;
    }

    public void setSource_lat(String source_lat) {
        this.source_lat = source_lat;
    }

    public String getSource_long() {
        return source_long;
    }

    public void setSource_long(String source_long) {
        this.source_long = source_long;
    }


    public String getDestination_lat() {
        return destination_lat;
    }

    public void setDestination_lat(String destination_lat) {
        this.destination_lat = destination_lat;
    }


    public String getDestination_long() {
        return destination_long;
    }

    public void setDestination_long(String destination_long) {
        this.destination_long = destination_long;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_request_id() {
        return user_request_id;
    }

    public void setUser_request_id(String user_request_id) {
        this.user_request_id = user_request_id;
    }

    public String getAfterImage() {
        return after_image;
    }

    public void setAfterImage(String afterImage) {
        this.after_image = afterImage;
    }

    @Override
    public String toString() {
        return "Flows{" +
                "deliveryAddress='" + deliveryAddress + '\'' +
                ", order='" + order + '\'' +
                ", comments='" + comments + '\'' +
                "status='" + status + '\'' +
                ", source_lat='" + source_lat + '\'' +
                ", source_long='" + source_long + '\'' +
                "destination_lat='" + destination_lat + '\'' +
                ", destination_long='" + destination_long + '\'' +
                ", after_image='" + after_image + '\'' +
                '}';
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public void setReceiver_name(String receiver_name) {
        this.receiver_name = receiver_name;
    }

    public String getReceiver_number() {
        return receiver_number;
    }

    public void setReceiver_number(String receiver_number) {
        this.receiver_number = receiver_number;
    }
}

