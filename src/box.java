import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import java.io.File;

import javafx.geometry.*;

public class box {

	
    public static void addTag(int sashin_id, ListView<String> listview) {
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
    
    public static void removeTag(int sashin_id, ListView<String> listview) {
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
    
    public static void addDirectories() {
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
    

	public static void removeDirectories() {
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
    
    public static void alert(String title, String message) {
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

	public static void confirmRemoval(int sashin_id) {
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

	public static void removeTagMenu() {
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