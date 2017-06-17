package wuxin.enroll.prediction;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class ObjectItem implements Parcelable {
    public int sid = 0;
    public double rate = 0;
    public String avg = null;
    public String name = null;

    public void readFromParcel(Parcel in) {
    	sid = in.readInt();
    	rate = in.readDouble();
    	avg = in.readString();
    	name = in.readString();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sid);
        dest.writeDouble(rate);
        dest.writeString(avg);
        dest.writeString(name);
    }
    public static class SchoolItem extends ObjectItem {
        public int IconID = -1;
        public String min = null;
        public String region = null;
        public String s_type = null;
        public ArrayList<MajorItem> majors = null;
        
        @Override
        public void readFromParcel(Parcel in) {
        	super.readFromParcel(in);
            IconID = in.readInt();
            min = in.readString();
            region = in.readString();
            s_type = in.readString();
            ArrayList<MajorItem> parts = new ArrayList<MajorItem>();
            in.readTypedList(parts, MajorItem.CREATOR);
            majors = parts;
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(IconID);
            dest.writeString(min);
            dest.writeString(region);
            dest.writeString(s_type);
            dest.writeTypedList(majors);
        }

        public static final Creator<SchoolItem> CREATOR = new Creator<SchoolItem>() {
            @Override
            public SchoolItem createFromParcel(Parcel source) {
            	SchoolItem sItem = new SchoolItem();
            	sItem.readFromParcel(source);
                return sItem; // 在构造函数里面完成了 读取 的工作
            }

            // 供反序列化本类数组时调用的
            @Override
            public SchoolItem[] newArray(int size) {
                return new SchoolItem[size];
            }
        };
    }

    public static class MajorItem extends ObjectItem {
        public int id = 0;
        public String max = null;
        public String school = null;

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(id);
            dest.writeString(max);
            dest.writeString(school);
        }
        
        @Override
        public void readFromParcel(Parcel in) {
        	super.readFromParcel(in);
            id = in.readInt();
            max = in.readString();
            school = in.readString();
        }
        public static final Creator<MajorItem> CREATOR = new
        		Creator<MajorItem>() {
        	@Override
        	public MajorItem createFromParcel(Parcel source) {
        		MajorItem mItem = new MajorItem();
        		mItem.readFromParcel(source); // 在构造函数里面完成了 读取 的工作
				return mItem;
        	}
        	
        	// 供反序列化本类数组时调用的
        	@Override
        	public MajorItem[] newArray(int size) {
        		return new MajorItem[size];
        	}
        };
    }
}
