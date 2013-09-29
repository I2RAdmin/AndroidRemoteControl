package com.i2r.androidremotecontroller.main.databouncer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

public class DataBouncerAdapter extends BaseExpandableListAdapter implements OnChildClickListener {
	
	private static final String TAG = "DataBouncerAdapter";
	
	private static final int ADD_POSITION = 0;
	private static final int REMOVE_POSITION = 1;
	
	private static final String ADD_BOUNCER = "Add bouncer";
	private static final String REMOVE_BOUNCER = "Remove bouncers";
	
	private static final String[] PARENT_GROUP = {
		ADD_BOUNCER,REMOVE_BOUNCER
	};
	
	private Context context;
	private DataBouncer bouncer;
	
	public DataBouncerAdapter(Context context){
		this.context = context;
		this.bouncer = DataBouncer.getInstance();
	}
	
	@Override
	public Object getChild(int parent, int child) {
		Object result = null;
		switch(parent){
		case ADD_POSITION:
			
			break;
			
		case REMOVE_POSITION:
			
			break;
			
		default:
			Log.d(TAG, "get child went to default");
			break;
		}
		return result;
	}

	@Override
	public long getChildId(int parent, int child) {
		return Integer.parseInt(new StringBuilder()
			.append(parent).append(child).toString());
	}

	@Override
	public View getChildView(int arg0, int arg1,
			boolean arg2, View arg3, ViewGroup arg4) {
		return null;
	}

	@Override
	public int getChildrenCount(int parent) {
		int result = 0;
		switch(parent){
		case ADD_POSITION:
			
			break;
			
		case REMOVE_POSITION:
			
			break;
			
		default:
			Log.d(TAG, "get child count went to default");
			break;
		}
		return result;
	}

	@Override
	public Object getGroup(int parent) {
		Object result = null;
		switch(parent){
		
		case ADD_POSITION:
			result = bouncer.getConnectors();
			break;
			
		case REMOVE_POSITION:
			result = bouncer.getConnectors();
			break;
			
			default:
				Log.d(TAG, "get group went to default");
				break;
		}
		return result;
	}

	@Override
	public int getGroupCount() {
		return PARENT_GROUP.length;
	}

	@Override
	public long getGroupId(int group_position) {
		return group_position;
	}

	@Override
	public View getGroupView(int arg0,
			boolean arg1, View arg2, ViewGroup arg3) {
		return null;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int group, int child) {
		return true;
	}
	
	public Context getContext(){
		return context;
	}
	
	public DataBouncer getBouncer(){
		return bouncer;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		switch(groupPosition){
		case ADD_POSITION:
			
			break;
			
		case REMOVE_POSITION:
			
			break;
			
			default:
				
				break;
		}
		return true;
	}
	
}
