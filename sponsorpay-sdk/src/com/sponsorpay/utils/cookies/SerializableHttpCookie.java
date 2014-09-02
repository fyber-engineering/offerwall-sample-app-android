package com.sponsorpay.utils.cookies;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpCookie;

/**
 * from http://codereview.stackexchange.com/questions/61494/persistent-cookie-support-using-volley-and-httpurlconnection
 * based on https://github.com/loopj/android-async-http
 */

public class SerializableHttpCookie implements Serializable {
    private static final long serialVersionUID = -6051428667568260064L;

    private transient HttpCookie cookie;

    public SerializableHttpCookie(HttpCookie cookie) {
        this.cookie = cookie;
    }

    public HttpCookie getCookie() {
        return cookie;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(cookie.getName());
        out.writeObject(cookie.getValue());
        out.writeObject(cookie.getComment());
        out.writeObject(cookie.getCommentURL());
        out.writeBoolean(cookie.getDiscard());
        out.writeObject(cookie.getDomain());
        out.writeLong(cookie.getMaxAge());
        out.writeObject(cookie.getPath());
        out.writeObject(cookie.getPortlist());
        out.writeBoolean(cookie.getSecure());
        out.writeInt(cookie.getVersion());
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        String name = (String) in.readObject();
        String value = (String) in.readObject();
        cookie = new HttpCookie(name, value);
        cookie.setComment((String) in.readObject());
        cookie.setCommentURL((String) in.readObject());
        cookie.setDiscard(in.readBoolean());
        cookie.setDomain((String) in.readObject());
        cookie.setMaxAge(in.readLong());
        cookie.setPath((String) in.readObject());
        cookie.setPortlist((String) in.readObject());
        cookie.setSecure(in.readBoolean());
        cookie.setVersion(in.readInt());
    }
}