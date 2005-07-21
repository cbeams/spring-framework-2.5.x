package org.springframework.scheduling.quartz;

/**
 * @author robh
 */
public class QuartzTestBean {
	private int exportCount;

	private int importCount;

	public void doImport() {
		++importCount;
		System.out.println("doImport");
	}

	public void doExport() {
		++exportCount;
		System.out.println("doExport");
	}

	public int getExportCount() {
		return exportCount;
	}

	public int getImportCount() {
		return importCount;
	}
}
