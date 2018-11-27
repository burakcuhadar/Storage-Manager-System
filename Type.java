import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class Type {	
	String   typeName;
	String[] fieldNames;
	int      numberOfFields;
	long     nextEmptyPage;
	
	public Type(String typeName, int numberOfFields) {
		this.typeName       = typeName;
		this.numberOfFields = numberOfFields;
		this.nextEmptyPage  = 0L;
	}
	public Type(String typeName, int numberOfFields, String[] fields) {
		this.typeName       = typeName;
		this.numberOfFields = numberOfFields;
		this.nextEmptyPage  = 0L;
		this.fieldNames     = copyStringArr(fields);
	}
	public Type() {	}

	public int getRecordSize() {
		return C.INT_SIZE * (numberOfFields + 1 ); // +1 is for next empty record address
	}
	
	public int getCatalogSize() {
		return C.NAME_SIZE * (numberOfFields + 1) + C.INT_SIZE +  C.LONG_SIZE;
	}
	
	public static String[] copyStringArr(String[] arr) {
		String[] result = new String[arr.length];
		for(int i=0; i<arr.length; i++)
			result[i] = new String(arr[i]);
		return result;
	}
	
	public void createType() throws IOException {
		File typeFile = new File( C.PARENT_PATH + typeName );
		if( !typeFile.exists() ) typeFile.createNewFile();
		else {
			System.out.println("this type already exists!");
			throw new IOException();
		}
		Page.writeEmptyPage(this);
		File catalogFile = new File( C.PARENT_PATH + C.CATALOG_FILE );
		if( !catalogFile.exists() ) catalogFile.createNewFile();
		RandomAccessFile file = new RandomAccessFile( catalogFile, "rw");
		file.seek( catalogFile.length() );
		file.write( this.typeToByteArr() );
		file.close();
		System.out.println("type is created successfully.");
	}
	
	public byte[] typeToByteArr() {
		byte[] catalogRecord = new byte[ getCatalogSize() ];
		int currentPos = 0;
		currentPos = ByteUtility.copyStringToArray(typeName, catalogRecord, currentPos);
		currentPos = ByteUtility.copyIntToArray(numberOfFields, catalogRecord, currentPos);
		for(String name : fieldNames) {
			currentPos = ByteUtility.copyStringToArray(name, catalogRecord, currentPos);
		}
		currentPos = ByteUtility.copyLongToArray(nextEmptyPage, catalogRecord, currentPos);
		return catalogRecord;
	}
	
	public void deleteType() throws IOException { 
		File typeFile = new File( C.PARENT_PATH + typeName );
		if( !typeFile.exists() ) {
			System.out.println("This type has not been defined yet. You cannot delete it.");
			return;
		}
		typeFile.delete();
		File catalogFile = new File(C.PARENT_PATH + C.CATALOG_FILE );
		File newCatalogFile = new File(C.PARENT_PATH + C.CATALOG_FILE + ".new");
		RandomAccessFile catalog = new RandomAccessFile( catalogFile, "r");
		RandomAccessFile newCatalog = new RandomAccessFile( newCatalogFile, "rw");
		long typePos = this.seekToType(catalog);
		catalog.seek(0);
		for(long pos=0; pos < typePos; pos += C.BUFFER_SIZE) {
			byte[] buffer;
			if(typePos - pos < C.BUFFER_SIZE)
				buffer = new byte[(int) (typePos - pos)];
			else
				buffer = new byte[ C.BUFFER_SIZE ];
			catalog.read(buffer);
			newCatalog.write(buffer);
		}
		catalog.seek(catalog.getFilePointer() + this.getCatalogSize());
		for(long pos=catalog.getFilePointer(), catalogLength=catalog.length(); pos < catalogLength; pos += C.BUFFER_SIZE) {
			byte[] buffer;
			if(catalogLength - pos < C.BUFFER_SIZE)
				buffer = new byte[(int) (catalogLength - pos)];
			else
				buffer = new byte[ C.BUFFER_SIZE ];
			catalog.read(buffer);
			newCatalog.write(buffer);
		}
		catalog.close();
		newCatalog.close();
		catalogFile.delete();
		newCatalogFile.renameTo(catalogFile);
		System.out.println("type is deleted successfully.");
	}
	
	public long seekToType(RandomAccessFile file) throws IOException {
		while(true) {
			try {
				byte[] stringBytes = new byte[C.NAME_SIZE];
				file.read(stringBytes);
				String s = new String(stringBytes,StandardCharsets.UTF_8);
				if(typeName.equals( Type.getNameFromMarked(s) )) {
					long pos = file.getFilePointer() - C.NAME_SIZE;
					file.seek(pos);
					return pos;
				}
				//skip number of fields and field names
				int numOfFields = file.readInt();
				file.seek(file.getFilePointer() + numOfFields * C.NAME_SIZE + C.LONG_SIZE);
			}
			catch(EOFException e ) {
				break;
			}
		}
		return -1L;
	}
	public static int getNumberOfFields(String typeName) throws IOException {
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(C.PARENT_PATH + C.CATALOG_FILE, "r");
		} catch (FileNotFoundException e) {
			System.out.println("This type has not been defined yet.");
			return 0;
		}
		while(true) {
			try {
				byte[] stringBytes = new byte[C.NAME_SIZE];
				file.read(stringBytes);
				String s = new String(stringBytes,StandardCharsets.UTF_8);
				int numOfFields = file.readInt();
				if(typeName.equals( Type.getNameFromMarked(s) )) {
					file.close();
					return numOfFields;
				}
				file.seek(file.getFilePointer() + numOfFields * C.NAME_SIZE + C.LONG_SIZE);
			}
			catch(EOFException e ) {
				break;
			}
		}
		file.close();
		return 0;
	}
	
	public static String getNameFromMarked(String s) {
		int i = s.indexOf(C.PADDING_CHAR);
		if(i == -1)
			return s;
		else
			return s.substring(0, i);
	}

	public void setNextEmptyPage(long pos) throws IOException {
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(C.PARENT_PATH + C.CATALOG_FILE, "rw");
		} catch (FileNotFoundException e) {
			throw new IOException();
		}
		this.seekToType(file);
		file.seek(file.getFilePointer() + C.NAME_SIZE * (numberOfFields + 1) + C.INT_SIZE);
		file.writeLong(pos);
		file.close();
	}
	public long getNextEmptyPage() throws IOException {
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(C.PARENT_PATH + C.CATALOG_FILE, "r");
		} catch (FileNotFoundException e) {
			throw new IOException();
		}
		this.seekToType(file);
		file.seek(file.getFilePointer() + C.NAME_SIZE * (numberOfFields + 1) + C.INT_SIZE);
		long pagePos = file.readLong();
		file.close();
		return pagePos;
	}
	public void printType() {
		System.out.print( this.typeName + "( " );
		for(String field : fieldNames)
			System.out.print(field + "  " );
		System.out.println(" )");
	}
}
