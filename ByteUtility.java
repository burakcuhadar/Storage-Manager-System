import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteUtility { 
    public static final int NAME_MAX_LENGTH = 30;
    public static final char PADDING_CHAR = '!';

    public static byte[] longToBytes(long x) {
    	ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, x);
        return buffer.array();
    }
    public static byte[] intToBytes(int x) {
    	ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(0, x);
        return buffer.array();
    }
    public static byte[] stringToBytes(String x) {
    	String s = fillString(x);
        return s.getBytes(StandardCharsets.UTF_8);
    }
    public static String fillString(String s) {
    	char[] fill = new char[NAME_MAX_LENGTH - s.length()];
    	Arrays.fill(fill, PADDING_CHAR );
    	StringBuilder sb = new StringBuilder(NAME_MAX_LENGTH);
    	sb.append(s);
    	sb.append(fill);
    	return sb.toString();
    }

    public static long bytesToLong(byte[] bytes) {
    	ByteBuffer buffer = ByteBuffer.allocate(C.LONG_SIZE);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getLong();
    }
   
    public static String bytesToString(byte[] bytes) {
    	return new String(bytes,StandardCharsets.UTF_8);
    }
    
    public static long convertArrayToLong(byte[] source, int source_pos) {
    	ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(source, source_pos, C.LONG_SIZE);
        buffer.flip();//need flip 
        return buffer.getLong();
    }
    public static int convertArrayToInt(byte[] source, int source_pos) {
    	ByteBuffer buffer = ByteBuffer.allocate(C.INT_SIZE);
        buffer.put(source, source_pos, C.INT_SIZE);
        buffer.flip();//need flip 
        return buffer.getInt();
    }
    public static int copyLongToArray(long source, byte[] dest, int dest_pos) {
    	byte[] sourceArray  = longToBytes(source); 
    	System.arraycopy(  sourceArray , 0, dest, dest_pos, sourceArray.length);
    	return dest_pos + sourceArray.length;
    }
    
    public static int copyIntToArray(int source, byte[] dest, int dest_pos) {
    	byte[] sourceArray  = intToBytes(source); 
    	System.arraycopy(  sourceArray , 0, dest, dest_pos, sourceArray.length);
    	return dest_pos + sourceArray.length;
    }
   
    public static int copyStringToArray(String source, byte[] dest, int dest_pos) {
    	byte[] sourceArray  = stringToBytes(source); 
    	System.arraycopy(  sourceArray , 0, dest, dest_pos, sourceArray.length);
    	return dest_pos + sourceArray.length;
    }
}