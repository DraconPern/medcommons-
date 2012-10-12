package net.medcommons.router.services.ccrmerge.preprocess;

public interface MergeConstants {
	public static final String HEALTHFRAME_SOURCES = "//x:Source/x:Description[x:Text='Unknown']";
	public static final String CCROBJECTIDS = "//x:CCRDataObjectID";
	
	public static final String HEALTHFRAME_FUZZYDATES = 
		"//x:Medication/x:DateTime/x:ExactDateTime |" +
		"//x:Result/x:DateTime/x:ExactDateTime |" +
		" //x:Procedure/x:DateTime/x:DateTimeRange/x:BeginRange/x:ExactDateTime  | " +
		" //x:Problem/x:DateTime/x:DateTimeRange/x:BeginRange/x:ExactDateTime  | " +
		" //x:Alert/x:DateTime/x:DateTimeRange/x:BeginRange/x:ExactDateTime";
	public static final String MARKED_ATTRIBUTE = "//*[@marked]";
}
