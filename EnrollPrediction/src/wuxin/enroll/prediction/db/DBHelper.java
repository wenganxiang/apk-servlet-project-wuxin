package wuxin.enroll.prediction.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 用法： DBHelper dbHelper = new DBHelper(this); dbHelper.createDataBase();
 * SQLiteDatabase db = dbHelper.getWritableDatabase(); Cursor cursor =
 * db.query() db.execSQL(sqlString); 注意：execSQL不支持带;的多条SQL语句，只能一条一条的执行，晕了很久才明白
 * 见execSQL的源码注释 (Multiple statements separated by ;s are not supported.)
 * 将把assets下的数据库文件直接复制到DB_PATH，但数据库文件大小限制在1M以下
 * 如果有超过1M的大文件，则需要先分割为N个小文件，然后使用copyBigDatabase()替换copyDatabase()
 */

public class DBHelper extends SQLiteOpenHelper {

	// 用户数据库文件的版本
	public static int VERSION = 1;
	// 数据库文件目标存放路径为系统默认位置，wuxin.enroll.prediction是包名
	public static final String DB_PATH = "/data/data/wuxin.enroll.prediction/databases/";
	
	public static final String DB_NAME = "enroll_data.db";
	public static String ASSETS_DB_NAME = "wuxin_data.db";

	public static final String ID   = "id";     //int:lines,majors,schools,accounts,orders
	public static final String SID  = "sid";    //int:lines,majors
	public static final String YEAR = "year";   //int:lines,majors,orders
	public static final String TYPE = "type";   //int:majors,text:schools
	public static final String NAME = "name";   //text:majors,schools
	public static final String AVG  = "avg";    //text:lines,majors
	public static final String MIN  = "min";    //text:lines,majors
	public static final String MAX  = "max";    //text:lines,majors
	public static final String CODE = "code";   //text:lines,majors,schools
	
	public static final String ACCOUNT_TABLE = "accounts";
	public static final String ACCOUNT  = "account";  //text
	public static final String PASSWORD = "password"; //text
	public static final String LOGIN    = "login";    //text
	public static final String REGION   = "region";   //text

	public static final String LINES_TABLE = "lines";//4+3
	public static final String CONTROL = "control";  //text
	public static final String CATE    = "cate";     //text

	public static final String MAJORS_TABLE = "majors"; //6+1

	public static final String SCHOOLS_TABLE = "schools"; //3+2
	public static final String LOCATION  = "location";    //text
	
	public static final String ORDERS_TABLE = "orders"; //3+2
	public static final String CLASS  = "_class";    //int 0理科， 1文科
	public static final String SCORE  = "_score";    //int 
	public static final String TOTAL  = "_total";    //int
	public static final String ORDER  = "_order";    //int
	
	public static final String SETTINGS = "settings"; //存储一些其他数据，以键值对形式
	public static final String KEY_NAME = "_name";    
	public static final String KEY_VALUE = "_value";    

	private SQLiteDatabase myDataBase = null;
	private final Context myContext;

	/**
	 * 如果数据库文件较大，使用FileSplit分割为小于1M的小文件
	 */
	// 第一个文件名后缀
	private static final int ASSETS_SUFFIX_BEGIN = 101;
	// 最后一个文件名后缀
	private static final int ASSETS_SUFFIX_END = 103;

	/**
	 * 在SQLiteOpenHelper的子类当中，必须有该构造函数
	 * 
	 * @param context
	 *            上下文对象
	 * @param name
	 *            数据库名称
	 * @param factory
	 *            一般都是null
	 * @param version
	 *            当前数据库的版本，值必须是整数并且是递增的状态
	 */
	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		// 必须通过super调用父类当中的构造函数
		super(context, name, null, version);
		this.myContext = context;
	}

	public DBHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public DBHelper(Context context, int version) {
		this(context, DB_PATH + DB_NAME, version);
		VERSION = version;
	}

	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		if (dbExist) {
			// 数据库已存在，do nothing
		} else {
			// 创建数据库
			try {
				File dir = new File(DB_PATH);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				File dbf = new File(DB_PATH + DB_NAME);
				if (dbf.exists()) {
					dbf.delete();
				}
				SQLiteDatabase.openOrCreateDatabase(dbf, null);
				// 复制asseets中的db文件到DB_PATH下
				copyDataBase();
				//写入数据库版本号
				SQLiteDatabase.openOrCreateDatabase(dbf, null).setVersion(VERSION);
			} catch (IOException e) {
				throw new Error("数据库创建失败");
			}
		}
	}

	// 检查数据库是否有效
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		String myPath = DB_PATH + DB_NAME;
		try {
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}
	
	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 */
	private void copyDataBase() throws IOException {
		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(ASSETS_DB_NAME);
		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;
		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);
		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	// 复制assets下的大数据库文件时用这个
	@SuppressWarnings("unused")
	private void copyBigDataBase() throws IOException {
		InputStream myInput;
		String outFileName = DB_PATH + DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFileName);
		for (int i = ASSETS_SUFFIX_BEGIN; i < ASSETS_SUFFIX_END + 1; i++) {
			myInput = myContext.getAssets().open(ASSETS_DB_NAME + "." + i);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
			myInput.close();
		}
		myOutput.close();
	}

	@Override
	public synchronized void close() {
		if (myDataBase != null) {
			myDataBase.close();
		}
		super.close();
	}

	/**
	 * 该函数是在第一次创建的时候执行， 实际上是第一次得到SQLiteDatabase对象的时候才会调用这个方法
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	/**
	 * 数据库表结构有变化时采用
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion == 2 && oldVersion == 1) {
			db.execSQL("DROP TABLE IF EXISTS " + SETTINGS);
			db.execSQL("DROP TABLE IF EXISTS " + ACCOUNT_TABLE);
			String sql = "CREATE TABLE " + ACCOUNT_TABLE + " ( "
					+ ID + " INTEGER PRIMARY KEY , " 
					+ ACCOUNT + " VERCHAR(50) NOT NULL , "
					+ PASSWORD + " VERCHAR(50) NOT NULL , "
					+ NAME + " VERCHAR(50) NOT NULL DEFAULT 'TEST', "
					+ LOGIN + " TEXT NOT NULL DEFAULT 'false', "
					+ CLASS + " INTEGER NOT NULL DEFAULT '-1', "
					+ SCORE + " INTEGER NOT NULL DEFAULT '-1', "
					+ ORDER + " INTEGER NOT NULL DEFAULT '-1', "
					+ " UNIQUE (id,account) )";
			db.execSQL(sql);
			sql = "CREATE TABLE " + SETTINGS + " ( "
					+ ID + " INTEGER PRIMARY KEY , " 
					+ KEY_NAME + " VERCHAR(50) NOT NULL , "
					+ KEY_VALUE + " TEXT NOT NULL DEFAULT '-1', "
					+ " UNIQUE (id,_name) )";
			db.execSQL(sql);
		}
	}

}