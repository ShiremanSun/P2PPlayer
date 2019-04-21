package com.itheima.ck.bean;

import java.io.Serializable;

public class MovieBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	public String name;
	public String details;
	public String datasourcePath;
	public String imagePathString;
	public String torrentpathString;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	
	public void setImagePathString(String imagePathString) {
		this.imagePathString = imagePathString;
	}
	public void setTorrentPathString(String torrentpathString) {
		this.torrentpathString = torrentpathString;
	}
	
    public String getName(){
    	return name;
    }
    
    public String getDetails() {
    	return details;
    }
    public String getDataSourcePath() {
    	return datasourcePath;
    }
   
    public void setDataSourcePath(String dataSourcePath) {
    	this.datasourcePath = dataSourcePath;
    }
    public String getImagePathString() {
    	return imagePathString;
    }
    public String getTorrentPathString() {
    	return torrentpathString;
    }

}
