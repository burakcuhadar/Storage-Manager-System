import java.io.IOException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		Scanner reader = new Scanner(System.in);
		System.out.println("Please enter your operation: ");
		String command = reader.nextLine();
		while( !command.equals("quit") ) {
			try {
				parser(command);
			}
			catch ( NoSuchElementException e) {
				System.out.println("You have entered the operation in wrong format. Try again:");
			}
			System.out.println("Please enter your operation: ");
			command = reader.nextLine();
		}
		reader.close();
	}
	
	public static void parser( String command ) throws InputMismatchException{
		Scanner reader = new Scanner(command);
		String operation = reader.next();
		String object = reader.next();
		switch(operation) {
		case "create":
			if(object.equals("record"))
				createRecord(reader);
			else if(object.equals("type"))
				createType(reader);
			else
				System.out.println("You can 'create record' or 'create type'. Please try again.");
			break;
		case "delete":
			if(object.equals("record"))
				deleteRecord(reader);
			else if(object.equals("type"))
				deleteType(reader);
			else
				System.out.println("You can 'delete record' or 'delete type'. Please try again.");
			break;
		case "list":
			if(object.equals("record"))
				listRecords(reader);
			else if(object.equals("type"))
				listTypes(reader);
			else
				System.out.println("You can 'list record' or 'list type'. Please try again.");
			break;
		case "search":
			if(object.equals("record"))
				searchRecord(reader);
			else
				System.out.println("You can 'search record'. Please try again.");
			break;
		default:
			System.out.println("You entered invalid operation. Please try again.");
		}
	}
	
	public static void searchRecord(Scanner reader) throws InputMismatchException, NoSuchElementException{
		String typeName = reader.next();
		int key 		= reader.nextInt();
		try {
			Type type		= new Type(typeName, Type.getNumberOfFields(typeName));
			Record.searchRecord(key, type);
		}
		catch(IOException e) {
			System.out.println("Record could not be searched. Please try again.");
		}
	}
	public static void listTypes(Scanner reader) {
		try {
			SystemCatalog.listAllTypes();
		} catch (IOException e) {
			System.out.println("Error occurred while trying to list types. Please try again.");
		}
	}
	public static void listRecords(Scanner reader) throws NoSuchElementException {
		String typeName = reader.next();
		try {
			Type type = new Type(typeName, Type.getNumberOfFields(typeName));
			Record.listRecords(type);
		}
		catch(IOException e) {
			System.out.println("Error occurred while trying to list records. Please try again.");
		}
	}
	public static void createRecord(Scanner reader) throws InputMismatchException, NoSuchElementException{
		String typeName = reader.next();
		try {
			int numberOfFields = Type.getNumberOfFields(typeName);
			if(numberOfFields == 0) {
				System.out.println("The type " + typeName + " has not been created yet.");
				return;
			}
			int[] fields = new int[numberOfFields];
			for(int i=0; i<numberOfFields; i++) {
				fields[i] = reader.nextInt();
			}
			Record.createRecord(typeName, fields);
		} 
		catch (IOException e) {
			System.out.println("Error occured. Record could not be created.");
		}
	}
	public static void createType(Scanner reader) throws InputMismatchException, NoSuchElementException {
		String typeName = reader.next();
		int numberOfFields = reader.nextInt();
		String[] fields = new String[numberOfFields];
		for(int i=0; i<numberOfFields; i++) {
			fields[i] = reader.next();
		}
		Type type = new Type(typeName, numberOfFields, fields);
		try {
			type.createType();
		} catch (IOException e) {
			System.out.println("Error occured. Type could not be created.");
		}
	}
	public static void deleteRecord(Scanner reader) throws InputMismatchException, NoSuchElementException {
		String typeName = reader.next();
		int key 		= reader.nextInt();
		Type type;
		try {
			type = new Type(typeName, Type.getNumberOfFields(typeName));
			Record.deleteRecord(type, key);
		} 
		catch (IOException e) {
			System.out.println("Error occurred while deleting. Try again.");
		}
	}
	public static void deleteType(Scanner reader) throws NoSuchElementException{
		String typeName = reader.next();
		try {
			int numberOfFields = Type.getNumberOfFields(typeName);
			Type type = new Type(typeName, numberOfFields);
			type.deleteType();
		}
		catch(IOException e) {
			System.out.println("Type could not be deleted. Try again.");
		}
	}
}
