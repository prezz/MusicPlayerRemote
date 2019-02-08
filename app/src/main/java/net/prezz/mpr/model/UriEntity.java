package net.prezz.mpr.model;

import java.io.Serializable;

import net.prezz.mpr.Utils;

public class UriEntity implements Serializable {

	private static final long serialVersionUID = -6990570522929131413L;

	public static final String DIR_SEPERATOR = "/";
	public enum UriType { DIRECTORY, FILE };
	public enum FileType { NA, MUSIC, PLAYLIST };

	private UriType uriType;
	private FileType fileType;
	private String parentUriPath;
	private String uriPath;
	
	public UriEntity(UriType uriType, FileType fileType, String parentUriPath, String uriPath) {
		this.uriType = uriType;
		this.fileType = fileType;
		this.parentUriPath = parentUriPath;
		this.uriPath = uriPath;
	}
	
	public UriType getUriType() {
		return uriType;
	}

    public FileType getFileType() {
        return fileType;
    }

	public String getParentUriPath() {
		return parentUriPath;
	}

	public String getUriPath() {
		return uriPath;
	}
	
	public String getFullUriPath(boolean appendDirSeperator) {
		StringBuilder sb = new StringBuilder();
		sb.append(parentUriPath);
		sb.append(uriPath);
		if (appendDirSeperator && uriType == UriType.DIRECTORY) {
			sb.append(DIR_SEPERATOR);
		}
		return sb.toString();
	}
	
	public String getUriFilname() {
		if (uriType == UriType.FILE) {
			String[] sections = uriPath.split(DIR_SEPERATOR);
			return sections[sections.length - 1];
		}
		
		return "";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj instanceof UriEntity) {
			UriEntity other = (UriEntity) obj;
			
			if (!Utils.equals(this.uriType, other.uriType)) {
				return false;
			}
            if (!Utils.equals(this.fileType, other.fileType)) {
                return false;
            }
			if (!Utils.equals(this.parentUriPath, other.parentUriPath)) {
				return false;
			}
			if (!Utils.equals(this.uriPath, other.uriPath)) {
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		
		hash = 31 * hash + Utils.hashCode(uriType);
        hash = 31 * hash + Utils.hashCode(fileType);
		hash = 31 * hash + Utils.hashCode(parentUriPath);
		hash = 31 * hash + Utils.hashCode(uriPath);
		
		return hash;
	}
}
