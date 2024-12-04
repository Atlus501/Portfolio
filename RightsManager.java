package managerClasses;

import java.util.ArrayList;

import DatabaseFiles.DatabaseHelper;

/**
 * This will be the class that manages the admin/viewing rights related things.
 * The idea of this class is that there will be an arraylist that stores
 * all the admins and viewers that are currently viewed.
 * There will be a method that updates the database to reflect the state of the 
 * arrayList. 
 * Of course, the other methods are there to update the arrayList.
 */
public class RightsManager {
	
	//the DatabaseHelper that is going to access the data.
	private DatabaseHelper dataManager = null;
	
	//The ArrayLsit that is going to store the current admins being observed.
	private ArrayList<String> adminsWithAdminRights = new ArrayList<String>();
	private ArrayList<String> instructorsWithAdminRights = new ArrayList<String>();
	
	//The ArrayList that is going to store the users who can view the articles
	//in the group
	private ArrayList<String> instructorsWithViewRights = new ArrayList<String>();
	private ArrayList<String> studentsWithViewRights = new ArrayList<String>();
	
	//The current group being edited by the admin Manager. 
	private String currentGroup = null;
	
	//variables to check roles
	boolean student = false;
	boolean instructor = false;
	boolean admin = false;
	
	//variable to see if current user is a valid admin
	boolean validAdmin = false;
	
	/**
	 * The constructor for the rights manager class.
	 * @param dataStorage
	 */
	public RightsManager(DatabaseHelper dataStorage) {
		//gets the information from the databaseHelper object
		dataManager = dataStorage;
	}
	
	/**
	 * Returns the current list of admins for a group
	 * @param group
	 * @return
	 */
	public ArrayList<String> getAdmins(){
		this.retrieveRights(true, false);
		return adminsWithAdminRights;
	}
	
	/**
	 * Returns the instructors that have admin rights
	 * @return
	 */
	public ArrayList<String> getInstructorsWithAdminRights(){
		this.retrieveRights(true, true);
		return instructorsWithAdminRights;
	}
	
	/**
	 * This method returns the current group.
	 * @return
	 */
	public String getCurrentGroup() {
		return currentGroup;
	}
	
	/**
	 * Returns the current list of viewer of a group
	 * @param group
	 * @return
	 */
	public ArrayList<String> getInstructorViewers(){
		this.retrieveRights(false, true);
		return instructorsWithViewRights;
	}
	
	public ArrayList<String> getStudentViewers(){
		this.retrieveRights(false, false);
		return studentsWithViewRights;
	}
	
	/**
	 * THis is the method that will select the correct column
	 * @param checkAdmin
	 * @param isInstructor
	 * @return
	 */
	public ArrayList<String> selectColumn(boolean checkAdmin, boolean isInstructor){
		if(checkAdmin) {
			if(isInstructor)
				return adminsWithAdminRights;
			else
				return instructorsWithAdminRights;
		}
		else {
			if(isInstructor)
				return instructorsWithViewRights;
			else
				return studentsWithViewRights;
		}
	}
	
	/**
	 * The method for retrieving the list of admins or viewers from the database. 
	 * Since this method will be called first, before any operations with admins,
	 * it will be the one to set the group it would use. 
	 */
	private void retrieveRights(boolean checkAdmins, boolean isInstructor) {
		
		ArrayList<String> editList = this.selectColumn(checkAdmins, isInstructor);

		//clears the list first to not overlap with anything.
		editList.clear();
		
		//need to puree the string first since everything is separated by "|"
		 String[] list = dataManager.returnRights( checkAdmins, isInstructor).split("|");
		
		//The for loop puts the contents of the pureed stirng into the arrayList only
		 //if they are valid users.
		for(String i : list) 
			if(dataManager.validUser(i)){
				editList.add(i);}
		
	}
	
	/**
	 * Checks if there is an instructor added
	 */
	private boolean firstInstructor() {
		return instructorsWithAdminRights.size() == 0 &&
				instructorsWithViewRights.size() == 0;
	}
	
	
	/**
	 * The method for adding admins into a group.
	 * Also returns if the process was successful or not. 
	 * @param group
	 * @param newAdmin
	 */
	public boolean addAdmin(String newAdmin, boolean isInstructor) {
		//checks if the new user is an valid user in the database
		if(dataManager.validUser(newAdmin)) {
		
		ArrayList<String> editList = this.selectColumn(true, isInstructor);
		//checks the role of user
		this.evaluateRole(newAdmin);
		
		//if user is a valid admin and the added user is an instructor or admin, 
		//the person gets added as an admin of that group
		if(validAdmin){
			
			editList.add(newAdmin);
			this.updateDatabase(true, isInstructor);
			
			if(isInstructor && firstInstructor()) {
				editList = this.selectColumn(false, isInstructor);
				editList.add(newAdmin);
				this.updateDatabase(false, isInstructor);
			}
			
			return true;
	}}return false;}
	
	public void setCurrentGroup(String group) {
		this.currentGroup = group;
	}
	
	/**
	 * THis method is for adding viewers into a group.
	 * Also returns if the process is successful for not.
	 * @param newViewer
	 */
	public boolean addViewer(String newViewer, boolean isInstructor) {
		//checks if the viewer is a valid user
		if(dataManager.validUser(newViewer)) {
		
		ArrayList<String> list = this.selectColumn(false, isInstructor);
		//evaluates the user's roles
		this.evaluateRole(newViewer);
		
		//if the user is a valid admin and the viewer is an instructor or student, 
		//they will be added to the group as an admin
		if(validAdmin && (instructor || student)) {
			
			list.add(newViewer);
			this.updateDatabase(false, isInstructor);
			
			if(this.firstInstructor()) {
				list = this.selectColumn(true, isInstructor);
				list.add(newViewer);
				this.updateDatabase(true, isInstructor);
			}
			
			return true;
	}}return false;}
	
	/**
	 * This is the method that deletes admins from the database
	 * @param group
	 * @param admin
	 */
	public boolean deleteAdmin(String admin, boolean isInstructor) {
		
		ArrayList<String> list = this.selectColumn(true, isInstructor);
		//This will only trigger if the admin is in the arrayList
		if(this.checkValidAdmin(admin) && this.anyAdmin()) {
			list.remove(admin);//removes the admin from the database and arrayList
			this.updateDatabase(true, isInstructor);
			return true;
		}
		return false;
	}
	
	/**
	 * Check if there would be at least one admin.
	 */
	private boolean anyAdmin() {
		return adminsWithAdminRights.size() + instructorsWithAdminRights.size() > 1;
	}
	
	/**
	 * The logic system to see if the admin can be removed from 
	 * @param admin
	 * @return
	 */
	private boolean checkValidAdmin(String admin) {
		return validAdmin && adminsWithAdminRights.contains(admin) || 
				instructorsWithAdminRights.contains(admin);
	}
	
	/**
	 * Checks if the user exists in the arrayLists
	 * @param viewer
	 * @return
	 */
	private boolean checkValidViewer(String viewer) {
		return validAdmin && instructorsWithViewRights.contains(viewer) ||
				studentsWithViewRights.contains(viewer);
	}
	
	
	/**
	 * This method would delete viewers from the database
	 * @param viewer
	 * @param b 
	 */
	public boolean deleteViewer(String viewer, boolean editInstructor, boolean isInstructor) {
		//activates if user is a valid admin and 
		if(this.checkValidViewer(viewer)) {
			//evaluates role of user
			this.evaluateRole(viewer);
			
			//if the user was an admin or the target is a student, then the user can be removed
			if(isInstructor || student) {
				this.selectColumn(false, editInstructor).remove(viewer);
				this.updateDatabase(false, editInstructor);
				return true;}
		
	}return false;}
	
	/**
	 * This is the method that will update the database
	 */
	private void updateDatabase(boolean checkAdmin, boolean isInstructor) {
		//prepares the string first before updating the database
		String list = this.prepareString(checkAdmin, isInstructor);
		dataManager.updateRights(currentGroup, list, checkAdmin, isInstructor);
	}
	
	
	/**
	 * This is the method that prepares the string before it is stored
	 * @return
	 */
	private String prepareString(boolean checkAdmin, boolean isInstructor) {
		String users = "";
		
		ArrayList<String> list = this.selectColumn(checkAdmin, isInstructor);
		
		//Adds the "|" to separate the elements
		for(String i : list) {
			users += i;
			users += "|";
		}
		
		return users;
	}
	
	/**
	 * This is the method that checks if a user has admin rights to a group. 
	 * @param group
	 * @param user
	 * @return
	 */
	public boolean checkAdminRights() {
		String admins = dataManager.returnRights( true, true);
		admins += dataManager.returnRights( true, false);
		validAdmin = admins.contains(dataManager.getCurrentUsername());
		return validAdmin;
		}

	/**
	 * This is the method that would check if a user has viewing rights to a group
	 * @param group
	 * @param user
	 * @return
	 */
	public boolean checkViewingRights(String group) {
		//returns the viewing rights from the databaseHelper
		String viewers = dataManager.returnRights( false, true);
		viewers += dataManager.returnRights( false, false);
		//see if the user is within the list of viewers
		return viewers.contains(dataManager.getCurrentUsername());
	}
	/**
	 * Evaluates the role of the user based on their roleId
	 * @param user
	 */
	public void evaluateRole(String user) {
		int roleId = dataManager.returnRole(user);
		
		//evaluates the valid roles of a user based on their role id
		if(roleId % 2 == 0)
			this.admin = true;
		
		if(roleId %3 == 0)
			this.student = true;
		
		if(roleId % 5 ==0)
			this.instructor = true;
	}

}
