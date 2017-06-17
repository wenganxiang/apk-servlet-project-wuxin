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
 * �÷��� DBHelper dbHelper = new DBHelper(this); dbHelper.createDataBase();
 * SQLiteDatabase db = dbHelper.getWritableDatabase(); Cursor cursor =
 * db.query() db.execSQL(sqlString); ע�⣺execSQL��֧�ִ�;�Ķ���SQL��䣬ֻ��һ��һ����ִ�У����˺ܾò�����
 * ��execSQL��Դ��ע�� (Multiple statements separated by ;s are not supported.)
 * ����assets�µ����ݿ��ļ�ֱ�Ӹ��Ƶ�DB_PATH�������ݿ��ļ���С������1M����
 * ����г���1M�Ĵ��ļ�������Ҫ�ȷָ�ΪN��С�ļ���Ȼ��ʹ��copyBigDatabase()�滻copyDatabase()
 */

public class DBHelper extends SQLiteOpenHelper {

	// �û����ݿ��ļ��İ汾
	public static int VERSION = 1;
	// ���ݿ��ļ�Ŀ����·��ΪϵͳĬ��λ�ã�wuxin.enroll.prediction�ǰ���
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
	public static final String CLASS  = "_class";    //int 0��ƣ� 1�Ŀ�
	public static final String SCORE  = "_score";    //int 
	public static final String TOTAL  = "_total";    //int
	public static final String ORDER  = "_order";    //int
	
	public static final String SETTINGS = "settings"; //�洢һЩ�������ݣ��Լ�ֵ����ʽ
	public static final String KEY_NAME = "_name";    
	public static final String KEY_VALUE = "_value";    

	private SQLiteDatabase myDataBase = null;
	private final Context myContext;

	/**
	 * ������ݿ��ļ��ϴ�ʹ��FileSplit�ָ�ΪС��1M��С�ļ�
	 */
	// ��һ���ļ�����׺
	private static final int ASSETS_SUFFIX_BEGIN = 101;
	// ���һ���ļ�����׺
	private static final int ASSETS_SUFFIX_END = 103;

	/**
	 * ��SQLiteOpenHelper�����൱�У������иù��캯��
	 * 
	 * @param context
	 *            �����Ķ���
	 * @param name
	 *            ���ݿ�����
	 * @param factory
	 *            һ�㶼��null
	 * @param version
	 *            ��ǰ���ݿ�İ汾��ֵ���������������ǵ�����״̬
	 */
	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		// ����ͨ��super���ø��൱�еĹ��캯��
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
			// ���ݿ��Ѵ��ڣ�do nothing
		} else {
			// �������ݿ�
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
				// ����asseets�е�db�ļ���DB_PATH��
				copyDataBase();
				//д�����ݿ�汾��
				SQLiteDatabase.openOrCreateDatabase(dbf, null).setVersion(VERSION);
			} catch (IOException e) {
				throw new Error("���ݿⴴ��ʧ��");
			}
		}
	}

	// ������ݿ��Ƿ���Ч
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

	// ����assets�µĴ����ݿ��ļ�ʱ�����
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
	 * �ú������ڵ�һ�δ�����ʱ��ִ�У� ʵ�����ǵ�һ�εõ�SQLiteDatabase�����ʱ��Ż�����������
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	/**
	 * ���ݿ��ṹ�б仯ʱ����
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