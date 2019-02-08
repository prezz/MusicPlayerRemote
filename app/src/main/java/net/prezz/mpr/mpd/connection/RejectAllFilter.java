package net.prezz.mpr.mpd.connection;

public class RejectAllFilter implements Filter {

	public static RejectAllFilter INSTANCE = new RejectAllFilter();
	
	private RejectAllFilter() {
	}
	
	@Override
	public boolean accepts(String line) {
		return false;
	}
}
