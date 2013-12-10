package de.l3s.dlg.ncbikraken.ftp;

public enum MedlineFileType {
	
	GZ ("gz"),
	ZIP ("zip");

	private String relativeServerPath;

	MedlineFileType(String path) {
		this.relativeServerPath = path;
	}
	
	public String getServerPath() {
		return this.relativeServerPath;
	}
}
