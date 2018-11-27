import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SystemCatalog {
	
	public static void listAllTypes() throws IOException {
		System.out.println("listing all types...");
		File catalogFile = new File( C.PARENT_PATH + C.CATALOG_FILE );
		if( !catalogFile.exists() ) return;
		RandomAccessFile file; 
		try {
			file  = new RandomAccessFile( catalogFile, "r");
		}
		catch(FileNotFoundException e) {
			System.out.println("System catalog does not exist.");
			return;
		}
		byte[] name = new byte[C.NAME_SIZE];
		while(true) {
			try {
				file.read(name);
				Type type = new Type();
				type.typeName = Type.getNameFromMarked( ByteUtility.bytesToString(name) ) ;
				int numOfFields = file.readInt();
				type.fieldNames = new String[numOfFields];
				for(int i=0; i<numOfFields; i++) {
					file.read(name);
					type.fieldNames[i] = Type.getNameFromMarked( ByteUtility.bytesToString(name) ) ;
				}
				file.seek(file.getFilePointer() + C.LONG_SIZE);
				type.printType();
			}
			catch (EOFException e) {
				break;
			}
		}
		file.close();
	}
		
	public static long getNextEmptyPage(Type type) throws IOException {
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(C.PARENT_PATH + C.CATALOG_FILE, "r");
		} 
		catch (FileNotFoundException e) {
			System.out.println("System catalog file does not exist.");
			throw new IOException();
		}
		type.seekToType(file);
		file.seek(file.getFilePointer() + C.NAME_SIZE);
		int numOfFields = file.readInt();
		file.seek(file.getFilePointer() + numOfFields * C.NAME_SIZE);
		return file.readLong();
	}
	
}
