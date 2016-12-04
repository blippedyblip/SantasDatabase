import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class ChristmasApplication
{

	private Connection conn;
	private int numChildren,numGifts,numHelpers;
	
	public ChristmasApplication()
	{

	}

	public void connectToDB(String url, String username, String pass)
	{

		System.out.println("Example showing connection to mod-intro-databases server");

		try
		{

			// Load the PostgreSQL JDBC driver
			Class.forName("org.postgresql.Driver");

		}
		catch (ClassNotFoundException ex)
		{
			System.out.println("Driver not found");
			System.exit(1);
		}

		System.out.println("PostgreSQL driver registered.");

		conn = null;
		
		try
		{
			conn = DriverManager.getConnection(url, username, pass);
		}
		catch (SQLException ex)
		{
			System.out.println("Ooops, couldn't get a connection");
			System.out.println("Check that <username> & <password> are right");
			System.exit(1);
		}

		if (conn != null)
		{
			System.out.println("Database accessed!");
		}
		else
		{
			System.out.println("Failed to make connection");
			System.exit(1);
		}
		
		numChildren = 1000;
		numGifts = 30;
		numHelpers = 30;
		
		clearPresent();
		clearChild();
		clearHelper();
		clearGift();
		
		populateChildren(numChildren);
		populateGifts(numGifts);
		populateHelpers(numHelpers);
		populatePresents(numChildren);
		
		Scanner lineReader = new Scanner(System.in);
		Scanner intReader = new Scanner(System.in);

		while(true)
		{
			try
			{
				System.out.println("Which report would you like? c - child, h - helper, e - exit");
				String line = lineReader.nextLine();
				
				if(line.equals("c"))
				{
					System.out.println("Enter cid");
					int num = intReader.nextInt();
					
					if(num > 0 && num <= numChildren)
					{
						childReport(num);
					}
					else 
					{
						System.out.println("Invalid cid");
					}
				}
				else if(line.equals("h"))
				{
					System.out.println("Enter slhid");
					int num = intReader.nextInt();
					
					if(num > 0 && num <= numHelpers)
					{
						helperReport(num);
					}
					else 
					{
						System.out.println("Invalid slhid");
					}
				}
				else if(line.equals("e"))
				{
					break;
				}
				else
				{
					System.out.println("Invalid input");
				}
			}
			catch(Exception e)
			{
				System.out.println("Invalid input");
			}
			
		}
		// Now, just tidy up by closing connection
		
		try
		{
			conn.close();
			System.out.println("Connection closed");
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/*
	 * 
	 * 
	 * REPORTS
	 * 
	 * 
	 * 
	 */
	
	
	private void childReport(int cid)
	{
		try
		{
			PreparedStatement childQuery = conn.prepareStatement("SELECT * FROM Child WHERE cid = "+cid+";");
			ResultSet rsc = childQuery.executeQuery();
			
			rsc.next();
			System.out.println("ID: "+rsc.getInt("cid"));
			System.out.println("Name: "+ rsc.getString("name"));
			System.out.println("Address: " + rsc.getString("address"));
			
			//select from gifts where gift.gid = present.gid where cid = 'javacid' 
			PreparedStatement presentQuery = conn.prepareStatement("SELECT * FROM Gift WHERE Gift IN (SELECT Gift FROM Present WHERE Gift.gid = Present.gid AND Present.cid = "+cid+");");
			ResultSet rsp = presentQuery.executeQuery();
			
			String gid;
			String description;
			
			// Iterate through the gifts
			while (rsp.next())
			{
				gid = ((Integer) rsp.getInt("gid")).toString();
				description = rsp.getString("description");
				System.out.println(gid+": "+description);
			}
		}
		catch (SQLException sqlE)
		{
			System.out.println("SQL code is broken");

		}
	}
	
	private void helperReport(int slhid)
	{
		try
		{
			PreparedStatement helperQuery = conn.prepareStatement("SELECT * FROM SantasLittleHelper WHERE slhid = "+slhid+";");
			ResultSet rsh = helperQuery.executeQuery();
			
			rsh.next();
			System.out.println("ID: "+rsh.getInt("slhid"));
			System.out.println("Name: "+ rsh.getString("name"));
			
			//Find all with helper id in presents and order by cid
			PreparedStatement presentQuery = conn.prepareStatement("SELECT * FROM Present WHERE slhid = "+slhid+" ORDER BY cid;");
			ResultSet rsp = presentQuery.executeQuery();
			
			int currentcid = 0;
			
			while(rsp.next())
			{
				int newcid = rsp.getInt("cid");
				int newgid = rsp.getInt("gid");
				
				//if a new cid find name and address
				if(newcid != currentcid)
				{
					PreparedStatement childQuery = conn.prepareStatement("SELECT name,address FROM Child WHERE cid = "+newcid+";");
					ResultSet rsc = childQuery.executeQuery();
					
					rsc.next();
					System.out.println("Child Name: "+rsc.getString("name"));
					System.out.println("Child Address :"+rsc.getString("address"));
					currentcid = newcid;
				}
				
				PreparedStatement giftQuery = conn.prepareStatement("SELECT * FROM Gift WHERE Gift.gid ="+newgid+";");
				ResultSet rsg = giftQuery.executeQuery();
				rsg.next();
				String gid = ((Integer) rsg.getInt("gid")).toString();
				String description = rsg.getString("description");
				System.out.println(gid+": "+description);
			}
		}
		catch (SQLException sqlE)
		{
			System.out.println("SQL code is broken");

		}
		
		//ID
		//Name
		//Finds child in present table - finds all data with helper's id - then orders it by child, and then iterates through until cid changes... giving a new name each time
	}
	
	
	/*
	 * 
	 *  
	 *  ADDING
	 *  
	 *  
	 */
	
	private void addChild(int cid,String name, String address)
	{
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO Child VALUES("+cid+",'"+name+"','"+address+"');");
			
			System.out.println("Added Child");
		}
		catch (SQLException sqlE)
		{
			System.out.println("AddChild code is broken");

		}
	}
	
	
	
	private void addHelper(int slhid,String name)
	{
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO SantasLittleHelper VALUES("+slhid+",'"+name+"');");
			
			System.out.println("Added Helper");
		}
		catch (SQLException sqlE)
		{
			System.out.println("AddHelper code is broken");

		}
	}

	private void addGift(int gid,String description)
	{
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO Gift VALUES("+gid+",'"+description+"');");
			
			System.out.println("Added Gift");
		}
		catch (SQLException sqlE)
		{
			System.out.println("AddHelper code is broken");

		} 
	}
	
	/*
	 * 
	 * 
	 * POPULATING DATA AND FORMING PRESENT TABLE
	 * 
	 * 
	 */
	
	private void populateChildren(int n)
	{
		System.out.println("Populating Children table");
		
		//Data for realistic child
		String realname = "Callum";
		String realaddress = "435 Roadname Road";
		addChild(1,realname,realaddress);
		
		//Auto generated child data
		for(int i=1;i<n;i++)
		{
			int cid=i+1;
			String name = "Child"+((Integer) cid).toString();
			String address = "Address"+((Integer) cid).toString();
			
			addChild(cid,name,address);
		}
	}
	
	private void populateHelpers(int n)
	{
		System.out.println("Populating Helper table");
		
		//Data for realistic helper
		String realname = "Elf";
		addHelper(1,realname);
		
		//Auto generated child data
		for(int i=1;i<n;i++)
		{
			int slhid=i+1;
			String name = "SLH"+((Integer) slhid).toString();
			
			addHelper(slhid,name);
		}
	}
	
	private void populateGifts(int n)
	{
		System.out.println("Populating Gift table");
		
		//Data for realistic gift
		String bikeDescription = "A bike. The coolest looking bike in existence.";
		String ps4Description = "A ps4.";
		String waterPistolDescription = "A giant water pistol. The coolest looking water pistol in existence.";
		
		addGift(1,bikeDescription);
		addGift(2,ps4Description);
		addGift(3,waterPistolDescription);		
		
		for(int i=3;i<n;i++)
		{
			int gid=i+1;
			String description = "Gift"+((Integer) gid).toString();
			
			addGift(gid,description);
		}
	}
	
	//n is the amount of children - present table size should be 3x the size with this data
	private void populatePresents(int n)
	{
		//populate first 3 presents manually - 1st child matched with 1st helper and given the 1st 3 gifts.
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO Present SELECT Gift.gid,Child.cid,SantasLittleHelper.slhid FROM Gift,Child,SantasLittleHelper WHERE Gift.gid >= 1 AND Gift.gid <= 3 AND Child.cid = 1 AND SantasLittleHelper.slhid = 1;");
			
			System.out.println("Populated 1st child's presents");
		}
		catch (SQLException sqlE)
		{
			System.out.println("1st child's presents code is broken");

		}		
		
		//each child given 3 random gifts from gift, and are given a random helper
		
		for(int i=1; i<n;i++)
		{
			int cid = i+1;
			int slhid1 = (int) (Math.random() * numHelpers) + 1;
			int slhid2 = (int) (Math.random() * numHelpers) + 1;
			int slhid3 = (int) (Math.random() * numHelpers) + 1;
			int gift1 = (int) (Math.random() * numGifts) + 1;
			int gift2 = (int) (Math.random() * numGifts) + 1;
			int gift3 = (int) (Math.random() * numGifts) + 1;
			
			try
			{
				Statement stmt1 = conn.createStatement();
				stmt1.executeUpdate("INSERT INTO Present VALUES("+gift1+","+cid+","+slhid1+");");
				
				Statement stmt2 = conn.createStatement();
				stmt2.executeUpdate("INSERT INTO Present VALUES("+gift2+","+cid+","+slhid2+");");
				
				Statement stmt3 = conn.createStatement();
				stmt3.executeUpdate("INSERT INTO Present VALUES("+gift3+","+cid+","+slhid3+");");
				
			}
			catch (SQLException sqlE)
			{
				System.out.println("1st child's presents code is broken");

			}		
		}
		
		System.out.println("Populated presents");
	}
	
	
	/*
	 * 
	 * 
	 * CLEARING TABLES
	 * 
	 * 
	 */
	
	private void clearChild()
	{
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM Child;");
			
			System.out.println("Deleted all children");
		}
		catch (SQLException sqlE)
		{
			System.out.println("ClearChild code is broken");

		}
	}
	
	private void clearHelper()
	{
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM SantasLittleHelper;");
			
			System.out.println("Deleted all helpers");
		}
		catch (SQLException sqlE)
		{
			System.out.println("ClearHelper code is broken");

		}
	}
	
	private void clearGift()
	{
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM Gift;");
			
			System.out.println("Deleted all gifts");
		}
		catch (SQLException sqlE)
		{
			System.out.println("ClearGift code is broken");

		}
	}
	
	private void clearPresent()
	{
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM Present;");
			
			System.out.println("Deleted all presents");
		}
		catch (SQLException sqlE)
		{
			System.out.println("ClearPresent code is broken");

		}
	}
	
	// Main method to connect to database on the module server.
	public static void main(String[] args)
	{

		String username = "cag559";
		String password = "zrrrhxsca4";
		String database = "cag559";
		String URL = "jdbc:postgresql://mod-intro-databases.cs.bham.ac.uk/" + database;

		ChristmasApplication test = new ChristmasApplication();

		test.connectToDB(URL, username, password);

	}
}