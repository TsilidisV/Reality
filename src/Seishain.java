 
import javafx.scene.control.ListView;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

import javax.swing.*;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 

public class Seishain
{
	
    private Connection connect()
    {
        String url = "jdbc:sqlite:reality database.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void sashinInsert(String name, String path, String location, String thumb_path)
    {
        String sql   = "INSERT INTO sashin(sashin_name,path,location,thumb_path) VALUES(?,?,?,?)";
        try (Connection conn = this.connect();
                PreparedStatement pstmt   = conn.prepareStatement(sql);
        		){
            pstmt.setString(1, name);
            pstmt.setString(2, path);
            pstmt.setString(3, location);
            pstmt.setString(4, thumb_path);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void tagInsert(String tag_name, int sashin_id)
    {
        String sql1 = "INSERT INTO tag(tag_name) VALUES(?)";
        String sql2 = "INSERT INTO tagmap(sashin_id,tag_id) VALUES(?,?)";
        String sqlA = "SELECT tag_id FROM tag WHERE tag_name=?";
        String sqlB = "SELECT tag_name FROM tag";
        
        String sqlI  = "SELECT tag_id FROM tag WHERE tag_name=?";
        String sqlII = "SELECT tagmap_id FROM tagmap WHERE tag_id = ? AND sashin_id = ?";
    	int tag_id = -1;
 
        try (Connection conn = this.connect();
                PreparedStatement pstmt1 = conn.prepareStatement(sql1);
        		PreparedStatement pstmt2 = conn.prepareStatement(sql2);
        		PreparedStatement pstmtA = conn.prepareStatement(sqlA);
        		Statement stmt = conn.createStatement();
        		PreparedStatement pstmtI  = conn.prepareStatement(sqlI);
        		PreparedStatement pstmtII = conn.prepareStatement(sqlII);
                ) {
        	pstmtA.setString(1,tag_name);
        	
        	List<String> listOfTags = new ArrayList<String>();
        	ResultSet rs1    = stmt.executeQuery(sqlB);
        	while (rs1.next()) {
                listOfTags.add(rs1.getString("tag_name"));                                 
            }
        	rs1.close();
        	
        	//checks if a tag with the same name exists in table: tag
            if (!listOfTags.contains(tag_name))
            {
				pstmt1.setString(1, tag_name);
				pstmt1.executeUpdate();
			}
            
            pstmtI.setString(1,tag_name); 
            ResultSet rsI    = pstmtI.executeQuery();
            tag_id=rsI.getInt("tag_id");
            rsI.close();
            
            pstmtII.setInt(1, tag_id);
            pstmtII.setInt(2, sashin_id);
            ResultSet rsII = pstmtII.executeQuery();
            
            //checks if a same row exists in table: tagmap
			if (!rsII.next()) {
				pstmt2.setInt(1, sashin_id);
				ResultSet rsA = pstmtA.executeQuery();
				pstmt2.setInt(2, rsA.getInt("tag_id"));
				pstmt2.executeUpdate();
				rsA.close();
			} 
			rsII.close();
            
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public boolean tagRemove(String tag_name,int sashin_id)
    {
    	boolean lastTag = false;
    	if ( !tag_name.equals("All") ) {
    		System.out.println(tag_name);
			int tag_id = tagNameToID(tag_name);
			String sql1 = "DELETE FROM tagmap WHERE tag_id = ? AND sashin_id = ?";
			String sql2 = "SELECT tagmap_id FROM tagmap WHERE tag_id = ?";
			String sql3 = "DELETE FROM tag WHERE tag_id = ?";
			try (Connection conn = this.connect();
					PreparedStatement pstmt1 = conn.prepareStatement(sql1);
					PreparedStatement pstmt2 = conn.prepareStatement(sql2);
					PreparedStatement pstmt3 = conn.prepareStatement(sql3);
			) {

				pstmt1.setInt(1, tag_id);
				pstmt1.setInt(2, sashin_id);
				pstmt1.executeUpdate();

				pstmt2.setInt(1, tag_id);
				ResultSet rs2 = pstmt2.executeQuery();

				if (!rs2.next()) {
					rs2.close();
					pstmt3.setInt(1, tag_id);
					pstmt3.executeUpdate();
					lastTag = true;
				}

			} catch (SQLException e) {
				System.out.println(e.getMessage());
			} 
		}else box.alert("Error!", "'" + tag_name + "' tag cannot be removed!");
		
    	//returns true if the tag removed isn't associated with other files
    	return lastTag;
    }
    
    public void deleteTag(int tag_id)
    {
    	String sql1 = "DELETE FROM tag WHERE tag_id = ?";
    	String sql2 = "DELETE FROM tagmap WHERE tag_id = ?";
    	
        try (Connection conn = this.connect();
                PreparedStatement pstmt1 = conn.prepareStatement(sql1);
        		PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                ) {
        	pstmt1.setInt(1, tag_id);
        	pstmt1.executeUpdate();
        	
        	pstmt2.setInt(1, tag_id);
        	pstmt2.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public List<String> showSashinsTags(int sashin_id)
    {
    	String sql1 = "SELECT tag_id FROM tagmap WHERE sashin_id=?";
    	String sql2 = "SELECT tag_name FROM tag WHERE tag_id=?";
    	List<String> listOfTags = new ArrayList<String>();
   	 
   	 try (Connection conn = this.connect();
               PreparedStatement pstmt1 = conn.prepareStatement(sql1);
   			   PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                ) {
   		
   		    pstmt1.setInt(1,sashin_id);
        	ResultSet rs1 = pstmt1.executeQuery();
        	
        	while (rs1.next()) {
        		pstmt2.setInt(1,rs1.getInt("tag_id"));
        		ResultSet rs2 = pstmt2.executeQuery();
        		listOfTags.add(rs2.getString("tag_name"));
        		rs2.close();
            }
        	rs1.close();
        	     
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }
		return listOfTags;

    }
    
    public List<Integer> showSashinFromTag(int tag_id)
    {
    	 String sql = "SELECT sashin_id FROM tagmap WHERE tag_id=?";
    	 List<Integer> listOfSashins = new ArrayList<Integer>();
    	 
    	 try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                 ) {
    		
    		pstmt.setInt(1,tag_id); 
         	ResultSet rs    = pstmt.executeQuery();
         	
         	while (rs.next()) {
         		listOfSashins.add(rs.getInt("sashin_id"));
                                  
             }
         	rs.close();
         	     
         }catch (SQLException e) {
             System.out.println(e.getMessage());
         }
		return listOfSashins;
    }
    
    public List<Integer> showSashinFromTagName(List<String> tagNames)
    {
    	int id=0;
    	List<Integer> sashinIDs = new ArrayList<Integer>();
    	
    	for (String tagName : tagNames)
    	{
    		String sql1 = "SELECT tag_id FROM tag WHERE tag_name=?";
    		try (Connection conn = this.connect();
    				PreparedStatement pstmt = conn.prepareStatement(sql1);
    				) {
		
    			pstmt.setString(1,tagName); 
    			ResultSet rs1 = pstmt.executeQuery();
    			id = rs1.getInt(1);
    			rs1.close();
    			
    		}catch (SQLException e) {
    			System.out.println(e.getMessage());
    		}
    		
	    	 String sql = "SELECT sashin_id FROM tagmap WHERE tag_id=?";
	    	 
	    	 try (Connection conn = this.connect();
	                PreparedStatement pstmt = conn.prepareStatement(sql);
	                 ) {
	    		
	    		pstmt.setInt(1,id); 
	         	ResultSet rs    = pstmt.executeQuery();
	         	
	         	while (rs.next()) {
	         		if (!sashinIDs.contains(rs.getInt("sashin_id"))) {
						sashinIDs.add(rs.getInt("sashin_id"));
					}                
	             }
	         	rs.close();
	         	     
	         }catch (SQLException e) {
	             System.out.println(e.getMessage());
	         }
    	}
		return sashinIDs;
    }
    
    public int tagNameToID(String tag_name)
    {
    	String sql = "SELECT tag_id FROM tag WHERE tag_name=?";
    	int tag_id = -1;
   	 	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                 ) {
    		
    		pstmt.setString(1,tag_name); 
         	ResultSet rs    = pstmt.executeQuery();
         	tag_id=rs.getInt("tag_id");
         	rs.close();
         	     
         }catch (SQLException e) {
             System.out.println(e.getMessage());
         }
   	 	return tag_id;
    }
    
    public List<String> IDsToThumbPaths (List<Integer> sashin_ids)
    {
    	List<String> thumbPaths = new ArrayList<String>();
    	String sql = "SELECT thumb_path FROM sashin WHERE sashin_id=?";
    	
    	for (int sashin_id : sashin_ids)
    	{
       	 
       	 	try (Connection conn = this.connect();
                   PreparedStatement pstmt = conn.prepareStatement(sql);
                    ) {
       		
       		pstmt.setInt(1,sashin_id); 
            	ResultSet rs    = pstmt.executeQuery();
            	
            	while (rs.next()) {
            		thumbPaths.add(rs.getString("thumb_path"));            
                }
            	rs.close();
            	     
            }catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
    	return thumbPaths;
    }
    
    public List<String> IDsToPaths (List<Integer> sashin_ids)
    {
    	List<String> sashinPaths = new ArrayList<String>();
    	String sql = "SELECT path FROM sashin WHERE sashin_id=?";
    	
    	for (int sashin_id : sashin_ids)
    	{
       	 
       	 	try (Connection conn = this.connect();
                   PreparedStatement pstmt = conn.prepareStatement(sql);
                    ) {
       		
       		pstmt.setInt(1,sashin_id); 
            	ResultSet rs    = pstmt.executeQuery();
            	
            	while (rs.next()) {
            		sashinPaths.add(rs.getString("path"));            
                }
            	rs.close();
            	     
            }catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
    	return sashinPaths;
    }
    
    public String PathToThumbPath(String path)
    {
    	String sql = "SELECT thumb_path FROM sashin WHERE path=?";
    	String thumb_path = "";
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                 ) {
    		
    		pstmt.setString(1,path); 
         	ResultSet rs = pstmt.executeQuery();
         	thumb_path = rs.getString("thumb_path");
         	rs.close();
         	
         	     
         }catch (SQLException e) {
             System.out.println(e.getMessage());
             realityMain ok = new realityMain();
             ok.alert("Error!", "Scan Directories");
             }
 		return thumb_path;
    }
    
    public int PathToID(String path)
    {
    	String sql = "SELECT sashin_id FROM sashin WHERE path=?";
    	int sashin_id = -1;
    	
    	try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                 ) {
    		
    		pstmt.setString(1,path); 
         	ResultSet rs = pstmt.executeQuery();
         	sashin_id = rs.getInt("sashin_id");
         	rs.close();
         	     
         }catch (SQLException e) {
             System.out.println(e.getMessage());
         }
 		return sashin_id;
    }
    
    public void addToListView(ListView<String> listview, String newTag)
    {
    	if (!listview.getItems().contains(newTag)) {
    		listview.getItems().add(newTag);
    		listview.getItems().sort(null);
    	}
    }
    
	public List<String> showAllTags()
	{
		String sql = "SELECT tag_name FROM tag";
		List<String> listOfTags = new ArrayList<String>();
		try(Connection conn = this.connect();
				Statement stmt = conn.createStatement();
				){
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next())
			{
                listOfTags.add(rs.getString("tag_name"));
            }
			rs.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listOfTags;
	}
	
    public void scan(String targetFolder)
	{
		File folder = new File(targetFolder);
		File[] listOfFiles = folder.listFiles();
		Seishain app = new Seishain();
		File destinationDir = new File("thumbnails");
		
		if (!destinationDir.exists()) new File("thumbnails").mkdirs();
		
		//create thumbnails
		try {
			Thumbnails.of(listOfFiles)
			.size(200,300)
			.toFiles(destinationDir,Rename.PREFIX_DOT_THUMBNAIL);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (File file : listOfFiles)
				{
					if (file.isFile())

						
						
					    app.sashinInsert(file.getName(), file.getAbsolutePath(), file.getParent(), destinationDir+"\\thumbnail."+file.getName() );
					 	app.tagInsert("All", app.PathToID(file.getAbsolutePath()));
				}
	}
    
    public void thumbnailCreator()
	{
    	String sql1 = "SELECT sashin_name FROM sashin";
    	String sql2 = "SELECT thumb_path FROM sashin";
    	List<String> listOfNames = new ArrayList<String>();
    	
		try(Connection conn = this.connect();
				Statement stmt1 = conn.createStatement();
				Statement stmt2 = conn.createStatement();
				){
			ResultSet rs1 = stmt1.executeQuery(sql1);
			while (rs1.next()) {
				listOfNames.add(rs1.getString("sashin_name"));
			}
			rs1.close();
			
			ResultSet rs2 = stmt2.executeQuery(sql2);
			while (rs2.next()) {
                if (rs2.getString("thumb_path") == null) {
                	
                }
            }
			rs2.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
    
    public void addFolderPath(String folder_path)
    {
    	 String sql = "INSERT INTO directories(folder_path) VALUES(?)";
    	 try (Connection conn = this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
    	                 ) {
    		 List<String> folderPaths = getFolderPaths(); 
    		 if (!folderPaths.contains(folder_path)){
    			 pstmt.setString(1, folder_path);
    	 		 pstmt.executeUpdate();
		 	 }else box.alert("Error!", "Directory already included!");
    	 }catch (SQLException e) {
    	             System.out.println(e.getMessage());
    	 }
    }
    
    public void removeFolderPath(String folder_path)
    {
    	String sql = "DELETE FROM directories WHERE folder_path = ?";
    	
        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ) {
        	pstmt.setString(1, folder_path);
        	pstmt.executeUpdate();
        	
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public List<String> getFolderPaths()
    {
    	String sql = "SELECT folder_path FROM directories";
    	List<String> folderPaths = new ArrayList<String>();
    	
   	 	try (Connection conn = this.connect();
   	 		Statement stmt = conn.createStatement();
                 ) {
   	 		ResultSet rs = stmt.executeQuery(sql);
   	 		while (rs.next()) {
   	 			folderPaths.add(rs.getString("folder_path"));
   	 		}
         	     
         }catch (SQLException e) {
             System.out.println(e.getMessage());
         }
   	 	return folderPaths;
    }
    
    public void removeFromDB(int sashin_id)
    {
    	String sql1 = "DELETE FROM sashin WHERE sashin_id = ?";
    	String sql2 = "DELETE FROM tagmap WHERE sashin_id = ?";
    	
        try (Connection conn = this.connect();
        		PreparedStatement pstmt1 = conn.prepareStatement(sql1);
        		PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                ) {       	
        	pstmt1.setInt(1, sashin_id);
        	pstmt1.executeUpdate();
        	
        	pstmt2.setInt(1, sashin_id);
        	pstmt2.executeUpdate();
        	
        	
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void deleteEntriesFromLocation(String directory)
    {
    	String sql1 = "SELECT sashin_id FROM sashin WHERE location = ?";
    	String sql2 = "DELETE FROM sashin WHERE location = ?";
    	String sql3 = "DELETE FROM tagmap WHERE sashin_id = ?";
    	
        try (Connection conn = this.connect();
                PreparedStatement pstmt1 = conn.prepareStatement(sql1);
        		PreparedStatement pstmt2 = conn.prepareStatement(sql2);
        		PreparedStatement pstmt3 = conn.prepareStatement(sql3);
                ) {       	
        	pstmt2.setString(1, directory);
        	pstmt2.executeUpdate();
        	
        	pstmt1.setString(1, directory);
        	ResultSet rs = pstmt1.executeQuery();
        	
        	while (rs.next()) {
        		pstmt3.setInt(1, rs.getInt("sashin_id"));
        		pstmt3.executeUpdate();
        	}
        	rs.close();
        	
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public DefaultListModel retrieveAllTags()
    {
        DefaultListModel dm = new DefaultListModel();

        String sql = "SELECT tag_name FROM tag";

        try (
            Connection con =  this.connect();

            //PREPARED STMT
            Statement s = con.createStatement();){
            ResultSet rs = s.executeQuery(sql);

            //LOOP THRU GETTING ALL VALUES
            while (rs.next()) {
                //GET VALUES
                String name = rs.getString("tag_name");

                //ADD TO DM
                dm.addElement(name);
            }

            return dm;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
