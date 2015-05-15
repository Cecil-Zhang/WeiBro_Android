package com.joker.weibro;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.joker.weibro.API.StatusesAPI;
import com.joker.weibro.API.UsersAPI;
import com.joker.weibro.helper.AccessTokenKeeper;
import com.joker.weibro.helper.Constants;
import com.joker.weibro.helper.WeiboAdapter;
import com.joker.weibro.model.ErrorInfo;
import com.joker.weibro.model.Status;
import com.joker.weibro.model.StatusList;
import com.joker.weibro.model.User;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;

import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WeiboActivity extends ActionBarActivity {
	private static final String TAG = WeiboActivity.class.getName();
	private Handler handler;
	private ProgressDialog dialog;
	/** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
    /** 用于获取微博信息流等操作的API */
    private StatusesAPI mStatusesAPI;
    private UsersAPI userAPI;
    private Status[] statuses;
    private User user;
    private boolean imageFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weibo);
		
		// 从 SharedPreferences 中读取上次已保存好 AccessToken 等信息，
        // 第一次启动本应用，AccessToken 不可用
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        if (mAccessToken.isSessionValid()) {
        	ListView listView = (ListView) findViewById(R.id.card_listView);
            listView.setAdapter(null);
    		// 获取当前已保存过的 Token
            // 对statusAPI实例化
            mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);
            userAPI = new UsersAPI(this, Constants.APP_KEY, mAccessToken);
            this.refresh();
        }else{
        	Intent intent = new Intent();
            intent.setClass(this, WBAuthActivity.class);
            startActivity(intent);
        }
        
	}

	private void refresh(){
		//刷新微博数据
		dialog=ProgressDialog.show(this, "Loading...", "客官莫急，我正在和网速赛跑...");
		mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false, statusListener);
		long uid = Long.parseLong(mAccessToken.getUid());
		userAPI.show(uid, userListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weibo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.logout) {
			AccessTokenKeeper.clear(getApplicationContext());
            mAccessToken = new Oauth2AccessToken();
            Toast.makeText(WeiboActivity.this, "注销成功", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setClass(this, WBAuthActivity.class);
            startActivity(intent);
			return true;
		}
//		else if(id == R.id.refresh){
//			statuses=null;
//			user=null;
//			ListView listView = (ListView) findViewById(R.id.card_listView);
//            listView.setAdapter(null);
//			this.refresh();
//		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setWeiboContent(){
		if(user!=null && statuses!=null && statuses.length>0){
			ListView listView = (ListView) findViewById(R.id.card_listView);
	       
	        Long ago = System.currentTimeMillis();
	        Thread th=new Thread(new Runnable(){
	        	public void run(){
	        		imageFlag = user.loadImageOfUser();
	        		for(Status s:statuses){
	        			s.loadImageOfStatus();
	        		}
	        	}
	        });
	        th.start();
	        while(!imageFlag){
	        	Long now = System.currentTimeMillis();
	        	if(now - ago > 120000){
	        		break;
	        	}else{
	        		try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        }
	        Log.i(TAG, "图片读取完毕");
	        listView.setAdapter(new WeiboAdapter(statuses,user,this));
	        dialog.dismiss();
		}
	}
	
	/**
     * 微博 StatusAPI.friendsTimelineIds 回调接口。
     */
    private RequestListener statusListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                if (response.startsWith("{\"statuses\"")) {
                	// 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statusList = StatusList.parse(response);
                    
                    if (statusList != null && statusList.total_number > 0) {
                    	ArrayList<Status> list=statusList.statusList;
                        Status[] array=(Status[]) list.toArray(new Status[list.size()]);
                        Arrays.sort(array);
                        statuses=array;
                        setWeiboContent();
                    }
                }
            }
            
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(WeiboActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };
    
    /**
     * 微博 UserAPI 回调接口。
     */
    private RequestListener userListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                // 调用 User#parse 将JSON串解析成User对象
                user = User.parse(response);
                if (user != null) {
                    Log.i(TAG, "读取用户信息成功");
                    setWeiboContent();
                } else {
                	Log.e(TAG, "读取用户信息失败");
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(WeiboActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };
    
}
