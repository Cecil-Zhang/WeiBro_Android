package com.joker.weibro.model;

import android.graphics.drawable.Drawable;

public class Userinfo {
	private Integer _id;
	private String userId;
	private String token;
	private String tokenSecret;
	private String username;
	private Drawable usericon;
	
	public Integer get_id() {
		return _id;
	}
	public void set_id(Integer _id) {
		this._id = _id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getTokenSecret() {
		return tokenSecret;
	}
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Drawable getUsericon() {
		return usericon;
	}
	public void setUsericon(Drawable usericon) {
		this.usericon = usericon;
	}

}
