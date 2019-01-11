import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;



import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;



import javafx.geometry.*;

/**
 *
 * Created by blueRose on 14/12/2018
 * Version 0.1.0
 * 
 */
public class realityMain extends Application
{
	static String url = "jdbc:sqlite:reality database.db";
	
	ObservableList<String> tags;
	
	ListView<String> listView;
	
	List<String> paths, thumb_paths;
	
	
	FlowPane flowpane;
	
	BorderPane borderpane = new BorderPane();
	
	ScrollPane s1 = new ScrollPane();
	
	Seishain app = new Seishain();
	
    public static void connect()
    {
        Connection conn = null;
        try {
        	
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");
            
        } catch (SQLException e){
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    public static void createTables()
    {   
        String sql1 = "CREATE TABLE `sashin` (\r\n" + 
        		"	`sashin_id`	integer,\r\n" + 
        		"	`sashin_name`	TEXT NOT NULL UNIQUE,\r\n" + 
        		"	`path`	TEXT NOT NULL UNIQUE,\r\n" + 
        		"	`location`	TEXT,\r\n" + 
        		"	`thumb_path`	TEXT,\r\n" + 
        		"	PRIMARY KEY(`sashin_id`)\r\n" + 
        		");";
        
        String sql2 = "CREATE TABLE `tag` (\r\n"+ 
        		"	`tag_id`	INTEGER,\r\n" + 
        		"	`tag_name`	INTEGER UNIQUE,\r\n" + 
        		"	PRIMARY KEY(`tag_id`)\r\n" + 
        		");";
        
        String sql3 = "CREATE TABLE `tagmap` (\r\n" + 
        		"	`tagmap_id`	INTEGER,\r\n" + 
        		"	`sashin_id`	INTEGER,\r\n" + 
        		"	`tag_id`	INTEGER,\r\n" + 
        		"	FOREIGN KEY(`sashin_id`) REFERENCES `sashin`(`sashin_id`),\r\n" + 
        		"	PRIMARY KEY(`tagmap_id`),\r\n" + 
        		"	FOREIGN KEY(`tag_id`) REFERENCES `tag`(`tag_id`)\r\n" + 
        		");";
        
        String sql4 = "CREATE TABLE `directories` (\r\n" + 
        		"	`dir_id`	INTEGER,\r\n" + 
        		"	`folder_path`	INTEGER UNIQUE,\r\n" + 
        		"	PRIMARY KEY(`dir_id`)\r\n" + 
        		");";
        
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
        	
            stmt.execute(sql1);
            stmt.execute(sql2);
            stmt.execute(sql3);
            stmt.execute(sql4);
            
            System.out.println("Database created.");      
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void main(String[] args)
    {
    	 File file = new File ("reality database.db");
    	 if(file.exists())
    	     {
    		  	 connect();
    	     }
    	     else
    	     {
    	    	 createTables();
    	     }

		 
		 launch(args);
		 
		 
    }

	@Override
	public void start(Stage primaryStage) throws Exception {
		Stage window = primaryStage;
		window.setTitle("Reality");
		
		refresh();
		
		
		
		
		//Top Component: Menu
		Menu fileMenu = new Menu("_File");
		MenuItem scanFile = new MenuItem("Scan Directories");
		scanFile.setOnAction(e -> {
										for (String folderPath : app.getFolderPaths())
								        {
								        	app.scan(folderPath);
								        }
									});
		MenuItem addDirFile = new MenuItem("Add Directories...");
		addDirFile.setOnAction(e -> addDirectories());
		MenuItem removeDirFile = new MenuItem("Remove Directories...");
		removeDirFile.setOnAction(e -> removeDirectories());
		fileMenu.getItems().addAll(scanFile,addDirFile,removeDirFile);
		
		Menu tagMenu = new Menu("_Tags");
		MenuItem removetagsTag = new MenuItem("Remove tags...");
		removetagsTag.setOnAction(e -> removeTagMenu());
		tagMenu.getItems().addAll(removetagsTag);
		
		Menu aboutMenu = new Menu("_About");
		MenuItem githubAbout = new MenuItem("GitHub");
		githubAbout.setOnAction(e -> {
			try {
				java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://github.com/blueRoseXIV/Reality"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		aboutMenu.getItems().addAll(githubAbout);
		
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(fileMenu, tagMenu, aboutMenu);
		
		borderpane.setTop(menuBar);

		
		Scene scene = new Scene(borderpane, 1200, 400);
		window.setScene(scene);
		window.show();
	}
	
	private void refresh()
	{
		List<String> listOfTags = app.showAllTags();
		listOfTags.sort(null);
		
		//Left Component: List of tags
		listView = new ListView<>();
		for (int i=0; i<listOfTags.size() ; i++) {
			listView.getItems().add(listOfTags.get(i));
		}
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listView.getSelectionModel().selectedItemProperty().addListener( e -> {
			try {
				handler();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		} );


		System.out.println(tags);
		
		//Main Component: Files
		flowpane = new FlowPane();
		flowpane.setVgap(8);
		flowpane.setHgap(8);

		s1.setFitToWidth(true);
		s1.setContent(flowpane);
		
		borderpane.setLeft(listView);
		borderpane.setCenter(s1);
		
	}
	
	private void handler() throws FileNotFoundException
	{
		tags = listView.getSelectionModel().getSelectedItems();
		paths = app.IDsToPaths(app.showSashinFromTagName(tags));
		
		System.out.println(tags);
		System.out.println( app.showSashinFromTagName(tags) );
		System.out.println( paths );
		
		flowpane.getChildren().clear();
		
		
		for (String path : paths)
		{
			File file = new File( app.PathToThumbPath(path) );
			if (file.canRead()) {
				FileInputStream input = new FileInputStream(file);
				Image image = new Image(input);
				ImageView imageView = new ImageView(image);
				int sashin_id = app.PathToID(path);
				ContextMenu contextMenu = new ContextMenu();
				MenuItem open = new MenuItem("Open");
				open.setOnAction(e -> {
					try {
						Desktop.getDesktop().open(new File(path));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				});
				SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
				MenuItem addTag = new MenuItem("Add tag");
				addTag.setOnAction(e -> addTag(sashin_id, listView));
				MenuItem removeTag = new MenuItem("Remove tag");
				removeTag.setOnAction(e -> removeTag(sashin_id, listView));
				SeparatorMenuItem separatorMenuItem2 = new SeparatorMenuItem();
				MenuItem removeSashin = new MenuItem("Remove image from database");
				removeSashin.setOnAction(e -> confirmRemoval(sashin_id));
				contextMenu.getItems().addAll(open, separatorMenuItem, addTag, removeTag, separatorMenuItem2,
						removeSashin);
				imageView.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));
				flowpane.getChildren().add(imageView);
			}else alert("Error!", "Perform a directory scan!");
		}
	}

		
	    public void addTag(int sashin_id, ListView<String> listview) {
	        Stage window = new Stage();
	        Seishain app = new Seishain();
	        Button button = new Button("OK");
	        
	        //Block events to other windows
	        window.initModality(Modality.APPLICATION_MODAL);
	        window.setTitle("Add a new tag");
	        window.setMinWidth(350);

	        ComboBox<Object> comboBox = new ComboBox<>();
	        for (String alltag : app.showAllTags())
	        {
	        	comboBox.getItems().add(alltag);
	        }
	        comboBox.setPromptText("Type a new tag or select an old one");
	        comboBox.setEditable(true);
	        
	        button.setOnAction(e -> { 	app.tagInsert((String) comboBox.getValue(), sashin_id);
	        							app.addToListView(listview, (String) comboBox.getValue());
	        						  	window.close();     						 
	        							});
	        
	        HBox layout = new HBox(10);
	        layout.getChildren().addAll(comboBox, button);
	        layout.setAlignment(Pos.CENTER);


	        //Display window and wait for it to be closed before returning
	        Scene scene = new Scene(layout, 350, 50);
	        window.setScene(scene);
	        window.showAndWait();
	    }
	    
	    public void removeTag(int sashin_id, ListView<String> listview) {
	        Stage window = new Stage();
	        Seishain app = new Seishain();
	        Button button = new Button("OK");
	        
	        //Block events to other windows
	        window.initModality(Modality.APPLICATION_MODAL);
	        window.setTitle("Tag Removal");
	        window.setMinWidth(350);

	        ComboBox<Object> comboBox = new ComboBox<>();
	        for (String itstag : app.showSashinsTags(sashin_id))
	        {
	        	comboBox.getItems().add(itstag);
	        }
	        comboBox.setPromptText("Choose a tag to remove");
	        
	        button.setOnAction(e -> { 	boolean lastTag = app.tagRemove((String) comboBox.getValue(), sashin_id);
	        							if (lastTag) listview.getItems().remove(listview.getItems().indexOf(comboBox.getValue()));
	        						  	window.close();     						 
	        							});
	        
	        HBox layout = new HBox(10);
	        layout.getChildren().addAll(comboBox, button);
	        layout.setAlignment(Pos.CENTER);


	        //Display window and wait for it to be closed before returning
	        Scene scene = new Scene(layout, 350, 50);
	        window.setScene(scene);
	        window.showAndWait();
	    }
	    
	    public void addDirectories() {
	        Stage window   = new Stage();
	        Seishain app   = new Seishain();
	        Button OKButton = new Button("OK");
	        Button browseButton = new Button("Browse...");
	        
	        //Block events to other windows
	        window.initModality(Modality.APPLICATION_MODAL);
	        window.setTitle("Add Directories");
	        window.setMinWidth(350);
	        
	        TextField text = new TextField();
	        text.setPrefWidth(400);
	        
	        DirectoryChooser chooser = new DirectoryChooser();
	        chooser.setTitle("Choose a folder");
	        
	        browseButton.setOnAction( e -> { File selectedFolder = chooser.showDialog(window);
	        								 text.setText(selectedFolder.getAbsolutePath());
	        								 });
	        
	        OKButton.setOnAction( e ->{  app.addFolderPath(text.getText());
	        							 app.scan(text.getText());
	        							 refresh();
	        							 window.close(); });
	        
	        GridPane layout = new GridPane();
	        layout.add(text, 0, 0);
	        layout.add(browseButton, 1, 0);
	        layout.add(OKButton, 0, 1);
	        layout.setAlignment(Pos.CENTER);
	        layout.setVgap(5);
	        layout.setHgap(5);
	        
	        //Display window and wait for it to be closed before returning
	        Scene scene = new Scene(layout, 500, 72);
	        window.setScene(scene);
	        window.showAndWait();
	    }
	    

		public void removeDirectories() {
	        Stage window = new Stage();
	        Seishain app = new Seishain();
	        Button button = new Button("OK");
	        
	        //Block events to other windows
	        window.initModality(Modality.APPLICATION_MODAL);
	        window.setTitle("Directory Removal");
	        window.setMinWidth(350);
	        
	        Label label = new Label("Also remove database entries for selected directory: ");
	        
	        ToggleGroup toggleGroup = new ToggleGroup();
	        RadioButton button1 = new RadioButton("Yes");
	        button1.setToggleGroup(toggleGroup);
	        button1.setSelected(true);
	        RadioButton button2 = new RadioButton("No");
	        button2.setToggleGroup(toggleGroup);
	        button2.setSelected(true);

	        ComboBox<Object> comboBox = new ComboBox<>();
	        for (String folderPath : app.getFolderPaths())
	        {
	        	comboBox.getItems().add(folderPath);
	        }
	        comboBox.setPromptText("Choose a directory to remove");
	        
	        button.setOnAction(e -> {
	        							app.removeFolderPath((String) comboBox.getValue());
	        							if (button1.isSelected()) {
	        								app.deleteEntriesFromLocation((String) comboBox.getValue());
	        							}
	        							refresh();
	        						  	window.close();     						 
	        							});
	        
	        HBox hbox = new HBox(10);
	        hbox.getChildren().addAll(label, button1, button2);
	        hbox.setAlignment(Pos.CENTER);
	        
	        VBox layout = new VBox(7);
	        layout.getChildren().addAll(comboBox, hbox, button);
	        layout.setPrefWidth(500);
	        layout.setAlignment(Pos.CENTER);


	        //Display window and wait for it to be closed before returning
	        Scene scene = new Scene(layout, 500, 100);
	        window.setScene(scene);
	        window.showAndWait();
		}
	    
	    public void alert(String title, String message) {
	        Stage window = new Stage();
	        Button button = new Button("OK");
	        
	        //Block events to other windows
	        window.initModality(Modality.APPLICATION_MODAL);
	        window.setTitle(title);
	        window.setMinWidth(350);
	        
	        Label label = new Label(message);
	        
	        button.setOnAction( e ->window.close() );
	        
	        VBox layout = new VBox(5);
	        layout.getChildren().addAll(label, button);
	        layout.setAlignment(Pos.CENTER);


	        //Display window and wait for it to be closed before returning
	        Scene scene = new Scene(layout, 350, 72);
	        window.setScene(scene);
	        window.showAndWait();
	    }

		public void confirmRemoval(int sashin_id) {
	        Stage window = new Stage();
	        Seishain app = new Seishain();
	        Button buttonYes = new Button("Yes");
	        Button buttonNo = new Button("No");
	        
	        //Block events to other windows
	        window.initModality(Modality.APPLICATION_MODAL);
	        window.setTitle("Remove from database");
	        window.setMinWidth(350);
	        
	        Label label = new Label("Remove from database:");
	        
	        buttonYes.setOnAction( e -> 	{app.removeFromDB(sashin_id);
	        								 window.close(); });
	        
	        buttonNo.setOnAction( e -> window.close() );
	        
	        HBox hbox = new HBox(10);
	        hbox.getChildren().addAll(buttonYes, buttonNo);
	        hbox.setAlignment(Pos.CENTER);
	        
	        VBox layout = new VBox(7);
	        layout.getChildren().addAll(label, hbox);
	        layout.setPrefWidth(500);
	        layout.setAlignment(Pos.CENTER);

	        //Display window and wait for it to be closed before returning
	        Scene scene = new Scene(layout, 350, 72);
	        window.setScene(scene);
	        window.showAndWait();
	        
		}

		public void removeTagMenu() {
	        Stage window = new Stage();
	        Seishain app = new Seishain();
	        Button button = new Button("OK");
	        
	        //Block events to other windows
	        window.initModality(Modality.APPLICATION_MODAL);
	        window.setTitle("Tag Removal");
	        window.setMinWidth(350);

	        ComboBox<Object> comboBox = new ComboBox<>();
	        for (String tag : app.showAllTags())
	        {
	        	comboBox.getItems().add(tag);
	        }
	        comboBox.setPromptText("Choose a tag to remove");
	        
	        button.setOnAction(e -> { 	app.deleteTag( app.tagNameToID( (String) comboBox.getValue() ) );
	        							refresh();
	        						  	window.close();     						 
	        							});
	        
	        HBox layout = new HBox(10);
	        layout.getChildren().addAll(comboBox, button);
	        layout.setAlignment(Pos.CENTER);


	        //Display window and wait for it to be closed before returning
	        Scene scene = new Scene(layout, 350, 50);
	        window.setScene(scene);
	        window.showAndWait();
		}
}
    