package net.prezz.mpr.model;

public class SearchResult {

	private LibraryEntity[] libraryEntities;
	private UriEntity[] uriEntities;
	
	
	public SearchResult(LibraryEntity[] libraryEntities, UriEntity[] uriEntities) {
		this.libraryEntities = libraryEntities;
		this.uriEntities = uriEntities;
	}

	public LibraryEntity[] getLibraryEntities() {
		return libraryEntities;
	}
	
	public UriEntity[] getUriEntities() {
		return uriEntities;
	}
}
