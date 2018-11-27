import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Record {
	
	long pos;
	int addressOfNextEmptyRecord; //header
	int[] fields;
	
	public Record() { }
	
	public Record(int[] fields) { this.fields = fields.clone(); }
	
	//returns old address of next empty record
	public int writeRecord(RandomAccessFile file) throws IOException {
		long initialPos = file.getFilePointer();
		file.seek(this.pos);
		int nextEmptyRecord = file.readInt();
		file.seek(file.getFilePointer()-C.INT_SIZE);
		file.writeInt(this.addressOfNextEmptyRecord);
		for(int field : fields)
			file.writeInt(field);
		file.seek(initialPos);
		return nextEmptyRecord;
	}
	
	public static void createRecord(String typeName, int[] fields) throws IOException {
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(C.PARENT_PATH + typeName, "rw");
		} 
		catch (FileNotFoundException e) {
			System.out.println("This type has not been defined yet. You cannot create a record before defining it.");
			throw new IOException();
		}
		Record record  	= new Record(fields);
		Type type     	= new Type(typeName, fields.length);
		Page page		= Page.readPageHeader(file, type, SystemCatalog.getNextEmptyPage(type));
		record.pos  	= page.pos + page.header.addressOfNextEmptyRecord;
		record.addressOfNextEmptyRecord = C.FULL;
		int oldNextEmptyRecord = record.writeRecord(file);
		page.setNextEmptyRecord(file, oldNextEmptyRecord);
		if(oldNextEmptyRecord == C.NULL_ADDRESS) {
			if(page.header.addressOfNextEmptyPage == C.NULL_ADDRESS) {
				long addressOfNewPage = file.length();
				Page.writeEmptyPage(type);
				type.setNextEmptyPage(addressOfNewPage);
			}
			else {
				type.setNextEmptyPage(page.header.addressOfNextEmptyPage);
				page.setNextEmptyPage(file, C.NULL_ADDRESS);
			}
		}
		System.out.println("the page with the new record is written successfully.");
		file.close();
	}
	
	
	public static void deleteRecord(Type type, int key) throws IOException {
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(C.PARENT_PATH + type.typeName, "rw");
		} 
		catch (FileNotFoundException e) {
			System.out.println("This type has not been defined yet. You cannot delete a record before defining it.");
			throw new IOException();
		}
		for(long pos=0; pos<file.length(); pos += C.PAGE_SIZE) {
			Page page = Page.readPage(file, type, pos);
			System.out.println("searching record to be deleted in the page...");
			for(Record r : page.records) {
				if(r.addressOfNextEmptyRecord == C.FULL) {
					if(r.fields[0] == key) {
						System.out.println("record found.");
						r.addressOfNextEmptyRecord = page.header.addressOfNextEmptyRecord;
						r.writeRecord(file);
						page.setNextEmptyRecord( file, (int)(r.pos - page.pos) );
						if(page.header.addressOfNextEmptyPage == C.NULL_ADDRESS) {
							page.setNextEmptyPage(file, type.getNextEmptyPage()); 
							type.setNextEmptyPage(page.pos);
						}
						else {
							//do nothing
						}
						System.out.println("record deleted succesfully.");
						return;
					}
				}
			}
			System.out.println("the record could not be found in this page.");
		}
	}
	
	public static void listRecords(Type type) throws IOException {
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(C.PARENT_PATH + type.typeName, "r");
		} 
		catch (FileNotFoundException e) {
			System.out.println("This type has not been defined yet. You cannot list records of a type before defining it.");
			throw new IOException();
		}
		for(long pos=0; pos<file.length(); pos += C.PAGE_SIZE) {
			Page page = Page.readPage(file, type, pos);
			System.out.println("listing records in the page...");
			for(Record r : page.records) {
				if(r.addressOfNextEmptyRecord == C.FULL ) {
					for(int field : r.fields)
						System.out.print(field + "\t");
					System.out.println();
				}
			}
		}
	}
	public static void searchRecord(int key, Type type) throws IOException {
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(C.PARENT_PATH + type.typeName, "r");
		} 
		catch (FileNotFoundException e) {
			System.out.println("This type has not been defined yet. You cannot list records of a type before defining it.");
			throw new IOException();
		}
		for(long pos=0; pos<file.length(); pos += C.PAGE_SIZE) {
			Page page = Page.readPage(file, type, pos);
			System.out.println("searching the record in the page...");
			for(Record r : page.records) {
				if(r.addressOfNextEmptyRecord == C.FULL) {
					if(r.fields[0] == key) {
						System.out.println("record found");
						for(int field : r.fields)
							System.out.print(field + "\t");
						System.out.println();
						return;
					}
				}
			}
		}
		System.out.println("record could not be found");
	}
}
