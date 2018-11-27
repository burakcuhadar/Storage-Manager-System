import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class Page {
	
	public class PageHeader {
		long addressOfNextEmptyPage;
		int addressOfNextEmptyRecord;
		
		public PageHeader() {
			addressOfNextEmptyPage 	 = -1;
			addressOfNextEmptyRecord = -1;
		}
		public PageHeader(long page, int record) {
			addressOfNextEmptyPage 	 = page;
			addressOfNextEmptyRecord = record;
		}
	};
	
	public long		  pos;
	public Record[]   records;
	public PageHeader header = new PageHeader();
	
	public static Page readPageHeader(RandomAccessFile file, Type type, long pos) throws IOException {
		long initialPos = file.getFilePointer();
		file.seek(pos);
		Page page = new Page();
		page.pos  = pos;
		page.header.addressOfNextEmptyPage = file.readLong();
		page.header.addressOfNextEmptyRecord = file.readInt();
		file.seek(initialPos);
		return page;
	}
	//requirements: type.numberOfFields
	public static Page readPage(RandomAccessFile file, Type type, long pos) throws IOException {
		System.out.println("reading page...");
		file.seek(pos);
		byte[] page = new byte[C.PAGE_SIZE];
		file.read(page);
		Page p = new Page();
		p.pos  = pos;
		int currentPos = 0;
		p.header.addressOfNextEmptyPage = ByteUtility.convertArrayToLong(page, currentPos);
		currentPos += C.LONG_SIZE;
		p.header.addressOfNextEmptyRecord = ByteUtility.convertArrayToInt(page, currentPos);
		currentPos += C.INT_SIZE;
		int numberOfRecords = (C.PAGE_SIZE - C.LONG_SIZE - C.INT_SIZE) / type.getRecordSize();
		p.records = new Record[numberOfRecords];
		for(int i=0; i<numberOfRecords; i++) {
			p.records[i] = new Record();
			p.records[i].pos = p.pos +  (long) currentPos;
			p.records[i].addressOfNextEmptyRecord = ByteUtility.convertArrayToInt(page, currentPos);
			currentPos += C.INT_SIZE;
			p.records[i].fields = new int[type.numberOfFields];
			for(int j=0; j<type.numberOfFields; j++ ) {
				p.records[i].fields[j] = ByteUtility.convertArrayToInt(page, currentPos);
				currentPos += C.INT_SIZE;
			}
		}
		return p;
	}
	

	public static void writeEmptyPage(Type type) throws IOException{
		System.out.println("creating empty page...");
		File f = new File( C.PARENT_PATH + type.typeName );
		if( !f.exists() ) f.createNewFile();
		RandomAccessFile file;
		try {
			file = new RandomAccessFile( f, "rw");
		}
		catch(FileNotFoundException e) {
			System.out.println("The type has not been defined yet.");
			throw new IOException();
		}
		file.seek( f.length() );
		file.write( createEmptyPage(type) );
		file.close();
	}
	
	public static byte[] createEmptyPage(Type type) {
		byte[] page = new byte[ C.PAGE_SIZE ]; 
		long nextEmptyPage = (long) C.NULL_ADDRESS;
		int currentPos = 0;
		currentPos = ByteUtility.copyLongToArray(nextEmptyPage, page, currentPos);
		int nextEmptyRecord = C.LONG_SIZE + C.INT_SIZE;
		currentPos = ByteUtility.copyIntToArray(nextEmptyRecord, page, currentPos);
		int recordSize=type.getRecordSize();
		while( currentPos < C.PAGE_SIZE ) {
			if( (C.PAGE_SIZE - currentPos) < recordSize)
				break;
			else if( (C.PAGE_SIZE - currentPos) < 2 * recordSize )
				currentPos = ByteUtility.copyIntToArray(C.NULL_ADDRESS, page, currentPos);
			else
				currentPos = ByteUtility.copyIntToArray(currentPos + recordSize, page, currentPos);
			for(int j=0; j<type.numberOfFields; j++)
				currentPos = ByteUtility.copyIntToArray(0, page, currentPos);
		}
		return page;
	}
	
	public void setNextEmptyRecord(RandomAccessFile file, int nextEmptyRecord) throws IOException {
		long initialPos = file.getFilePointer();
		file.seek(this.pos + C.LONG_SIZE);
		file.writeInt(nextEmptyRecord);
		file.seek(initialPos);
	}
	public void setNextEmptyPage(RandomAccessFile file, long pos) throws IOException {
		long initialPos = file.getFilePointer();
		file.seek(this.pos);
		file.writeLong(pos);
		file.seek(initialPos);
	}
	
}
