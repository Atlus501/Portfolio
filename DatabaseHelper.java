// Package Declaration
package DatabaseFiles;

// Local package imports
import usefulTools.ColorLogger;

import usefulTools.StatusCode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
// Other Java Imports
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import managerClasses.RightsManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*******
 * <p> DatabaseHelper Class </p>
 * 
 * <p> Description: A Class that handles all interactions between the database and the application </p>
 * 
 * @author Group 59
 * 
 * @version 1.00	2024-10-09 Phase 1 Submission
 * @version 2.00	2024-10-30 Phase 2 Submission
 * 
 */
public class DatabaseHelper
{
	// JDBC driver name and database URL
	public static final String JDBC_DRIVER = "org.h2.Driver";
	public static String DB_URL = "jdbc:h2:databases./Group59Database";

	// Database credentials
	public static final String DB_USER = "sa";
	public static final String DB_PASS = "";

	// Current Logged in User Data
	private String currentUsername = "";
	private Role currentRole;
	private List<SearchEntry> searchHistory = new ArrayList<SearchEntry>();

	private Connection connection = null;
	private Statement statement = null;
	
	// Rights Management
	public RightsManager rightsManager;
	
	// Local nested classes
	public Users users;
	public Articles articles;
	public InviteCodes inviteCodes;
	public HelpMessages helpMessages;
	
	// SQL string names as to not hard code them into the program
	private final String userbaseTableName = "Group59Accounts";
	private final String groupsTableName = "Groups";
	private final String articlesTableName = "Articles";
	private final String inviteCodesTableName = "InviteCodes";
	private final String inviteCodeSeedTableName = "InviteSeed";
	private final String helpMessagesTableName = "HelpMessages";

	// ================== Local Variables ==================
	private static final boolean doDebugLog = true;
	private final int inviteCodeLength = 24;
	private final int otpPasswordLength = 24;

	public DatabaseHelper()
	{
		// Attempt to connect to database
		try
		{
			Class.forName(JDBC_DRIVER); // Load the JDBC driver

			ColorLogger.loglnColor("Connecting to database...", ColorLogger.TextColor.YELLOW);
			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			statement = connection.createStatement();
			ColorLogger.loglnColor("Connection Established Successfully!", ColorLogger.TextColor.GREEN);
			createAccountTables(); // Create the account tables
			createInviteCodesTable(); // Create the invite codes tables
			createSeedTable(); // Create the table that will store the current seed for invite codes
			createGroupTable();
			createHelpMessagesTable();
			createArticleTable(); // Create the table that will store the articles
			rightsManager = new RightsManager(this); // Create the Rights Management Class
		}
		catch (ClassNotFoundException e)
		{
			// Failed to get JDBC Driver
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
		catch (SQLException e)
		{
			// Failed to load database
			System.err.println(
					"Failed to establish a connection to database! Error thrown on DatabaseHelper.constructor\n"
							+ e.getMessage());
		}
		// Otherwise, connection succeeded
		// Initialize nested classes
		users = new Users();
		articles = new Articles();
		inviteCodes = new InviteCodes();
		helpMessages = new HelpMessages();
	}

	/**
	 * Gets role of current user
	 * @return
	 */
	public Role getCurrentRole() {
		return currentRole;
	}
	
	/**
	 * Sets the current role of the user
	 * @param role
	 */
	public void setCurrentRole(Role role) {
		this.currentRole = role;
	}
	
	/**
	 * Creates the database that we are going to use to store the accounts. The
	 * information that is going to included are the users': real name, preferred
	 * name, username, email, password, if password is onetime, when the one time
	 * password will expire, and the viable roles the users have.
	 */
	private void createAccountTables() throws SQLException
	{
		String userTable = "CREATE TABLE IF NOT EXISTS " + userbaseTableName + " (" + //
				"id INT AUTO_INCREMENT PRIMARY KEY, " + //
				"firstName VARCHAR(255), " + //
				"middleName VARCHAR(255), " + //
				"lastName VARCHAR(255), " + //
				"preferredName VARCHAR(255), " + //
				"username VARCHAR(255) UNIQUE NOT NULL, " + //
				"email VARCHAR(255) UNIQUE, " + //
				"password VARCHAR(255) NOT NULL, " + //
				"oneTimePassword BOOLEAN, " + //
				"oneTimePassExpireDate DATE, " + //
				"role INT NOT NULL, " + // Role as an integer
				"skillLevel INTEGER)";

		statement.execute(userTable);
	}

	// Method that creates the table that will store the articles
	private void createArticleTable() throws SQLException
	{
		//this.clearArticleTable();
		// creates a new table to store all the articles if one doesn't exist already
		String createArticleTableSQL = "CREATE TABLE IF NOT EXISTS " + articlesTableName + " (" + //
				"id BIGINT AUTO_INCREMENT PRIMARY KEY," + //
				"title CLOB(10K)," + //
				"abstractText CLOB(100K)," + //
				"body CLOB(100K)," + //
				"diffLevel INT," + //
				"groupName INT," + //
				"authorList CLOB(50K)," + // Separated by "|"
				"keywords CLOB(50K)," + // Separated by "|"
				"references CLOB(50K)," + // Separated by "|"
				"groupIdentifiers CLOB(10K)," + "groupList CLOB(10K)," + // Separated by "|"
				"allowedRoles INT" + //
				")";

		try (Statement stmt = connection.createStatement())
		{
			stmt.execute(createArticleTableSQL);
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO CREATE ARTICLES TABLE! Happened in DatabaseHelper.createArticleTable\n"
							+ e.getMessage());
			throw e;
		}
	}
	
	// Method that will create the table for the help messages
	private void createHelpMessagesTable() throws SQLException
	{
		String createTableSQL = "CREATE TABLE IF NOT EXISTS " + helpMessagesTableName + " (" //
				+ "id LONG AUTO_INCREMENT PRIMARY KEY, " //
				+ "authorUsername VARCHAR(255) NOT NULL, " //
				+ "problemMessage TEXT NOT NULL, " //
				+ "searchHistory BLOB" //
				+ ");"; //

		try (Statement stmt = connection.createStatement())
		{
			// Execute the create table statement
			stmt.executeUpdate(createTableSQL);
		}
		catch (SQLException e)
		{
			System.err.println("Error creating table: " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * This will be the command that creates the groups database
	 */
	public void createGroupTable() throws SQLException
	{
		// creates a new table to store all the articles if one doesn't exist already
		//this.clearTable();
		// IAN: Did you not double check and rename the local variables?
		String createArticleTableSQL = "CREATE TABLE IF NOT EXISTS " + groupsTableName + " (" + //
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," + //
                "groupName VARCHAR(255) UNIQUE," + //
                "encryptionKey VARCHAR(255)," + //
                "AdminsWithAdminRights CLOB(10K)," + //
                "InstructorsWithAdminRights CLOB(10K)," + //
                "InstructorsWithViewingRights CLOB(10K)," + //
                "StudentsWithViewingRights CLOB(10K)" +     //
                ")";
		
		try (Statement stmt = connection.createStatement())
		{
			stmt.execute(createArticleTableSQL);
		}
		catch (SQLException e)
		{
			System.err.println(
					"TODO: Error message cuz someone copy pasted this and didnt change it"
							+ e.getMessage());
			throw e;
		}
	}

	// Method to create the InviteCodes table
	private void createInviteCodesTable() throws SQLException
	{
		String createTableSQL = "CREATE TABLE IF NOT EXISTS " + inviteCodesTableName + " (" + //
				"code VARCHAR(255) PRIMARY KEY, " + //
				"roleId INTEGER NOT NULL, " + //
				"created_at DATETIME NOT NULL" + //
				")";

		try (Statement stmt = connection.createStatement())
		{
			stmt.execute(createTableSQL);
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO CREATE INVITE CODES TABLE! Happened in DatabaseHelper.createInviteCodesTable\n"
							+ e.getMessage());
			throw e;
		}
	}

	public void clearArticleTable() {
		String createTableSQL = "DROP TABLE "+articlesTableName; //

		// Attempt to create table
		try (Statement stmt = connection.createStatement())
		{
			// Execute create table SQL
			stmt.execute(createTableSQL);
		}
		catch (SQLException e)
		{
			// Failed to create table
			System.err.println("Failed to remove table\n"
							+ e.getMessage());
		}
	}
	
	// Create Seed table
	private void createSeedTable() throws SQLException
	{
		String createTableSQL = "CREATE TABLE IF NOT EXISTS " + inviteCodeSeedTableName + " (" + //
				"id INTEGER PRIMARY KEY, " + // ID really is only used to reference the row
				"currentSeed BIGINT NOT NULL)"; //

		// Attempt to create table
		try (Statement stmt = connection.createStatement())
		{
			// Execute create table SQL
			stmt.execute(createTableSQL);

			// Check if the table was created
			String countSQL = "SELECT COUNT(*) AS count FROM " + inviteCodeSeedTableName;
			ResultSet rs = stmt.executeQuery(countSQL);
			if (rs.next() && rs.getInt("count") == 0)
			{
				// It was empty, Insert initial seed value
				String insertSeedSQL = "INSERT INTO " + inviteCodeSeedTableName + " (id, currentSeed) VALUES (1, 1)";
				stmt.execute(insertSeedSQL);
			}
		}
		catch (SQLException e)
		{
			// Failed to create table
			System.err.println("ERROR! FAILED TO CREATE SEED TABLE! Happened in DatabaseHelper.createSeedTable\n"
							+ e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Clears the groups table
	 */
	public void clearTable()
	{
		String query = "DROP TABLE IF EXISTS " + groupsTableName;

		try (Statement stmt = connection.createStatement())
		{
			stmt.execute(query);
		}
		catch (SQLException e)
		{
			System.err
					.println("TODO: Error message cuz someone copy pasted this and didnt change it\n" + e.getMessage());
		}
	}
	
	/**
	 * Retrieves the current group 
	 * @return
	 */
	public String getCurrentGroup() {
		return rightsManager.getCurrentGroup();
	}
	
	/**
	 * Changes the current group
	 * @param group
	 */
	public void setCurrentGroup(String group) {
		rightsManager.setCurrentGroup(group);
	}
	

	/**
	 * If the user exists in the database, it will return true. if not, returns
	 * false.
	 * 
	 * @param user
	 * @return
	 */
	public boolean validUser(String user)
	{
		String query = "SELECT * FROM " + userbaseTableName + " WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, user);
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		}
		catch (SQLException e)
		{
			System.err.println("TODO: Cannot Evaluate User " + e.getMessage());
			return false;
		}

	}

	/**
	 * The method that returns the list of admins from the database. 
	 */
	public String returnRights(boolean searchAdmin, boolean isInstructor) {
		
		//selects the correct column to search for the group
		String searchColumn = this.correctColumn(searchAdmin, isInstructor);
		
		//the query that returns the appropriate list from the group
		String query = "SELECT * FROM " + groupsTableName + " WHERE groupName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			String sample;
			pstmt.setString(1, rightsManager.getCurrentGroup());
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				Clob clob = rs.getClob(searchColumn);
				
				if(clob == null)
					return "";
				
				String clobData = clob.getSubString(1, (int) clob.length());
				return clobData;
			}}		
			catch (SQLException e)
		{
			System.err.println(
					"ERROR: FAILED TO RETURN LIST OF ADMINS/VIEWERS"
							+ e.getMessage());
		}
		return "";	
	}
	
	
	
	/**
	 * This deletes a specific admin
	 * @param isInstructor
	 * @param admin
	 */
	public boolean deleteRight(String admin, boolean checkAdmin, boolean isInstructor) {
		boolean valid = true;
		try {
			String other = this.returnRights(checkAdmin, !isInstructor);
			String admins = this.returnRights(checkAdmin, isInstructor);
		
			System.out.println(other.split("\\|").length + admins.split("\\|").length);
			System.out.println("*" + other.split("\\|")[0] + "*");
		if(checkAdmin && (other.split("\\|")[0].equals("") || admins.split("\\|")[0].equals("") )) {
			valid = false;
		}
			
		if(admins != null && valid) {
			admins = admins.replace(admin+"|", "");
		
		this.updateRights(this.rightsManager.getCurrentGroup(), admins, checkAdmin, isInstructor);
		
		return true;
		}else 
		return false;}
		catch(Exception e) {
			return false;
		}
	}
	
	/**
	 * this adds a right to a user. If the user added is an instructor, checks the other column 
	 * and if the other column is empty, it adds the instructor there as well
	 */
public boolean addRight(String admin, boolean checkAdmin, boolean isInstructor) {
		System.out.println("Being added");
		try {
		String admins = this.returnRights(checkAdmin, isInstructor);
		if(!admins.contains(admin+"|")) {
			admins += admin+"|";
			
		this.updateRights(this.rightsManager.getCurrentGroup(), admins, checkAdmin, isInstructor);
		
		if(isInstructor) {
			admins = this.returnRights(!checkAdmin, isInstructor);
			
			if(admins == null || admins.replace("|", "").replace(" ", "").length() == 0) {
				String input = admin+"|";
				this.updateRights(this.rightsManager.getCurrentGroup(), input, !checkAdmin, isInstructor);
			}
		}
		
		return true;
		}
		return false;
		}
		catch(Exception e) {
			return false;
		}
	} 
	
	/**
	 * This is hte method that would seleect the correct column to edit
	 * @param searchAdmin
	 * @param isInstructor
	 * @return
	 */
	public String correctColumn(boolean searchAdmin, boolean isInstructor) {
		String column = "AdminsWithAdminRights";
		
		//selects the appropriate column to update
		if(searchAdmin) {
			if(isInstructor)
				return "InstructorsWithAdminRights";
			else
				return column;
		}
		else if(!searchAdmin) {
			if(isInstructor)
				return "InstructorsWithViewingRights";
			else
				return "StudentsWithViewingRights";
		}
		return column;
	}
	
	/**
	 * This is the method that will update the list of admins or viewers in the database. 
	 * One would need to specify the group, list, and if they want to update the 
	 * UsersWithAdminRights or UsersWithViewingRights column. 
	 */
	public void updateRights(String group, String list, boolean searchAdmin, boolean isInstructor) {
		
		String column = this.correctColumn(searchAdmin, isInstructor);
		
		//query for returning the appropriate column
		String query = "UPDATE " + groupsTableName + " SET "+column+" = ? WHERE groupName = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, list);
			pstmt.setString(2, group);
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO UPDATE GROUPS TABLE\n"
							+ e.getMessage());
		}
	}
	
	/**
	 * This method returns the role of the selected user. 
	 * @param user
	 * @return
	 */
	public int returnRole(String user) {
		//query for returning the role of a specified user
		String query = "SELECT role FROM " + userbaseTableName + " WHERE username = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, user);
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
				int result = rs.getInt("role");
				return result;}
			else {
				System.out.println("Failure at Get Role");
				return -1;}
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO GET ROLE!\n"
							+ e.getMessage());
		}
		return -1;
	}
	
	public String groupContents() {
		String result = "";
		
		String query = "SELECT * FROM "+groupsTableName+" WHERE groupName = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, this.rightsManager.getCurrentGroup());
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
				Clob clob = rs.getClob("AdminsWithAdminRights");
				
				if(clob != null)
					result += clob.getSubString(1, (int) clob.length())+"*";
				else
					result += "*";
				
				clob = rs.getClob("InstructorsWithAdminRights");
				
				if(clob != null)
					result += clob.getSubString(1, (int) clob.length())+"*";
				else
					result += "*";
				
				clob = rs.getClob("InstructorsWithViewingRights");
				
				if(clob != null)
					result+=clob.getSubString(1, (int) clob.length())+"*";
				else
					result += "*";
				
				clob = rs.getClob("StudentsWithViewingRights");
				
				if(clob != null)
					result += clob.getSubString(1, (int) clob.length());
			}
			
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO GET ROLE!\n"
							+ e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Returns if the group exists in the database
	 */
	public boolean groupExist(String group) {
		//query for seeing if a group exists
		String query = "SELECT * FROM " + groupsTableName + " WHERE groupName = ?";
				
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, group);
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO GET ROLE!\n"
							+ e.getMessage());
		}
		return false;
	}
	
	public void printGroups() {
		String query = "SELECT * FROM Groups";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				System.out.println(rs.getString("groupName") + " " +rs.getString("AdminsWithAdminRights"));}
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO GET ROLE!\n"
							+ e.getMessage());
		}
	}
	
	/**
	 * THis is the method that checks if the group is a special one
	 * @param group
	 * @return
	 */
	public boolean isSpecial() {
		String query = "SELECT encryptionKey FROM "+groupsTableName+" WHERE groupName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, this.rightsManager.getCurrentGroup());
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
				return rs.getString("encryptionKey") == "";
			}
			else
				return false;
			
		}
		catch (SQLException e)
		{
			System.out.println(
					"ERROR! FAILED TO ADD GROUPS!\n"
							+ e.getMessage());
		}
		return false;
	}
	
	
	/**
	 * This will be the method that adds groups into our database
	 * @param group
	 * @param key
	 * @param creator
	 */
	public void addGroup(String group, String key) {
		
		String column = "InstructorsWithAdminRights";
		
		if(currentRole == Role.Admin)
			column = "AdminsWithAdminRights";
		
		//query for adding a new group into the Groups table
		String query = "INSERT INTO " + groupsTableName + " (groupName, encryptionKey, "+column+") VALUES"
				+ "(?, ?, ?)";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, group);
			pstmt.setString(2, key);
			pstmt.setString(3, currentUsername+"|");
			
			if(currentRole == Role.Instructor) {
				this.addRight(currentUsername, false, true);
				}
			
			pstmt.executeUpdate();
			
			System.out.println("Groups Sucessfulled Added");
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO ADD GROUPS!\n"
							+ e.getMessage());
		}
	}
	
	
	/**
	 * This is a helper method that checks if there exists an instructor in a group. 
	 * @param group
	 * @return
	 */
	public boolean firstInstructor() {
		String query = "SELECT InstructorsWithViewingRights, InstructorsWithAdminRights "
				+ "FROM " + groupsTableName + " WHERE groupName = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			
			pstmt.setString(1, this.rightsManager.getCurrentGroup());
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
			String adminInstructor = rs.getString("InstructorsWithAdminRights");
			String viewerInstructor = rs.getString("InstructorsWithViewingRights");
			
			System.out.println(adminInstructor + " " + viewerInstructor);
			
			return (adminInstructor == null || adminInstructor == "" || adminInstructor.replace("|", "") == "")  && 
					(viewerInstructor == null || viewerInstructor == "" || adminInstructor.replace("|", "") == "");
		}}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO GET ROLE!\n"
							+ e.getMessage());
		}
		return true;
	}
	
	/**
	 * This method will return all of the general groups
	 * @return
	 */
	public ArrayList<String> getAllGeneralGroups() {
		ArrayList<String> result = new ArrayList<String>();
		String query = "SELECT groupName FROM " + groupsTableName + " WHERE encryptionKey = ''";
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				result.add(rs.getString("groupName"));
			}
		
			return result;
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO GET ROLE!\n"
							+ e.getMessage());
		}
		return null;
	}
	
	/**
	 * Selects the groups the instructor is an admin of 
	 * @return
	 */
	public ArrayList<String> getGroupsAsInstructor(){
		ArrayList<String> result = new ArrayList<String>();
		
		String query = "SELECT groupName FROM "+groupsTableName + " WHERE InstructorsWithAdminRights LIKE ?"
				+ "OR InstructorsWithViewingRights LIKE ? OR encryptionkey = ''";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)){
			pstmt.setString(1 , "%"+currentUsername+"%");
			pstmt.setString(2,  "%"+currentUsername+"%");
			
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String sample = rs.getString("groupName");
				result.add(sample);
			}
			return result;
			
		}
		catch(Exception e) {
			System.out.println();
		}
		return result;
	}
	
	/**
	 * Tests if the article is in the group of a user. 
	 * @param id
	 * @return
	 */
	public boolean getArticleGroup(long id) {
		String query = "SELECT groupList FROM " + articlesTableName + " WHERE id = ? ";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setLong(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
			
			String result = rs.getString("groupList");
			ArrayList<String> generalArticles = this.getGroupsAsAdmin();
			
			ArrayList<String> adminGroups = new ArrayList<String>();
			
			String[] groups = result.split("\\|");
					
			if(currentRole == Role.Admin)
				 adminGroups = this.getGroupsAsAdmin();
			else if(currentRole == Role.Instructor)
				adminGroups = this.getGroupsAsInstructor();
			
			for(String i : groups)
				if(adminGroups.contains(i) || generalArticles.contains(i))
					return true;
			
		}}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO GET ROLE!\n"
							+ e.getMessage());
		}
		
		return false;
	}
	
	
	/**
	 * returns all the groups where the user is an admin or a general group
	 */
	
	public ArrayList<String> getGroupsAsAdmin(){
		
		String query = "SELECT groupName, AdminsWithAdminRights FROM " + groupsTableName + " WHERE AdminsWithAdminRights LIKE ?"
				+ "OR encryptionKey = '' ";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, "%"+currentUsername+"%");
			ResultSet rs = pstmt.executeQuery();
			
			ArrayList<String> result = new ArrayList<String>();
			
			
			while(rs.next()) {
				String sample = rs.getString("groupName");
				
				if(!result.contains(sample))
					result.add(rs.getString("groupName"));		
			}
				return result;
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED RETRIEVE GROUPS THE USER IS AN ADMIN IN!\n"
							+ e.getMessage());
		}
		
		return null;
	}
	
	/**
	 * THis method will return all the groups where the user is the admin of that group
	 * @param username
	 * @return
	 */
	public ArrayList<String> getGroupsWithAdmin(String username) {
		ArrayList<String> result = new ArrayList<String>();

		//query for seeing which groups the user is an admin for
		String query = "SELECT groupName FROM " + groupsTableName + " WHERE AdminsWithAdminRights LIKE ?"
				+ " OR InstructorsWithAdminRights LIKE ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, "%"+username+"%");
			pstmt.setString(2, "%"+username+"%");
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				result.add(rs.getString("groupName"));
			}
			
			return result;
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO GET ROLE!\n"
							+ e.getMessage());
		}
		
		return null;
	}

	

	// Check if the database is empty
	public boolean isDatabaseEmpty()
	{
		String query = "SELECT COUNT(*) AS count FROM " + userbaseTableName;

		try (ResultSet resultSet = statement.executeQuery(query))
		{
			// Return true if the count is zero, false otherwise
			if (resultSet.next())
			{
				return resultSet.getInt("count") == 0;
			}
		}
		catch (SQLException e)
		{
			// Log the exception
			System.err.println("SQL Exception occurred: " + e.getMessage());
		}

		// If there's an exception or the result is zero, return true (indicating
		// empty)
		return true;
	}

	// This is the method that would register the users into the system
	public void register(String username, String password, int role)
	{
		String insertUser = "INSERT INTO " + userbaseTableName + " (username, password, role) VALUES (?, ?, ?)";
		try
		{
			PreparedStatement pstmt = connection.prepareStatement(insertUser);
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			pstmt.setInt(3, role);
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR! FAILED TO REGISTER USER TO DATABASE! Happened in DatabaseHelper.register\n"
							+ e.getMessage());
		}
	}

	// Method to check if user has finished setup
	public boolean checkIfAccountIsFinished(String username)
	{
		String query = "SELECT firstName FROM " + userbaseTableName + " WHERE username = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next())
			{
				String firstName = rs.getString("firstName");
				if (firstName != null && !firstName.trim().isEmpty())
				{
					return true; // Setup is complete
				}
			}
		}
		catch (SQLException e)
		{
			System.err.println(
					"ERROR! FAILED TO CHECK IF ACCOUNT IS FINISHED! Happened in DatabaseHelper.checkIfAccountIsFinished\n"
							+ e.getMessage());
		}

		return false; // Setup is not complete
	}

	// This is the method that will finish setting up the account
	public void finishAccount(String username, String email, String firstName, String middleName, String lastName,
			String preferredName)
	{
		String query = "UPDATE " + userbaseTableName + " SET email = ?, firstName = ?, "
				+ "middleName = ?, lastName = ?, preferredName = ? " + "WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, email);
			pstmt.setString(2, firstName);
			pstmt.setString(3, middleName);
			pstmt.setString(4, lastName);
			pstmt.setString(5, preferredName);
			pstmt.setString(6, username);
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR! FAILED TO FINISH SETTING UP USER ACCOUNT TO DATABASE! "
					+ "Happend in DatabaseHelper.finishAccount\n" + e.getMessage());
		}
	}
	
	public void setCurrentUser(String user) {
		this.currentUsername = user;
	}

	// Method will attempt to log in the user, uses an enum to return different
	// statuses
	public StatusCode login(String username, String password)
	{
		String query = "SELECT * FROM " + userbaseTableName + " WHERE username = ? AND password = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, username);
			pstmt.setString(2, password);

			ResultSet rs = pstmt.executeQuery();

			// Check if the user was found
			if (!rs.next()) return StatusCode.UserNotFound; // No user found

			// Get OTP Values
			boolean isOneTimePassword = rs.getBoolean("oneTimePassword");
			LocalDate expireDate = null;

			// Check if the user is not using a OTP
			if (!isOneTimePassword)
			{
				// Not a one time password, regular login
				currentUsername = username.trim();
				return StatusCode.RegularLogin;
			}

			// Check if oneTimePassExpireDate is not NULL
			if (rs.getDate("oneTimePassExpireDate") != null)
			{
				expireDate = rs.getDate("oneTimePassExpireDate").toLocalDate();
			}

			// Check if the expireDate is present
			if (expireDate == null)
			{
				// This shouldnt happen, but ill make a check just in case
				System.err.println(
						"ERROR! One-time password is valid but has no expiration date. This shouldnt be possible");
				return StatusCode.OneTimePasswordMissingExpiryDate;
			}

			// Get local date for comparison
			LocalDate currentDate = LocalDate.now();

			// Check if the one-time password is expired
			if (currentDate.isAfter(expireDate))
			{
				// Password expired, handle it (return false or show an error message)
				if (doDebugLog) System.out.println("One-time password has expired. Please contact an admin");
				return StatusCode.OneTimePasswordExpired;
			}
			else
			{
				// OTP was still valid, redirect user to the reset password page
				currentUsername = username.trim();
				return StatusCode.ValidOneTimePassword;
			}

		}
		catch (SQLException e)
		{
			// Exception Ocurred
			System.err.println(
					"ERROR! FAILED TO LOGIN USER TO DATABASE! Happened in DatabaseHelper.login\n" + e.getMessage());
			return StatusCode.DatabaseException;
		}
	}

	// Method to log out a user
	public void LogoutUser()
	{
		currentUsername = "";
		searchHistory.clear();
		currentRole = null;
	}
	
	// Method to add a search entry into the history of searches of the user
	public void addSearchEntry(SearchEntry entry)
	{
		// Only add it if not present already
		if (!searchHistory.contains(entry)) searchHistory.add(entry);
	}
	
	// Method to get the search list
	public List<SearchEntry> getSearchHistory()
	{
		return searchHistory;
	}

	// Getter for current username
	public String getCurrentUsername()
	{
		return currentUsername;
	}

	// Method to close the connection to the database
	public void closeConnection()
	{
		boolean didStatementClose = false;
		boolean didConnectionClose = false;
		// Try to close statement
		try
		{
			if (statement != null)
			{
				statement.close();
				didStatementClose = true;
			}
		}
		catch (SQLException se2)
		{
			System.err.println("ERROR! FAILED TO CLOSE STATEMENT ON DATABASE!" + se2.getMessage());
		}
		// Try to close connection
		try
		{
			if (connection != null)
			{
				connection.close();
				didConnectionClose = true;
			}
		}
		catch (SQLException se)
		{
			System.err.println("ERROR! FAILED TO CLOSE CONNECTION ON DATABASE!" + se.getMessage());
		}
		// Log to console that database was successfully closed
		if (didStatementClose && didConnectionClose)
		{
			ColorLogger.loglnColor("Database Connection and Statement was closed successfully!",
					ColorLogger.TextColor.GREEN);
		}
	}
	
	// Nested class to help compartmentalize help system methods
	public class HelpMessages
	{
		// Method to insert MessageEntry into the database
		public void insertMessage(MessageEntry entry)
		{
			String insertSQL = "INSERT INTO " + helpMessagesTableName + " (authorUsername, problemMessage, searchHistory) VALUES (?, ?, ?)";

			try (PreparedStatement pstmt = connection.prepareStatement(insertSQL))
			{
				// Set the values for the insert query
				pstmt.setString(1, entry.authorUsername);
				pstmt.setString(2, entry.problemMessage);
				pstmt.setBytes(3, serializeSearchHistory(entry.searchHistory));

				// Execute the insert
				pstmt.executeUpdate();
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Method to delete a help message entry by ID
		public void deleteHelpMessageByID(long ID)
		{
			// query that deletes specified help messages based on ID
			String deleteMsg = "DELETE FROM " + helpMessagesTableName + " WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(deleteMsg))
			{
				pstmt.setLong(1, ID);
				pstmt.executeUpdate();
			}
			catch (SQLException e)
			{
				// Failed
				System.err.println("ERROR! COULDNT REMOVE HELP MESSAGE WITH ID: " + ID + "!\n" + e.getMessage());
			}
		}
		
		// Method to get a list of all message entries
		public List<MessageEntry> getHelpMessagesList()
		{
			List<MessageEntry> messages = new ArrayList<>();
			String query = "SELECT id, authorUsername, problemMessage, searchHistory FROM " + helpMessagesTableName;

			try (PreparedStatement stmt = connection.prepareStatement(query); ResultSet rs = stmt.executeQuery())
			{
				// Collect the list
				while (rs.next())
				{
					int id = rs.getInt("id");
					String authorUsername = rs.getString("authorUsername");
					String problemMessage = rs.getString("problemMessage");
					byte[] searchHistoryBytes = rs.getBytes("searchHistory");

					// Deserialize the searchHistory and messageClass from bytes
					List<SearchEntry> searchHistory = deserializeSearchHistory(searchHistoryBytes);

					// Create a new MessageEntry object and add it to the list
					MessageEntry messageEntry = new MessageEntry(authorUsername, problemMessage, searchHistory);
					messages.add(messageEntry);
				}
			}
			catch (SQLException e)
			{
				System.err.println("Error fetching help messages: " + e.getMessage());
			}
			catch (ClassNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Finished, return list
			return messages;
		}
		
		// Method to get a help message entry by ID
		public MessageEntry getHelpMessageByID(long ID)
		{
			MessageEntry messageEntry = null;
	        String query = "SELECT authorUsername, problemMessage, searchHistory FROM " + helpMessagesTableName + " WHERE id = ?";

			// Establish the connection and query the database
			try (PreparedStatement statement = connection.prepareStatement(query))
			{

				// Set the ID parameter in the query
				statement.setLong(1, ID);

				// Execute the query
				try (ResultSet resultSet = statement.executeQuery())
				{
					if (resultSet.next())
					{
						// We found it, retrieve the data from the result set
						String authorUsername = resultSet.getString("authorUsername");
						String problemMessage = resultSet.getString("problemMessage");
						byte[] searchHistoryBytes = resultSet.getBytes("searchHistory");

						// Convert searchHistoryBytes to a List<SearchEntry> if necessary
						List<SearchEntry> searchHistory = deserializeSearchHistory(searchHistoryBytes);

						// Create the MessageEntry object
						messageEntry = new MessageEntry(ID, authorUsername, problemMessage, searchHistory);
					}
				}
				catch (ClassNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}

			// Return the MessageEntry object (or null if not found)
			return messageEntry;
		}
		
		// Method to serialize a List<SearchEntry> to bytes for storing in the database
		private static byte[] serializeSearchHistory(List<SearchEntry> searchHistory) throws IOException
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(searchHistory);
			return byteArrayOutputStream.toByteArray();
		}
		
		// Method to deserialize the List<SearchEntry> from bytes
		@SuppressWarnings("unchecked")
		private static List<SearchEntry> deserializeSearchHistory(byte[] bytes) throws IOException, ClassNotFoundException 
		{
		    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		    return (List<SearchEntry>) objectInputStream.readObject();
		}
	}

	// Nested class to help compartmentalize article methods
	public class Articles
	{
		
		public void saveArticles(List<Article> list) {
			for(Article i: list)
				insertArticle(i);
		}
		
		/**
		 * This is the method that will store the article.
		 * 
		 * @param newArticle
		 */
		public void insertArticle(Article article)
		{
			String insertArticleSQL = "INSERT INTO " + articlesTableName + " (title, abstractText, body, "
					+ "diffLevel, groupName, authorList, keywords, references, groupIdentifiers, allowedRoles, groupList) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			try (PreparedStatement pstmt = connection.prepareStatement(insertArticleSQL))
			{
				pstmt.setString(1, article.getTitle());

				pstmt.setString(2, article.getAbstractText());

				pstmt.setString(3, article.getBody());

				pstmt.setInt(4, article.getDiffLevel().getValue());
				
				pstmt.setInt(5, article.getGroup().getValue());

				String authorsString = listToString(article.getAuthorList(), '|');
				pstmt.setString(6, authorsString);

				String keywordsString = listToString(article.getKeywords(), '|');
				pstmt.setString(7, keywordsString);

				pstmt.setString(8, article.getReferences());

				String groupIDs = listToString(ArticleTags.GroupIdentifiers.toStringList(article.getGroupIdentifiers()),
						'|');
				pstmt.setString(9, groupIDs);

				pstmt.setInt(10, article.getAllowedRoles());
				
				pstmt.setString(11, article.getListOfGroups());

				pstmt.executeUpdate(); // Execute the insert statement

				System.out.println("Article inserted successfully!");
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO INSERT ARTICLE! Happened in DatabaseHelper.insertArticle\n" + e.getMessage());
			}
		}
		
		// Method to update a target article
		public void updateArticle(Article article)
		{
			String updateArticleSQL = "UPDATE " + articlesTableName + " SET " //
					+ "title = ?, " //
					+ "abstractText = ?, " //
					+ "body = ?, " //
					+ "diffLevel = ?, " //
					+ "groupName = ?, "	//
					+ "authorList = ?, " //
					+ "keywords = ?, " //
					+ "references = ?, " //
					+ "groupIdentifiers = ?, " //
					+ "allowedRoles = ? " //
					+ "WHERE id = ?"; //

			try (PreparedStatement pstmt = connection.prepareStatement(updateArticleSQL))
			{
				pstmt.setString(1, article.getTitle());
				pstmt.setString(2, article.getAbstractText());
				pstmt.setString(3, article.getBody());
				pstmt.setInt(4, article.getDiffLevel().getValue());
				pstmt.setInt(5, article.getGroup().getValue());

				String authorsString = listToString(article.getAuthorList(), '|');
				pstmt.setString(6, authorsString);

				String keywordsString = listToString(article.getKeywords(), '|');
				pstmt.setString(7, keywordsString);

				pstmt.setString(8, article.getReferences());

				String groupIDs = listToString(ArticleTags.GroupIdentifiers.toStringList(article.getGroupIdentifiers()),
						'|');
				pstmt.setString(9, groupIDs);

				pstmt.setInt(10, article.getAllowedRoles());

				// Set the id of the article to be updated
				pstmt.setLong(11, article.getID());

				pstmt.executeUpdate(); // Execute the update statement

				System.out.println("Article updated successfully!");
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO UPDATE ARTICLE! Happened in DatabaseHelper.updateArticle\n" + e.getMessage());
			}
		}

		// Helper method to turn an array of strings into one concated list with delimiters
		private String listToString(List<String> input, char delimiter)
		{
			// Return if empty
			if (input.isEmpty()) return "";
			
			// Build the string
			String output = input.get(0);
			// Iterate through the next
			for (int i = 1; i < input.size(); i++)
			{
				output += delimiter + input.get(i);
			}
			// Return output
			return output;
		}

		/**
		 * This is the method that will remove the article that is indicated by the id.
		 * 
		 * @param id
		 */
		public void deleteArticle(long id)
		{
			// query that deletes specified articles based on ID
			String deleteArt = "DELETE FROM " + articlesTableName + " WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(deleteArt))
			{// activates the query
				pstmt.setLong(1, id);
				pstmt.executeUpdate();
			}
			catch (SQLException e)
			{
				// Failed
				System.err.println("ERROR! COULDNT REMOVE ARTICLE ID " + id + "!\n" + e.getMessage());
			}
		}
		
		// Method to check if an article with a specified ID exists
		public boolean isIDValid(long articleID)
		{
			Article article = null; // Initialize to null
			String query = "SELECT title FROM " + articlesTableName + " WHERE id = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setLong(1, articleID); // Set the article ID parameter
				ResultSet rs = pstmt.executeQuery();
				// Check if there's a result
				if (rs.next())
				{ 
					// There was
					return true;
				}

			}
			catch (SQLException e)
			{
			}
			// there wasnt
			return false;
		}
		
		// Method to fetch an article by ID
		public Article getArticleFromID(long articleId)
		{
			Article article = null; // Initialize to null
			String query = "SELECT title, abstractText, body, diffLevel, groupName, authorList, keywords, references, groupIdentifiers, allowedRoles FROM " + articlesTableName + " WHERE id = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setLong(1, articleId); // Set the article ID parameter
				ResultSet rs = pstmt.executeQuery();
				// Check if there's a result
				if (rs.next())
				{ 
					// There was
					String title = rs.getString("title");
					String abstractText = rs.getString("abstractText");
					String body = rs.getString("body");
					ArticleTags.DifficultyLevel diffLevel = ArticleTags.DifficultyLevel
							.fromValue(rs.getInt("diffLevel"));
					ArticleTags.Group group = ArticleTags.Group
							.fromValue(rs.getInt("groupName"));
					int allowedRoles = rs.getInt("allowedRoles");

					// Retrieve lists from texts
					List<String> authorList = Arrays.asList(rs.getString("authorList").split("\\|"));
					List<String> keywords = Arrays.asList(rs.getString("keywords").split("\\|"));
					String references = rs.getString("references");

					// Split gid string
					List<String> gidList = Arrays.asList(rs.getString("groupIdentifiers").split("\\|"));
					List<ArticleTags.GroupIdentifiers> groupIdentifiers = ArticleTags.GroupIdentifiers
							.fromStringList(gidList);

					// Create an Article object
					article = new Article(articleId, allowedRoles, title, diffLevel, group, authorList, abstractText, keywords, body,
							references, groupIdentifiers);
				}

			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! Failed to retrieve article with ID " + articleId + ": " + e.getMessage());
			}

			return article; // Return the article or null if not found
		}

		// Method to fetch all articles
		public List<Article> getAllArticles()
		{
			List<Article> articles = new ArrayList<Article>();
			String query = "SELECT * FROM " + articlesTableName;
			
			try (Statement stmt = connection.createStatement())
			{
				ResultSet rs = stmt.executeQuery(query);
				
				while (rs.next())
				{
					{
					long id = rs.getLong("id");
					String title = rs.getString("title");
					String abstractText = rs.getString("abstractText");
					String body = rs.getString("body");
					ArticleTags.DifficultyLevel diffLevel = ArticleTags.DifficultyLevel.fromValue(rs.getInt("diffLevel"));
					ArticleTags.Group group = ArticleTags.Group.fromValue(rs.getInt("groupName"));
					int allowedRoles = rs.getInt("allowedRoles");
					
					// Retrieve lists from texts
					List<String> authorList = Arrays.asList(rs.getString("authorList").split("\\|"));
					List<String> keywords = Arrays.asList(rs.getString("keywords").split("\\|"));
					String references = rs.getString("references");
					
					// Split gid string
					List<String> gidList = Arrays.asList(rs.getString("groupIdentifiers").split("\\|"));
					List<ArticleTags.GroupIdentifiers> groupIdentifiers = ArticleTags.GroupIdentifiers.fromStringList(gidList);
					
					// Create an Article object and add it to the list
					Article article = new Article(id, allowedRoles, title, diffLevel, group, authorList, abstractText, keywords, body,
							references, groupIdentifiers);

					// Add it to list
					articles.add(article);
				}}
			}
			catch (SQLException e)
			{
				System.err.println("ERROR! Failed to retrieve articles: " + e.getMessage());
			}
			return articles; // Return the list of articles
		}

		// Method to return a list of articles that matches the filters, returns null if none found
		public List<Article> getArticlesFiltered(Long targetID, List<ArticleTags.GroupIdentifiers> gID, int allowedRoles, List<String> keywords, ArticleTags.DifficultyLevel diffLevel)
		{
			// First, get a list of all articles
			List<Article> iteratingArticles = getAllArticles();
			List<Article> removeTargets = new ArrayList<Article>();
			
			// Check if empty
			if (iteratingArticles.isEmpty()) return null; // was empty, returns null
			
			// First check all articles if they have the matching role permission if any specified
			if (allowedRoles > 1)
			{
				// Wasnt empty, apply filters
				List<Role> inputRoles = Role.getRolesFromValue(allowedRoles);
				for (Article current : iteratingArticles)
				{
					// Check if subset
					List<Role> currentRoles = Role.getRolesFromValue(current.getAllowedRoles());
					if (!isSubset(inputRoles, currentRoles))
					{
						// Wasnt a subset, mark for removal
						removeTargets.add(current);
					}
				}
				// Remove all targets found
				for (Article current : removeTargets)
				{
					iteratingArticles.remove(current);
				}
				removeTargets.clear();
			}
			
			// Check if the iterating array is empty, return early if so
			if (iteratingArticles.isEmpty()) return null;
			
			// Now check if we are looking for a specific ID
			if (targetID != null)
			{
				// Yes we are, search the list
				for (Article current : iteratingArticles)
				{
					// Check for matches
					if (current.getID() != targetID)
					{
						// not the same
						removeTargets.add(current);
					}
				}
				// Remove all non matching targets found
				for (Article current : removeTargets)
				{
					iteratingArticles.remove(current);
				}
				removeTargets.clear();
			}
			
			// Check if the iterating array is empty, return early if so
			if (iteratingArticles.isEmpty()) return null;
			
			// Now check for the difficulty level
			if (diffLevel != ArticleTags.DifficultyLevel.Any)
			{
				// Was specified, search
				for (Article current : iteratingArticles)
				{
					if (current.getDiffLevel() != diffLevel)
					{
						// not the same
						removeTargets.add(current);
					}
				}
				// Remove all non matching targets found
				for (Article current : removeTargets)
				{
					iteratingArticles.remove(current);
				}
				removeTargets.clear();
			}
			
			// Check if the iterating array is empty, return early if so
			if (iteratingArticles.isEmpty()) return null;
			
			// Now check the group identifiers
			if (!gID.isEmpty())
			{
				// Some group IDs were specified
				for (Article current : iteratingArticles)
				{
					// Check if subset
					if (!isSubset(gID, current.getGroupIdentifiers()))
					{
						// Wasnt a subset, mark for removal
						removeTargets.add(current);
					}
				}
				// Remove all non matching targets found
				for (Article current : removeTargets)
				{
					iteratingArticles.remove(current);
				}
				removeTargets.clear();
			}
			
			// Check if the iterating array is empty, return early if so
			if (iteratingArticles.isEmpty()) return null;
			
			// Now for the keywords
			if (!keywords.isEmpty())
			{
				// Some keywords were specified
				// Since we have to check multiple places for the words/sentences, we need to perform a bunch of checks
				List<Article> matches = new ArrayList<Article>();
				boolean isArticleToBeAdded = false;
				
				// Search in the title
				for (Article currentArticle : iteratingArticles)
				{
					// HACK IAN: Lol gotta love that O(n^2)~ish time complexity
					isArticleToBeAdded = true;
					for (String currentKeyword : keywords)
					{
						// Search in the Title
						if (currentArticle.getTitle().contains(currentKeyword)) continue;
						
						// Search in the keywords
						if (currentArticle.getKeywords().contains(currentKeyword)) continue;
						
						// Search in the authors
						if (currentArticle.getAuthorList().contains(currentKeyword)) continue;
						
						// Search in the abstract
						if (currentArticle.getAbstractText().contains(currentKeyword)) continue;
						
						// Search in the body
						if (currentArticle.getBody().contains(currentKeyword)) continue;
						
						// Keyword not found within this article, do not add it to matches
						isArticleToBeAdded = false;
						break;
					}
					// If we made it here and the isArticleToBeAdded is still true, it means the article contained all the keywords
					// somehwere within it
					if (isArticleToBeAdded) matches.add(currentArticle);
				}
				// We finished searching within the article for the keywords
				iteratingArticles = matches;
			}
			
			// Finished filtered search, return
			return iteratingArticles;
		}
		
		// Helper method for filtering
		public static <T> boolean isSubset(List<T> subset, List<T> mainList) {
	        return mainList.containsAll(subset);
	    }
	}

	// Nested class to help compartmentalize invite code methods
	public class InviteCodes
	{
		// =================================== PUBLIC METHODS ===================================
		
		// Method to retrieve a list of the currently active Invite Codes
		public ObservableList<InviteCode> getInviteCodesList()
		{
			ObservableList<InviteCode> inviteCodes = FXCollections.observableArrayList();
			String query = "SELECT code, roleId, created_at FROM " + inviteCodesTableName;

			try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery())
			{

				while (rs.next())
				{
					String code = rs.getString("code");
					int roleId = rs.getInt("roleId");
					LocalDateTime creationDate = rs.getTimestamp("created_at").toLocalDateTime();

					// Parse the Role Id into a Role List
					List<Role> roleVals = Role.getRolesFromValue(roleId);
					// Check if null
					if (roleVals == null)
					{
						// If an error happened, break here
						System.err.println("ERROR! RoleID[" + roleId + "] couldnt be interpreted as roles!");
						break;
					}

					// Create a string with the roles
					String roleList = "";
					for (int i = 0; i < roleVals.size() - 1; i++)
					{
						roleList += roleVals.get(i).name() + " | ";
					}
					// Grab the last one
					roleList += roleVals.get(roleVals.size() - 1).name();

					// Add to list
					inviteCodes.add(new InviteCode(code, roleList, creationDate));
				}
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO RETRIEVE INVITE CODES! Happened in DatabaseHelper.getInviteCodes\n"
								+ e.getMessage());
			}

			return inviteCodes;
		}

		// Check if the invite code is valid
		public boolean checkInviteCode(String code)
		{
			// Query to check if the invite code exists in the database
			String query = "SELECT roleId FROM " + inviteCodesTableName + " WHERE code = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setString(1, code);
				ResultSet rs = pstmt.executeQuery();

				// Check if a row was returned
				if (rs.next())
				{
					return true; // Code is valid
				}
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO CHECK INVITE CODE! Happened in DatabaseHelper.checkInviteCode\n"
								+ e.getMessage());
			}

			return false; // Code is not valid
		}

		// Remove invite code from table, returns false if it fails
		public boolean removeInviteCode(String code)
		{
			String deleteSQL = "DELETE FROM " + inviteCodesTableName + " WHERE code = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL))
			{
				pstmt.setString(1, code);
				int rowsAffected = pstmt.executeUpdate();

				if (rowsAffected > 0)
				{
					if (doDebugLog) System.out.println("Invite code removed successfully.");
					return true;
				}
				else
				{
					if (doDebugLog) System.out.println("No invite code found with the specified code.");
				}
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO REMOVE INVITE CODE! Happened in DatabaseHelper.removeInviteCode\n"
								+ e.getMessage());
			}
			// Failed to remove
			return false;
		}

		// Method that retrieves the role id from an invite code, returns -1 if failed
		public int getRoleIDFromInvite(String code)
		{
			String query = "SELECT roleId FROM " + inviteCodesTableName + " WHERE code = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setString(1, code);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
				{
					// Return the roleId if found
					return rs.getInt("roleId");
				}
				else
				{
					if (doDebugLog) System.out.println("No matching invite code found.");
				}
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO GET ROLE ID FROM INVITE CODE! Happened in DatabaseHelper.getRoleIdFromInviteCode\n"
								+ e.getMessage());
			}
			// Failed to obtain role id
			return -1;
		}

		// Method to generate a 32 digit invite code, return it and also add it to the
		// database, returns null if failed
		public String generateInviteCode(int roleID)
		{
			long currentSeed = getCurrentSeed(); // Get the current seed
			// If failed to fetch seed, dont execute anything else
			if (currentSeed < 0) return null;

			// Increment the seed for the next time a code is generated
			// If it failed to increment, dont execute anything else
			if (!incrementSeed(currentSeed)) return null;

			// Create a random class to generate a code, using a seed to ensure it doesnt
			// repeat
			Random random = new Random(currentSeed);
			StringBuilder inviteCode = new StringBuilder();

			// Generate a random code
			for (int i = 0; i < inviteCodeLength; i++)
			{
				int nextDigit = random.nextInt(10); // only digits (0-9)
				inviteCode.append(nextDigit);
			}

			// Generated a code, cast it to string
			String strInviteCode = inviteCode.toString();

			// Now insert code into database, Dont return invite code if failed
			if (!insertInviteCode(strInviteCode, roleID)) return null;

			// If we didnt fail, it means we generated a code and stored it into database
			// successfully
			return strInviteCode;
		}
		
		// =================================== PRIVATE METHODS ===================================

		// Method to insert invite code into database, returns false if failed
		private boolean insertInviteCode(String code, int roleID)
		{
			String insertSQL = "INSERT INTO " + inviteCodesTableName + " (code, roleId, created_at) VALUES (?, ?, ?)";
			try (PreparedStatement pstmt = connection.prepareStatement(insertSQL))
			{
				pstmt.setString(1, code);
				pstmt.setInt(2, roleID);
				pstmt.setObject(3, LocalDateTime.now()); // Gets current date and time
				pstmt.executeUpdate();
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO INSERT INVITE CODE! Happened in DatabaseHelper.insertInviteCode\n"
								+ e.getMessage());
				// Return false if failed
				return false;
			}
			// Else, succeeded
			return true;
		}

		// Method to fetch the current seed from the Seed table, returns negative if
		// failed
		private long getCurrentSeed()
		{
			String query = "SELECT currentSeed FROM " + inviteCodeSeedTableName + " WHERE id = 1";
			try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query))
			{
				if (rs.next())
				{
					return rs.getLong("currentSeed");
				}
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO FETCH CURRENT SEED! Happened in DatabaseHelper.getCurrentSeed\n"
								+ e.getMessage());
			}
			return -1; // Return -1 if failed
		}

		// Method to increment the seed value in the Seed table, returns false if failed
		private boolean incrementSeed(long currentSeed)
		{
			String updateSeedSQL = "UPDATE " + inviteCodeSeedTableName + " SET currentSeed = ? WHERE id = 1";
			try (PreparedStatement pstmt = connection.prepareStatement(updateSeedSQL))
			{
				pstmt.setLong(1, currentSeed + 1); // Increment seed
				pstmt.executeUpdate();
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO INCREMENT SEED! Happened in DatabaseHelper.incrementSeed\n" + e.getMessage());
				// Failed
				return false;
			}
			// Succeded in updating
			return true;
		}

	}

	// Nested class to help compartmentalize user methods
	public class Users
	{ 
		// The sets display the users that are present in the database
		public void displayUsersByUser() throws SQLException
		{
			String sql = "SELECT * FROM " + userbaseTableName;
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next())
			{
				// Retrieve by column name
				int id = rs.getInt("id");
				String username = rs.getString("username");
				String email = rs.getString("email");
				String password = rs.getString("password");
				int role = rs.getInt("role");

				// Display values
				System.out.print("ID: " + id);
				System.out.print(", Username: " + username);
				System.out.print(", Email: " + email);
				System.out.print(", Password: " + password);
				System.out.println(", Role: " + role);
			}
		}

		// See all users that are admin on the database
		public void displayUsersByAdmin() throws SQLException
		{
			String sql = "SELECT * FROM " + userbaseTableName;
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next())
			{
				// Retrieve by column name
				int id = rs.getInt("id");
				String username = rs.getString("username");
				String email = rs.getString("email");
				String password = rs.getString("password");
				int role = rs.getInt("role");

				// Display values
				System.out.print("ID: " + id);
				System.out.print(", Username: " + username);
				System.out.print(", Email: " + email);
				System.out.print(", Password: " + password);
				System.out.println(", Role: " + role);
			}
		}

		// Function that will check if the username exists within databse
		public boolean isUsernameInDatabase(String username)
		{
			String query = "SELECT COUNT(*) FROM " + userbaseTableName + " WHERE username = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setString(1, username);
				ResultSet rs = pstmt.executeQuery();

				if (rs.next())
				{
					// If the count is greater than 0, the user exists
					return rs.getInt(1) > 0;
				}
			}
			catch (SQLException e)
			{
				System.err.println("A SQL Error Happened!\n" + e.getMessage());
			}
			return false; // If an error occurs, assume user doesn't exist
		}

		// Method to retrieve a list of users from the database
		public ObservableList<User> getUsers()
		{
			ObservableList<User> userList = FXCollections.observableArrayList();
			String query = "SELECT id, firstName, middleName, lastName, preferredName, username, email, role FROM " + userbaseTableName;

			try (PreparedStatement preparedStatement = connection.prepareStatement(query);
					ResultSet resultSet = preparedStatement.executeQuery())
			{

				while (resultSet.next())
				{
					int id = resultSet.getInt("id");
					String firstName = resultSet.getString("firstName");
					String middleName = resultSet.getString("middleName");
					String lastName = resultSet.getString("lastName");
					String preferredName = resultSet.getString("preferredName");
					String username = resultSet.getString("username");
					String email = resultSet.getString("email");
					int roleID = resultSet.getInt("role");

					// Parse the Role Id into a Role List
					List<Role> roleVals = Role.getRolesFromValue(roleID);
					// Check if null
					if (roleVals == null)
					{
						// If an error happened, break here
						System.err.println("ERROR! RoleID[" + roleID + "] couldnt be interpreted as roles!");
						break;
					}

					// Create a string with the roles
					String roleList = "";
					for (int i = 0; i < roleVals.size() - 1; i++)
					{
						roleList += roleVals.get(i).name() + " | ";
					}
					// Grab the last one
					roleList += roleVals.get(roleVals.size() - 1).name();

					// Create a new User object and add it to the ObservableList
					User user = new User(id, username, firstName, middleName, lastName, preferredName, email, roleList);
					userList.add(user);
				}
			}
			catch (SQLException e)
			{
				System.err.println("SQL ERROR! OCCURED AT: DatabaseHelper.getUsers");
				e.printStackTrace();
			}

			return userList;
		}

		// Method called to update a users roles
		// WARNING: Does not check if the user id is valid or the role ID
		public void updateUserRoles(int userID, int newRoleID)
		{
			String query = "UPDATE " + userbaseTableName + " SET role = ? WHERE id = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setInt(1, newRoleID); // Set new role ID
				pstmt.setInt(2, userID); // Set the username to identify the account

				int rowsAffected = pstmt.executeUpdate();

				if (rowsAffected > 0)
				{
					if (doDebugLog)
						System.out.println("Role successfully changed for user: " + getUsernameFromID(userID));
				}
				else
				{
					System.err.println("No user found with username ID: " + userID);
				}
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO CHANGE USER ROLE! Happened in DatabaseHelper.changeUserRole\n"
								+ e.getMessage());
			}
		}

		// Method to retrieve Username given a User ID, returns null if failed
		public String getUsernameFromID(int ID)
		{
			String query = "SELECT username FROM " + userbaseTableName + " WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setInt(1, ID);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
				{
					// Found
					return rs.getString("username");
				}
			}
			catch (SQLException e)
			{
				System.err.println("ERROR! FAILED TO GET USER ID! Happened in DatabaseHelper.getUsernameFromID\n" + e.getMessage());
			}
			return null; // Return null if no user ID is found
		}

		// Method to remove User by UserID, returns true if successful
		public boolean removeUserByID(int ID)
		{
			String removeUser = "DELETE FROM " + userbaseTableName + " WHERE id = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(removeUser))
			{
				pstmt.setInt(1, ID);
				pstmt.executeUpdate();
			}
			catch (SQLException e)
			{
				// Failed
				System.err.println("ERROR! COULDNT REMOVE USERAME ID " + ID + "!\n" + e.getMessage());
				return false;
			}
			// Succeeded
			return true;
		}

		// Method to remove User by username
		public boolean removeUserByUsername(String username)
		{
			// First check if the username is in the database
			if (!isUsernameInDatabase(username))
			{
				// Invalid username
				return false;
			}

			// Else execute database query
			String removeUser = "DELETE FROM " + userbaseTableName + " WHERE username = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(removeUser))
			{
				pstmt.setString(1, username);
				pstmt.executeUpdate();
			}
			catch (SQLException e)
			{
				// Failed
				System.err.println("ERROR! COULDNT REMOVE USERAME " + username + "!\n" + e.getMessage());
				return false;
			}
			// Succeeded
			return true;
		}

		// Method called to change a users password
		// WARNING: does not check if the string password argument is valid or if the
		// username exists
		public void changeUserPassword(String username, String newPassword)
		{
			String query = "UPDATE " + userbaseTableName + " SET password = ?, oneTimePassword = FALSE, oneTimePassExpireDate = NULL WHERE username = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setString(1, newPassword); // Set new password
				pstmt.setString(2, username); // Set the username to identify the account

				int rowsAffected = pstmt.executeUpdate();

				if (rowsAffected > 0)
				{
					if (doDebugLog) System.out.println("Password successfully changed for user: " + username);
				}
				else
				{
					System.err.println("No user found with username: " + username);
				}
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO CHANGE USER PASSWORD! Happened in DatabaseHelper.changeUserPassword\n"
								+ e.getMessage());
			}
		}

		// Method called to reset the password of a user, returns the one time password
		public String resetUserPasswordAndSetOTP(int userID, LocalDate expireDate)
		{
			// Generate a random 24-digit password
			StringBuilder newPassword = new StringBuilder();
			Random rand = new Random();
			for (int i = 0; i < otpPasswordLength; i++)
			{
				newPassword.append(rand.nextInt(10));
			}
			String oneTimePassword = newPassword.toString();

			// Prepare SQL query to update user's password, set OTP flag, and set expiration
			// date
			String updateSQL = "UPDATE " + userbaseTableName + " SET password = ?, oneTimePassword = TRUE, oneTimePassExpireDate = ? WHERE id = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(updateSQL))
			{
				pstmt.setString(1, oneTimePassword); // Set new password
				pstmt.setDate(2, Date.valueOf(expireDate)); // Set expiration date
				pstmt.setInt(3, userID); // Set user ID

				pstmt.executeUpdate(); // Execute update
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! Failed to reset user password in resetUserPassword\n" + e.getMessage());
				return null; // Return null if update fails
			}

			// Return the generated one-time password
			return oneTimePassword;
		}

		// Method to check if user ID is in database
		public boolean isUserIDInDatabase(int ID)
		{
			String query = "SELECT COUNT(*) FROM " + userbaseTableName + " WHERE id = ?";

			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setInt(1, ID);
				ResultSet rs = pstmt.executeQuery();

				if (rs.next())
				{
					// If the count is greater than 0, the user exists
					return rs.getInt(1) > 0;
				}
				else
				{
					// Doesnt exist
					return false;
				}
			}
			catch (SQLException e)
			{
				System.err.println("A SQL Error Happened!\n" + e.getMessage());
			}
			return false; // If an error occurs, assume user doesn't exist
		}

		// Method to retrieve User ID given a username
		public Integer getUserIDFromUsername(String username)
		{
			String query = "SELECT id FROM " + userbaseTableName + " WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setString(1, username);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
				{
					// Found
					return rs.getInt("id");
				}
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO GET USER ID! Happened in DatabaseHelper.getUserIdFromUsername\n"
								+ e.getMessage());
			}
			return null; // Return null if no user ID is found
		}

		// A method to be used by the program after a user logs in to get their roles
		public Integer getUserRole(String username)
		{
			String query = "SELECT role FROM " + userbaseTableName + " WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query))
			{
				pstmt.setString(1, username);

				try (ResultSet rs = pstmt.executeQuery())
				{
					if (rs.next())
					{
						return rs.getInt("role"); // Return the user's role
					}
				}
			}
			catch (SQLException e)
			{
				System.err.println(
						"ERROR! FAILED TO FETCH USER ROLE! Happened in DatabaseHelper.getUserRole\n" + e.getMessage());
			}
			return null; // Return null if no role found
		}
	}
}
