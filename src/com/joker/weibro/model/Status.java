/*
 * Copyright (C) 2010-2013 The SINA WEIBO Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joker.weibro.model;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * 微博结构体。
 * 
 * @author SINA
 * @since 2013-11-22
 */
public class Status implements Comparable<Status>{
    
    /** 微博创建时间 */
    public String created_at;
    /** 微博ID */
    public String id;
    /** 微博MID */
    public String mid;
    /** 字符串型的微博ID */
    public String idstr;
    /** 微博信息内容 */
    public String text;
    /** 微博来源 */
    public String source;
    /** 是否已收藏，true：是，false：否  */
    public boolean favorited;
    /** 是否被截断，true：是，false：否 */
    public boolean truncated;
    /**（暂未支持）回复ID */
    public String in_reply_to_status_id;
    /**（暂未支持）回复人UID */
    public String in_reply_to_user_id;
    /**（暂未支持）回复人昵称 */
    public String in_reply_to_screen_name;
    /** 缩略图片地址（小图），没有时不返回此字段 */
    public String thumbnail_pic;
    /** 中等尺寸图片地址（中图），没有时不返回此字段 */
    public String bmiddle_pic;
    /** 原始图片地址（原图），没有时不返回此字段 */
    public String original_pic;
    /** 地理信息字段 */
    public Geo geo;
    /** 微博作者的用户信息字段 */
    public User user;
    /** 被转发的原微博信息字段，当该微博为转发微博时返回 */
    public Status retweeted_status;
    /** 转发数 */
    public int reposts_count;
    /** 评论数 */
    public int comments_count;
    /** 表态数 */
    public int attitudes_count;
    /** 暂未支持 */
    public int mlevel;
    /**
     * 微博的可见性及指定可见分组信息。该 object 中 type 取值，
     * 0：普通微博，1：私密微博，3：指定分组微博，4：密友微博；
     * list_id为分组的组号
     */
    public Visible visible;
    /** 微博配图地址。多图时返回多图链接。无配图返回"[]" */
    public ArrayList<String> pic_urls;
    /** 微博流内的推广微博ID */
    
    public ArrayList<Bitmap> picList;
    //public Ad ad;
    
    public int popular=0;
    
    public static Status parse(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return Status.parse(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static Status parse(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }
        
        Status status = new Status();
        status.created_at       = jsonObject.optString("created_at");
        status.id               = jsonObject.optString("id");
        status.mid              = jsonObject.optString("mid");
        status.idstr            = jsonObject.optString("idstr");
        status.text             = jsonObject.optString("text");
        status.source           = jsonObject.optString("source");
        status.favorited        = jsonObject.optBoolean("favorited", false);
        status.truncated        = jsonObject.optBoolean("truncated", false);
        
        // Have NOT supported
        status.in_reply_to_status_id   = jsonObject.optString("in_reply_to_status_id");
        status.in_reply_to_user_id     = jsonObject.optString("in_reply_to_user_id");
        status.in_reply_to_screen_name = jsonObject.optString("in_reply_to_screen_name");
        
        status.thumbnail_pic    = jsonObject.optString("thumbnail_pic");
        status.bmiddle_pic      = jsonObject.optString("bmiddle_pic");
        status.original_pic     = jsonObject.optString("original_pic");
        status.geo              = Geo.parse(jsonObject.optJSONObject("geo"));
        status.user             = User.parse(jsonObject.optJSONObject("user"));
        status.retweeted_status = Status.parse(jsonObject.optJSONObject("retweeted_status"));
        status.reposts_count    = jsonObject.optInt("reposts_count");
        status.comments_count   = jsonObject.optInt("comments_count");
        status.attitudes_count  = jsonObject.optInt("attitudes_count");
        status.mlevel           = jsonObject.optInt("mlevel", -1);    // Have NOT supported
        status.visible          = Visible.parse(jsonObject.optJSONObject("visible"));
        
        JSONArray picUrlsArray = jsonObject.optJSONArray("pic_urls");
        if (picUrlsArray != null && picUrlsArray.length() > 0) {
            int length = picUrlsArray.length();
            status.pic_urls = new ArrayList<String>(length);
            JSONObject tmpObject = null;
            for (int ix = 0; ix < length; ix++) {
                tmpObject = picUrlsArray.optJSONObject(ix);
                if (tmpObject != null) {
                    status.pic_urls.add(tmpObject.optString("thumbnail_pic"));
                }
            }
        }
        
        status.popular=status.reposts_count+status.comments_count+status.attitudes_count;
        //status.ad = jsonObject.optString("ad", "");
        
        return status;
    }
    
    public boolean loadImageOfStatus(){
    	picList=new ArrayList<Bitmap>();
    	try{
    		for(String urlstr:pic_urls){
    			URL url = new URL(urlstr);
        		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if(conn.getResponseCode() == 200){
                	InputStream inputStream = conn.getInputStream();
                	Bitmap image = BitmapFactory.decodeStream(inputStream);
                	picList.add(image);
                }
    		}
    		if(picList.size()==pic_urls.size()){
    			return true;
    		}
    	}catch(Exception e){
    		Log.e("Status", "从网络获取微博配图失败");
    		e.printStackTrace();
    	}
    	return false;
    }
    

	@Override
	public int compareTo(Status another) {
		// TODO Auto-generated method stub
		if(this.popular>another.popular){
			return -1;
		}else{
			return 1;
		}
	}
}
