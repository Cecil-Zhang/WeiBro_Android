package com.joker.weibro.helper;

import java.util.ArrayList;

import com.joker.weibro.R;
import com.joker.weibro.model.Status;
import com.joker.weibro.model.User;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WeiboAdapter extends BaseAdapter {
	private Status[] statusList;
	private User user;
	private Context context;
	
	public WeiboAdapter(Status[] list,User u,Context cont){
		super();
		this.statusList = list;
		user = u;
		context = cont;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 11;
	}

	@Override
	public Object getItem(int index) {
		// TODO Auto-generated method stub
		return statusList[index-1];
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0+1;
	}
	
	@Override
	public int getItemViewType(int position){
		if(position==0){
			return 0;
		}else{
			return 1;
		}
	}
	
	@Override
	public int getViewTypeCount(){
		return 2;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		int type=this.getItemViewType(index);
		if(type == 0){
			if(view == null){
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				view = inflater.inflate(R.layout.weibo_profile_item, parent, false);
			}
			ImageView imageView = (ImageView) view.findViewById(R.id.avatar);
			
			imageView.setImageBitmap(user.getImageOfUser());
			TextView textView = (TextView) view.findViewById(R.id.usernameView);
			textView.setText(user.screen_name);
		}else{
			if(view == null){
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				view = inflater.inflate(R.layout.list_item_card, parent, false);
			}
			SpannableStringBuilder ssb=new SpannableStringBuilder();
			Status weibo = statusList[index-1];
			TextView textView = (TextView) view.findViewById(R.id.line1);
			String name=String.valueOf(index)+"、"+weibo.user.screen_name+": ";
			String popular="  (人气  "+weibo.popular+")\n";
			ssb.append(name);
			ssb.append(weibo.text);
			ssb.append(popular);
			ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length()-1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			int color=R.color.titleColor;
			if(index==1){
				color=Color.rgb(204, 0, 0);
			}else if(index==2){
				color=Color.rgb(204, 51, 102);
			}else if(index==3){
				color=Color.rgb(204, 102, 0);
			}
			ssb.setSpan(new RelativeSizeSpan((float) 1.4), 0, 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			ssb.setSpan(new ForegroundColorSpan(color), 0, name.length()-1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			int length=name.length()+weibo.text.length()+popular.length();
			if(weibo.picList!=null && weibo.picList.size()!=0){
				for(Bitmap image:weibo.picList){
					ssb.append("  ");
					ImageSpan imageSpan=new ImageSpan(context,image);
					ssb.setSpan(imageSpan, length, length+2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
					length=length+2;
				}
			}
			textView.setText(ssb);
			
			textView.setOnClickListener(new WeiboClickListener(Long.parseLong(weibo.id)));
		}
		return view;
	}

	class WeiboClickListener implements OnClickListener{
		private Long weiboId;
		
		public WeiboClickListener(Long id){
			super();
			this.weiboId=id;
		}
		
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			Oauth2AccessToken mAccessToken = AccessTokenKeeper.readAccessToken(context);
			String url = "http://api.weibo.com/2/statuses/go?access_token="+mAccessToken.getToken()+"&uid="+
			mAccessToken.getUid()+"&id="+weiboId;
			Uri uri = Uri.parse(url);    
			Intent it = new Intent(Intent.ACTION_VIEW, uri);    
			context.startActivity(it); 
		}
		
	}
}
