package com.joker.weibro.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.joker.weibro.API.UsersAPI;
import com.joker.weibro.model.User;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;

//负责用户信息相关的微博API调用
public class WBUserAPIHelper {
	private static final String TAG = WBUserAPIHelper.class.getName();
	/** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
	private UsersAPI mUsersAPI;
	private Context context;
	private User user;
    
    public WBUserAPIHelper(Context context){
    	super();
    	// 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(context);
        // 获取用户信息接口
        mUsersAPI = new UsersAPI(context, Constants.APP_KEY, mAccessToken);
        this.context = context;
    }
    
    public User getUserinfoByToken(){
    	long uid = Long.parseLong(mAccessToken.getUid());
        String response=mUsersAPI.showSync(uid);
        Log.i(TAG, "hello "+response);
        if (!TextUtils.isEmpty(response)) {
            LogUtil.i(TAG, response);
            // 调用 User#parse 将JSON串解析成User对象
            User userTemp = User.parse(response);
            if (userTemp != null) {
                user=userTemp;
                
            } 
        }
        if(user!=null){
        	Log.i(TAG, "got user profile");
        	return user;
        }else{
        	Log.e(TAG, "got an error when trying to get user profile");
        	return null;
        }
    }
    
}
