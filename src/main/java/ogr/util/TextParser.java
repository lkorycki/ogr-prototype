package ogr.util;

public class TextParser {
	
	public static String lastNode = "";
	
	public TextParser()	{
	}
	
	/**
		Extract or wrap text.
		@param textIn : text to work with
		@param in : extract (true) / wrap (false)
	 */
	public static String parseText(String textIn, boolean in)	{
		
		if(in)	{
			if(textIn.isEmpty()) return "";
			int s = textIn.indexOf("<html>") + 6; // text starts
			int e = textIn.indexOf("</html>"); // text ends
			
			return textIn.substring(s, e);
		}
		else	{
			return "<html>" + textIn + "</html> " + lastNode;
		}		
	}
	
	/**
		Get params that has been saved previously.
		@param node : node with label
	 */
	public static String getParams(String label)	{
		int s = label.indexOf("</html>") + 8;
		return label.substring(s);
	}
	
	/**
		Save the params of the selected node in JGraph
		@param label : label to parse
	 */
	public static void setLastNodeParams(String label)	{
		lastNode = getParams(label);
	}
	
	/**
		Extract the node id from its label.
		@param node : node with label
	 */
	public static String getNodeId(String label)	{
		if(label == null || label.isEmpty()) return "";
		String[] params = label.substring(label.indexOf("</html>") + 8).split(" "); // x y id
		return params[2];
	}
}
