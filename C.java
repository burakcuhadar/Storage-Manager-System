import java.io.File;

public class C {
	public static final char   PADDING_CHAR    	= '!';
	public static final String CATALOG_FILE    	= "SystemCatalog";
	public static final String PARENT_PATH		= System.getProperty("user.dir") + File.separator;
	//in bits
	public static final int    BYTE            	= 8;
	public static final int    NAME_LENGTH     	= 30;
	public static final int    CHAR_SIZE       	= BYTE; // UTF-8
	//in bytes
	public static final int    NAME_SIZE       	= NAME_LENGTH * CHAR_SIZE / BYTE;
	public static final int    LONG_SIZE       	= Long.BYTES;
	public static final int    INT_SIZE        	= Integer.BYTES;
	public static final int    PAGE_SIZE       	= 2048; 
	public static final int    BUFFER_SIZE      = 2048; 
	
	public static final int	   NULL_ADDRESS		= -1;
	public static final int    FULL				= -2;
}
