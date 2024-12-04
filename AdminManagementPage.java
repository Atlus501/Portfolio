package PageFiles;

import java.util.ArrayList;

import DatabaseFiles.DatabaseHelper;
import DatabaseFiles.Role;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import managerClasses.RightsManager;

/**
 * This is the UI for the page for managing admin status. 
 */
public class AdminManagementPage {

	//external objects required for normal funcitons
	private DatabaseHelper database = null;
	
	//information about the current user
	private String currentUser = null;
	private Role currentRole;
	
	//The textfields that the users will input their 
	//users from
	private TextField addingAdmin = new TextField();
	private TextField addingViewer = new TextField();
	
	//warnings that tell if something went wrong with the process
	private Label adminWarning = new Label();
	private Label viewerWarning = new Label();
	
	private ChoiceBox<String> groupChoice = new ChoiceBox<String>();
	
	private HBox root = new HBox(35);
	private VBox extras = new VBox(4);
	//private VBox adminRemoval = new VBox(2); 
	private VBox adminStorage = new VBox(4);
	private VBox adminsWithAW = new VBox(4);
	private VBox instructorsWithAW = new VBox(4);
	//private VBox viewerRemoval = new VBox(2);
	private VBox viewerStorage = new VBox(4);
	private VBox instructorsWithVW = new VBox(4);
	private VBox studentsWithVW = new VBox(4);
	
	private Label currentGroup = new Label();
	private CheckBox isAdmin = new CheckBox("Add As Admin?");
	
	private TextField newGroup = new TextField();
	private CheckBox privateGroup = new CheckBox("Private?");
	private Label special = new Label();
	
	/**
	 * This is the constructor for this page.
	 * Its parameters include the stage and RightsManager
	 * @param primaryStage
	 * @param data
	 * @param rights
	 */
	public AdminManagementPage(DatabaseHelper database) {
		this.database = database;
		this.setUpPage();
	}

	/**
	 * This sets up the layout of the page. There will be 5 subsections of the main page. 
	 * It also sets up the currentUser of the page as well. 
	 */
	public void setUpPage() {
		root.getChildren().addAll(extras, adminStorage, viewerStorage);
		root.setAlignment(Pos.TOP_CENTER);
		
		//sets the alignment of a couple of columns
		extras.setAlignment(Pos.TOP_CENTER);
		adminStorage.setAlignment(Pos.TOP_CENTER);
		viewerStorage.setAlignment(Pos.TOP_CENTER);
		
		//updates the current user information 
		this.currentUser = database.getCurrentUsername();
		this.currentRole = database.getCurrentRole();
		
		//the private methods that set up the GUI
		this.setUpExtras();
		this.setUpAdminStorage();
		this.setUpViewerStorage();
	}
	
	/**
	 * This methods sets up the widgets in the extras section.
	 * Unless told otherwise, this should don't contain the go back button
	 * that redirects the user to the previous new page. 
	 */
	public void setUpExtras() {
		//adds the button that would make the user go back to the tab group
		
		Label group = new Label("Currently Viewing Group: ");
		group.setFont(new Font(15));
		
		currentGroup.setFont(new Font(15));
		
		//adds the button that would make the user leave the group
		Button leave = new Button("Leave Group's Admins");
		leave.setFont(new Font(15));
		leave.setOnAction(event -> this.leaveGroupAdmin());
		
		groupChoice.prefWidth(45);
		
		ArrayList<String> groups = this.database.getGroupsAsAdmin();
		for(String i : groups)
			groupChoice.getItems().add(i);
		
		Button setGroup = new Button("Choose Group");
		setGroup.setFont(new Font(14));
		setGroup.setOnAction(event -> this.setUpData());
		
		Label create = new Label("Would you like to create a new group?");
		
		Button createGroup = new Button("Create!");
		createGroup.setFont(new Font(14));
		createGroup.setOnAction(event -> this.addNewGroup());
		
		extras.getChildren().addAll(groupChoice, setGroup, group, currentGroup, special, leave
				,create, newGroup, privateGroup, createGroup);
	}
	
	private void addNewGroup() {
		
		groupChoice.getItems().clear();
		
		ArrayList<String> groups = this.database.getGroupsAsAdmin();
		for(String i : groups)
			groupChoice.getItems().add(i);
		
		String sample = newGroup.getText();
		String key = "";
		int random = (int)Math.random()*1000000;
		
		if(privateGroup.isSelected())
			key = String.format("%d", random);
		
		if(!this.database.groupExist(sample)) {
			this.database.addGroup(sample, key);
			groupChoice.getItems().add(sample);
		}
	}
	
	/**
	 * This sets up the data when the user is entering this set.
	 */
	private void setUpData() {
		String sample = groupChoice.getValue();
		currentGroup.setText(sample);
		database.rightsManager.setCurrentGroup(sample);
		this.setUpLists();
		
		groupChoice.getItems().clear();
		
		ArrayList<String> groups = this.database.getGroupsAsAdmin();
		for(String i : groups)
			groupChoice.getItems().add(i);
	}
	
	/**
	 * This method attempts to remove the current user from the group
	 */
	private void leaveGroupAdmin() {
		database.rightsManager.deleteAdmin(currentUser, this.currentRole == Role.Instructor);
	}
	
	/**
	 * This is the method that will get triggered if the user decides to add a new admin. 
	 */
	private void addAdmin() {
		//gets the input from the admin textiflied
		String addedAdmin = addingAdmin.getText().trim();
		boolean first = database.firstInstructor();
		
		System.out.println(first);
		
		final boolean isInstructor = !isAdmin.isSelected();
		
		//see if hte admin can be added into the database
		boolean result = database.validUser(addedAdmin);
		boolean result2 = false;
		
		VBox correctColumn = this.chooseVBox(true, isInstructor);
		
		if(!correctColumn.getChildren().contains(addedAdmin)) {
		int roles = database.returnRole(addedAdmin);
		
		if(result && ((isInstructor && (roles % 5 == 0)) || (!isInstructor && (roles % 2 == 0)))) {
			result2 = this.database.addRight(addedAdmin, true, isInstructor);
			}
			
		//if the admin was added then adds a new button 
		if(result && result2)
		{
			Button newAdmin = new Button(addedAdmin);
			newAdmin.setPrefSize(250, 30);
			newAdmin.setOnAction(event -> this.totalRemove(true, newAdmin, isInstructor));
			
			correctColumn.getChildren().add(newAdmin);
			
			adminWarning.setText("");
			System.out.println(first);
			
			if(instructorsWithAW.getChildren().size() + instructorsWithVW.getChildren().size() == 1)
			{
				database.addRight(addedAdmin, false, isInstructor);
				if(!instructorsWithAW.getChildren().contains(addedAdmin)) {
					Button newViewer = new Button(addedAdmin);
					newViewer.setOnAction(event -> this.totalRemove(false, newViewer, isInstructor));
					newViewer.setPrefSize(250, 30);
					instructorsWithVW.getChildren().add(newViewer);}
			}
			return;
		}
		
		adminWarning.setText("User Doesn't Exist!");
	}
	adminWarning.setText("No Duplicates Allowed");	
	}
	
	/**
	 * This is the method that will get triggered if the user decides to add a new viewer
	 */
	private void addViewer(boolean isInstructor) {
		//gets the input from the viewr textfield
		String addedviewr = addingViewer.getText().trim();
		boolean first = this.database.firstInstructor();
		boolean result = database.validUser(addedviewr);
		boolean result2 = false;
		
		int roles = database.returnRole(addedviewr);
		VBox correctColumn = this.chooseVBox(false, isInstructor);
		
		if(!correctColumn.getChildren().contains(addedviewr)) {
		//see if the viewer can be added into the database
		if(result && ((isInstructor && (roles % 5 == 0)) || (!isInstructor && (roles % 3 == 0)))) {
		 result2 = database.addRight(addedviewr, false, isInstructor);
		}
		//if successful then adds the viewer into the button list
		if(result && result2) {
			Button newViewer = new Button(addedviewr);
			newViewer.setPrefSize(250, 30);
			newViewer.setOnAction(event -> this.totalRemove(false, newViewer, isInstructor));
			
			correctColumn.getChildren().add(newViewer);
			viewerWarning.setText("");
			
			if(instructorsWithAW.getChildren().size() + instructorsWithVW.getChildren().size() == 1)
			{
				Button newAdmin = new Button(addedviewr);
				newAdmin.setPrefSize(250, 30);
				newAdmin.setOnAction(event -> this.totalRemove(true, newAdmin, isInstructor));
				database.addRight(addedviewr, true, isInstructor);
				if(!instructorsWithAW.getChildren().contains(newAdmin)) {
					instructorsWithAW.getChildren().add(newAdmin);}
			}
			
			return;
		}
		viewerWarning.setText("User Doesn't Exist!");
	}
	viewerWarning.setText("Duplicates Not Allowed!");	
	}
	
	
	/**
	 * This methods sets up the widgets that will be placed in Admin storage.
	 * This includes the labels that clarify what 
	 */
	public void setUpAdminStorage() {
		//sets up a few labels
		Label adminM = new Label("Admin Management");
		adminM.setFont(new Font(18));
		
		Label adminA = new Label("Who Would you like to add as an admin?");
		adminA.setFont(new Font(14));
		
		//sets up the button that adds an admin
		Button adminAB = new Button("Give User Admin Rights");
		adminAB.setFont(new Font(14));
		
		isAdmin.setFont(new Font(10));
		
		adminAB.setOnAction(event -> this.addAdmin());
		
		//sets up the label that would show if something went wrong
		adminWarning.setFont(new Font(10));
		adminWarning.setTextFill(Color.RED);
		
		Label admins  = new Label("Press on the Admin to Remove:");
		admins.setFont(new Font(14));
		
		Label inst = new Label("Press on the Instructor to Remove:");
		inst.setFont(new Font(14));
		
		adminStorage.getChildren().addAll(adminM, adminA, addingAdmin, 
				adminAB, isAdmin, adminWarning, admins, adminsWithAW, inst, instructorsWithAW);
	}
	
	/**
	 * This method sets up the widgets that will be placed in Viewer storage.
	 * This includes the label that will clarify the functionalities of the widgets.
	 */
	public void setUpViewerStorage() {
		//sets up a couple of labels
		Label viewerM = new Label("Viewer Management");
		viewerM.setFont(new Font(18));
		
		Label viewerA = new Label("Who Would you like to add as a viewer?");
		viewerA.setFont(new Font(14));
		
		//sets up the button that would add a user as an admin
		Button viewerAB = new Button("Give User Viewer Rights");
		viewerAB.setFont(new Font(14));
		
		CheckBox isInstructor = new CheckBox("Add As an Instructor?");
		isInstructor.setFont(new Font(10));
		
		viewerAB.setOnAction(event -> this.addViewer(isInstructor.isSelected()));
		
		//sets up the label that would show is a user is added
		viewerWarning.setFont(new Font(10));
		viewerWarning.setTextFill(Color.RED);
		
		Label inst = new Label("Press on Instructor to Remove:");
		inst.setFont(new Font(14));
		
		Label viewers = new Label("Press on the Student to Remove:");
		viewers.setFont(new Font(14));
		
		viewerStorage.getChildren().addAll(viewerM, viewerA, addingViewer, 
				viewerAB, isInstructor, viewerWarning, inst, instructorsWithVW, viewers, studentsWithVW);
	}
	
	/**
	 * This method will set up the lists at the beginning of the page initalization
	 */
	private void setUpLists() {
		
		if(!this.database.isSpecial()) {
			special.setText("Special Group");
		}
		else
			special.setText("General Group");
		
		boolean searchAdmin = true;
		boolean isInstructor = true;
		
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 2; j++) {
				//ArrayList<String> list = database.rightsManager.selectColumn(searchAdmin, isInstructor);
				this.createList(searchAdmin, isInstructor);
				//System.out.println(searchAdmin + " " + isInstructor);
				isInstructor = !isInstructor;	
		}
		searchAdmin = false;	
		}
	}
	
	/**
	 * This is the method that would give the correct vbox
	 * @param checkAdmin
	 * @param isInstructor
	 * @return
	 */
	private VBox chooseVBox(boolean checkAdmin, boolean isInstructor) {
		if(checkAdmin) {
			if(isInstructor) {
				return instructorsWithAW;
				}
			else {
				return adminsWithAW;
				}
		}
		else {
			if(isInstructor) {
				return instructorsWithVW;
				}
			else {
				return studentsWithVW;
				}
		}
	}
	
	/**
	 * This creates the list of viewers and the buttons the removes them from the UI.
	 * Depending on the admin parameter, this will either edit the adminStorage or 
	 * viewerStorage list. 
	 * The list will only return a list if it doesn't have the current user in it.
	 * @param list
	 */
	public void createList(boolean checkAdmin, boolean isInstructor) {
		
		String[] getList = this.database.returnRights(checkAdmin, isInstructor).split("\\|");
		
		VBox editList = this.chooseVBox(checkAdmin, isInstructor);
		editList.getChildren().clear();
		
		for(String input : getList) {
		
			//adds everything except if the user had admin status
			if((!checkAdmin || input != currentUser) && input.length() > 0 && 
					!input.contains("|") && !input.contains(" ")) {
				Button user = new Button(input);
				user.setPrefSize(250, 30);
			
			//creates and sets up the button
			user.setOnAction(event -> this.totalRemove(checkAdmin, user, isInstructor));
			
			editList.getChildren().add(user);}}}
	
	/**
	 * This is the method that will assure the button will be removed from the Button list and 
	 * the database. 
	 * @param admin
	 * @param trigger
	 */
	public void totalRemove(boolean admin, Button trigger, boolean isInstructor) {
		
		System.out.println("Trigger");
		
		VBox editList = this.chooseVBox(admin, isInstructor);
		boolean result = false;
		int admins = this.chooseVBox(true, true).getChildren().size();
		int instructors = this.chooseVBox(true, false).getChildren().size();
		
		if(admin && (currentRole == Role.Admin && (admins + instructors > 1))) {//only removes admin rights if the user is an admin
			 result = database.deleteRight(trigger.getText(), true, isInstructor);
		}
		else if(!admin){//process for removing viewer rights
			 result = database.deleteRight(trigger.getText(), false, isInstructor);
			 
		}
		
		if(result)
			editList.getChildren().remove(trigger);
	}

	/**
	 * This button returns the root of the page. 
	 * @return
	 */
	public HBox getRoot() {
		return root;
	}
	
}
